package com.broadheadenterprises.websitescraper;

import com.broadheadenterprises.websitescraper.config.ConfigProperties;
import com.broadheadenterprises.websitescraper.repository.ItemInfoRepository;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class WebsiteScraperStartupRunner implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(WebsiteScraperStartupRunner.class);
    private final ConfigProperties config;
    private final WebClient webClient;
    private final ItemInfoRepository repository;

    @Autowired
    public WebsiteScraperStartupRunner(WebClient webClient, ConfigProperties config, ItemInfoRepository repository) {
        this.webClient = webClient;
        this.config = config;
        this.repository = repository;
    }

    @Override
    public void run(String...args) throws Exception {
        LOG.info("Scanning for product IDs {} through {}", config.getMinId(), config.getMaxId());
        TreeSet<Integer> idsForWhichDataExists = StreamSupport.stream(repository.findAll().spliterator(), true)
                .filter(item -> StringUtils.isNotEmpty(item.getPrice()))
                .map(ItemInfo::getId)
                .collect(Collectors.toCollection(TreeSet::new));
        Integer firstSkippedId = null;
        for (int i = config.getMinId(); i <= config.getMaxId(); i++) {
            if (shouldScanId(idsForWhichDataExists, i)) {
                if (previousIdWasSkipped(firstSkippedId)) {
                    LOG.info("Product IDs: {}-{} already exist in database, skipping.", firstSkippedId, i - 1);
                    firstSkippedId = null;
                }
                repository.save(getProductInfo(i).setId(i));
            } else {
                if (previousIdWasNotSkipped(firstSkippedId)) {
                    firstSkippedId = i;
                }
            }
        }
    }

    private boolean shouldScanId(TreeSet<Integer> idsForWhichDataExists, int i) {
        return idsForWhichDataExists.isEmpty()
                || i > idsForWhichDataExists.last()
                || (config.isRescanExistingProducts() && idsForWhichDataExists.contains(i));
    }

    private boolean previousIdWasNotSkipped(Integer firstSkippedId) {
        return firstSkippedId == null;
    }

    private boolean previousIdWasSkipped(Integer firstSkippedId) {
        return !previousIdWasNotSkipped(firstSkippedId);
    }


    private ItemInfo getProductInfo(int i) {
        ItemInfo itemInfo = new ItemInfo();
        try {
            Optional<ItemInfo> info = scanProduct(i);
            if (info.isPresent()) {
                itemInfo = info.get();
                LOG.info("Retrieved information for id: {}", i);
            } else {
                itemInfo.setErrorMessage("Option of none returned from scanProduct");
                LOG.info("No information retrieved for id: {}", i);
            }
        } catch (IOException | FailingHttpStatusCodeException e) {
            itemInfo.setErrorMessage(e.getMessage());
            LOG.info("Exception when scanning product. This is normal if product has not been created or has been sold: {}", i);
        }
        return itemInfo;
    }

    private Optional<ItemInfo> scanProduct(int productId) throws IOException {
        Optional<HtmlPage> page = getPage(productId);
        try {
            return page.map(p -> ItemInfoMapper.map(p, productId));
        } catch (Exception e) {
            LOG.error("Error occurred while mapping to an ItemInfo object", e);
        }
        return Optional.empty();
    }

    private Optional<HtmlPage> getPage(int product) throws IOException {
        String url = "https://" + config.getDomainName() + "/index.php?id_product=" + product + "&controller=product";
        return Optional.ofNullable(webClient.getPage(url));
    }
}
