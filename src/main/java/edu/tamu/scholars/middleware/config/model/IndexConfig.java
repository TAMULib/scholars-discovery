package edu.tamu.scholars.middleware.config.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "middleware.index")
public class IndexConfig {

    private String cron = "0 0 0 * * SUN";

    private String zone = "America/Chicago";

    private Boolean onStartup = true;

    private Integer onStartupDelay = 10000;

    private Boolean uploadConfigsetOnStartup = true;

    private Boolean createOnStartup = true;

    private Boolean deleteOnStartup = true;

    private Integer batchSize = 10000;

    private Boolean resumeIndividually = true;

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

    public Boolean getOnStartup() {
        return onStartup;
    }

    public void setOnStartup(Boolean onStartup) {
        this.onStartup = onStartup;
    }

    public Integer getOnStartupDelay() {
        return onStartupDelay;
    }

    public void setOnStartupDelay(Integer onStartupDelay) {
        this.onStartupDelay = onStartupDelay;
    }

    public Boolean getUploadConfigsetOnStartup() {
        return uploadConfigsetOnStartup;
    }

    public void setUploadConfigsetOnStartup(Boolean uploadConfigsetOnStartup) {
        this.uploadConfigsetOnStartup = uploadConfigsetOnStartup;
    }

    public Boolean getCreateOnStartup() {
        return createOnStartup;
    }

    public void setCreateOnStartup(Boolean createOnStartup) {
        this.createOnStartup = createOnStartup;
    }

    public Boolean getDeleteOnStartup() {
        return deleteOnStartup;
    }

    public void setDeleteOnStartup(Boolean deleteOnStartup) {
        this.deleteOnStartup = deleteOnStartup;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Boolean getResumeIndividually() {
        return resumeIndividually;
    }

    public void setResumeIndividually(Boolean resumeIndividually) {
        this.resumeIndividually = resumeIndividually;
    }

}
