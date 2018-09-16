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
        TreeSet<Integer> allIdRecordsInDb = StreamSupport.stream(repository.findAll().spliterator(), true)
                .map(ItemInfo::getId)
                .collect(Collectors.toCollection(TreeSet::new));
        TreeSet<Integer> idsForWhichProductsExist = StreamSupport.stream(repository.findAll().spliterator(), true)
                .filter(item -> StringUtils.isNotEmpty(item.getPrice()) && !item.isSold())
                .map(ItemInfo::getId)
                .collect(Collectors.toCollection(TreeSet::new));
        Integer firstSkippedId = null;
        for (int id = config.getMinId(); id <= config.getMaxId(); id++) {
            if (shouldScanId(idsForWhichProductsExist, allIdRecordsInDb, id)) {
                if (previousIdWasSkipped(firstSkippedId)) {
                    LOG.info("Product IDs: {}-{} already exist in database, skipping.", firstSkippedId, id - 1);
                    firstSkippedId = null;
                }
                ItemInfo productInfo = getProductInfo(id).setId(id);
                if (productHasSold(idsForWhichProductsExist, id, productInfo)) {
                    productInfo = repository.findById(id).orElse(productInfo).setSold(true);
                    productInfo.setSold(true);
                }
                repository.save(productInfo);
            } else {
                if (previousIdWasNotSkipped(firstSkippedId)) {
                    firstSkippedId = id;
                }
            }
        }
    }

    private boolean productHasSold(TreeSet<Integer> idsForWhichProductsExist, int id, ItemInfo productInfo) {
        return idsForWhichProductsExist.contains(id) && StringUtils.isNotEmpty(productInfo.getErrorMessage());
    }

    private boolean shouldScanId(TreeSet<Integer> idsForWhichDataExists, TreeSet<Integer> allRecordsInDb, int id) {
        return !allRecordsInDb.contains(id)
                || id > idsForWhichDataExists.last()
                || (config.isRescanExistingProducts() && idsForWhichDataExists.contains(id));
    }

    private boolean previousIdWasNotSkipped(Integer firstSkippedId) {
        return firstSkippedId == null;
    }

    private boolean previousIdWasSkipped(Integer firstSkippedId) {
        return !previousIdWasNotSkipped(firstSkippedId);
    }


    private ItemInfo getProductInfo(int id) {
        ItemInfo itemInfo = new ItemInfo();
        try {
            Optional<ItemInfo> info = scanProduct(id);
            if (info.isPresent()) {
                itemInfo = info.get();
                LOG.info("Retrieved information for id: {}", id);
            } else {
                itemInfo.setErrorMessage("Option of none returned from scanProduct, this is likely due to a POJO mapping issue. Check application logs.");
                LOG.error("No information retrieved for id: {}", id);
            }
        } catch (IOException | FailingHttpStatusCodeException e) {
            itemInfo.setErrorMessage(e.getMessage());
            LOG.info("Exception when scanning product. This is normal if product has not been created or has been sold: {}", id);
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
