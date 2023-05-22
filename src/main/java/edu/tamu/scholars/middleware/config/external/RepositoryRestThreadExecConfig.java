package edu.tamu.scholars.middleware.config.external;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "middleware.exec-config")
public class RepositoryRestThreadExecConfig {

    private int corePoolSize;

    private int maxPoolSize;

    private int queueCapacity;

    private String threadNamePrefix;

    private long asyncRequestTimeout;

    public RepositoryRestThreadExecConfig() {
        this.corePoolSize = 16;
        this.maxPoolSize = 128;
        this.queueCapacity = 64;
        this.threadNamePrefix = "async-task-executor-";
        this.asyncRequestTimeout = 900000;
    }

    public int getCorePoolSize() {
        return this.corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getQueueCapacity() {
        return this.queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public String getThreadNamePrefix() {
        return this.threadNamePrefix;
    }

    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    public long getAsyncRequestTimeout() {
        return this.asyncRequestTimeout;
    }

    public void setAsyncRequestTimeout(long asyncRequestTimeout) {
        this.asyncRequestTimeout = asyncRequestTimeout;
    }

}
