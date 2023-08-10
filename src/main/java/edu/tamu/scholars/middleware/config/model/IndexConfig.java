package edu.tamu.scholars.middleware.config.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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

}
