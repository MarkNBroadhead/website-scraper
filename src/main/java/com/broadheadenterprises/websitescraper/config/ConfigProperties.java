package com.broadheadenterprises.websitescraper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties("app")
@Component
public class ConfigProperties {

    private int minId;
    private int maxId;
    private String domainName;
    private boolean rescanExistingProducts;
}
