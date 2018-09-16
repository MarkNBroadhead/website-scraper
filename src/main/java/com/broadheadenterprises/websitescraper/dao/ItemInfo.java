package com.broadheadenterprises.websitescraper.dao;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

@Data
@Accessors(chain = true)
@Entity
public class ItemInfo {
    @Id
    private int id;
    private String name;
    private String sku;
    private String itemCondition;
    @Lob
    private String shortDescription;
    private String price;
    private String r2Status;
    private String cpuCount;
    private String cpuManufacturer;
    private String cpuSeries;
    private String cpuModel;
    private String cpuClockSpeed;
    private String cpuCores;
    private String ramSpeed;
    private String ramConfiguration;
    private String ramTotalAvailableSlots;
    private String ramNumberSticksIncluded;
    private String storageControllerModel;
    private String storageBayCount;
    private String storageBaySize;
    private String storageDriveQtyIncluded;
    private String storageDriveIncludedCapacity;
    private String storageDriveModel;
    private String optical;
    private String psuQty;
    private String psuWattage;
    private String faceplateIncluded;
    private String railsIncluded;
    private String notes;
    @Lob
    private String errorMessage;
    private boolean isSold = false;
}
