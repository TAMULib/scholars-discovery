package edu.tamu.scholars.middleware.config.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Injectable middleware configuration to specify index properties.
 * 
 * <p>See `middleware.index` in src/main/resources/application.yml.</p>
 */
@Component
@ConfigurationProperties(prefix = "middleware.index")
public class IndexConfig {

    private String name = "scholars-discovery";

    private String cron = "0 0 0 * * SUN";

    private String zone = "America/Chicago";

    private boolean schematize = true;

    private boolean onStartup = true;

    private boolean enableIndividualOnBatchFail = true;

    private int onStartupDelay = 10000;

    private int batchSize = 10000;

    private boolean cacheEnabled = false;

    private boolean cacheReadOnly = false;

    private boolean clearCache = true;

    private String cacheLocation = "src/test/resources";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public boolean isSchematize() {
        return schematize;
    }

    public void setSchematize(boolean schematize) {
        this.schematize = schematize;
    }

    public boolean isOnStartup() {
        return onStartup;
    }

    public void setOnStartup(boolean onStartup) {
        this.onStartup = onStartup;
    }

    public boolean isEnableIndividualOnBatchFail() {
        return enableIndividualOnBatchFail;
    }

    public void setEnableIndividualOnBatchFail(boolean enableIndividualOnBatchFail) {
        this.enableIndividualOnBatchFail = enableIndividualOnBatchFail;
    }

    public int getOnStartupDelay() {
        return onStartupDelay;
    }

    public void setOnStartupDelay(int onStartupDelay) {
        this.onStartupDelay = onStartupDelay;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public boolean isCacheReadOnly() {
        return cacheReadOnly;
    }

    public void setCacheReadOnly(boolean cacheReadOnly) {
        this.cacheReadOnly = cacheReadOnly;
    }

    public boolean isClearCache() {
        return clearCache;
    }

    public void setClearCache(boolean clearCache) {
        this.clearCache = clearCache;
    }

    public String getCacheLocation() {
        return cacheLocation;
    }

    public void setCacheLocation(String cacheLocation) {
        this.cacheLocation = cacheLocation;
    }

}
