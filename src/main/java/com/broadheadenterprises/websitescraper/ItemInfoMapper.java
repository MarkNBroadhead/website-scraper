package com.broadheadenterprises.websitescraper;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

public class ItemInfoMapper {
    private static final Logger LOG = LoggerFactory.getLogger(ItemInfoMapper.class);

    private ItemInfoMapper() {
    }

    public static ItemInfo map(HtmlPage htmlPage, int id) {
        ItemInfo itemInfo = new ItemInfo();
        itemInfo.setId(id);
        List<HtmlElement> title = (List<HtmlElement>) htmlPage.getByXPath("//h1[@itemprop='name']");
        if (!title.isEmpty()) {
            itemInfo.setName(title.get(0).getFirstChild().asText());
        }

        List<HtmlElement> sku = (List<HtmlElement>) htmlPage.getByXPath("//span[@itemprop='sku']");
        if (!sku.isEmpty()) {
            itemInfo.setSku(sku.get(0).getFirstChild().asText());
        }

        List<HtmlElement> condition = (List<HtmlElement>) htmlPage.getByXPath("//link[@itemprop='itemCondition']");
        if (!condition.isEmpty()) {
            itemInfo.setItemCondition(condition.get(0).getNextSibling().getFirstChild().asText());
        }

        List<HtmlElement> description = (List<HtmlElement>) htmlPage.getByXPath("//div[@itemprop='description']");
        if (!description.isEmpty()) {
            itemInfo.setShortDescription(description.get(0).getFirstChild().getFirstChild().asText());
        }

        List<HtmlElement> price = (List<HtmlElement>) htmlPage.getByXPath("//span[@itemprop='price']");
        if (!price.isEmpty()) {
            itemInfo.setPrice(price.get(0).getFirstChild().asText());
        }

        List<HtmlElement> dataTable = (List<HtmlElement>) htmlPage.getByXPath("//table[contains(@class, table-data-sheet)]");
        HashMap<String, String> kvPairMap = new HashMap();
        if (!dataTable.isEmpty()) {
            DomNode tableBody = dataTable.get(0).getFirstChild();
            DomNodeList<DomNode> rows = tableBody.getChildNodes();
            for (DomNode node : rows) {
                DomNode firstChild = node.getFirstChild();
                String key = null;
                String value = null;
                if (firstChild != null) {
                    DomNode secondChild = firstChild.getFirstChild();
                    if (secondChild != null) {
                        key = secondChild.asText();
                    }
                    DomNode nextSibling = firstChild.getNextSibling();
                    if (nextSibling != null) {
                        DomNode siblingsChild = nextSibling.getFirstChild();
                        value = siblingsChild.asText();
                    }
                }
                if (key != null && value != null) {
                    kvPairMap.put(key, value);
                } else {
                    LOG.error("Issue getting Key Value pairs from table for id {} node follows: {}.", id, node.toString());
                }
            }
            addTableToItemInfo(kvPairMap, itemInfo);
        }

        return itemInfo;
    }

    private static ItemInfo addTableToItemInfo(HashMap<String, String> map, ItemInfo itemInfo) {
        BiConsumer<String, String> biConsumer = (key, value) -> {
            switch (key) {
                case "Storage - Controller Model":
                    itemInfo.setStorageControllerModel(value);
                    break;
                case "Storage - Bays Quantity":
                    itemInfo.setStorageBayCount(value);
                    break;
                case "Storage - Drive Type":
                    itemInfo.setStorageDriveModel(value);
                    break;
                case "Optical":
                    itemInfo.setOptical(value);
                    break;
                case "PSU - Wattage":
                    itemInfo.setPsuWattage(value);
                    break;
                case "CPU - Clock Speed":
                    itemInfo.setCpuClockSpeed(value);
                    break;
                case "Storage - Drive Capacity":
                    itemInfo.setStorageDriveIncludedCapacity(value);
                    break;
                case "RAM - Speed":
                    itemInfo.setRamSpeed(value);
                    break;
                case "CPU - Series":
                    itemInfo.setCpuSeries(value);
                    break;
                case "CPU - Cores":
                    itemInfo.setCpuCores(value);
                    break;
                case "Storage - Drive QTY Included":
                    itemInfo.setStorageDriveQtyIncluded(value);
                    break;
                case "CPU - Model":
                    itemInfo.setCpuModel(value);
                    break;
                case "PSU - QTY":
                    itemInfo.setPsuQty(value);
                    break;
                case "Rails":
                    itemInfo.setRailsIncluded(value);
                    break;
                case "R2 Status":
                    itemInfo.setR2Status(value);
                    break;
                case "Storage - Drive Model":
                    itemInfo.setStorageDriveModel(value);
                    break;
                case "Storage - Bay Size":
                    itemInfo.setStorageBaySize(value);
                    break;
                case "CPU - QTY":
                    itemInfo.setCpuCount(value);
                    break;
                case "RAM - # of Sticks Included":
                    itemInfo.setStorageBayCount(value);
                    break;
                case "RAM - Total Memory Included":
                    itemInfo.setRamConfiguration(value);
                    break;
                case "CPU - Manufacturer":
                    itemInfo.setCpuManufacturer(value);
                    break;
                case "RAM - Total Slots":
                    itemInfo.setRamTotalSlots(value);
                    break;
                case "Faceplate Included":
                    itemInfo.setFaceplateIncluded(value);
                    break;
                case "Notes":
                    itemInfo.setNotes(value);
                    break;
                // TODO: Add any other data into a catchall field and log it.
                // TODO: Persist the entire webpage, status code (like 404) just in case
                // TODO: Persist URL
            }
        };
        map.forEach(biConsumer);
        return itemInfo;
    }
}
