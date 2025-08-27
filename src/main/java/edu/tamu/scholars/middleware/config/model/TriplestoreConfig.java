package edu.tamu.scholars.middleware.config.model;

import edu.tamu.scholars.middleware.service.TDBTriplestore;
import edu.tamu.scholars.middleware.service.Triplestore;

/**
 * {@link MiddlewareConfig} configuration to specify triplestore.
 * 
 * <p>See `middleware.triplestore` in src/main/resources/application.yml.</p>
 */
public class TriplestoreConfig {

    private Class<? extends Triplestore> type = TDBTriplestore.class;

    private String directory = "triplestore";

    private String layoutType = "layout2/hash";

    private String databaseType = "MySQL";

    private String datasourceUrl;

    private String username;

    private String password;

    private boolean jdbcStream = true;

    private int jdbcFetchSize = 8;

    private boolean streamGraphApi = true;

    private boolean annotateGeneratedSql = false;

    public Class<? extends Triplestore> getType() {
        return type;
    }

    public void setType(Class<? extends Triplestore> type) {
        this.type = type;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(String layoutType) {
        this.layoutType = layoutType;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getDatasourceUrl() {
        return datasourceUrl;
    }

    public void setDatasourceUrl(String datasourceUrl) {
        this.datasourceUrl = datasourceUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isJdbcStream() {
        return jdbcStream;
    }

    public void setJdbcStream(boolean jdbcStream) {
        this.jdbcStream = jdbcStream;
    }

    public int getJdbcFetchSize() {
        return jdbcFetchSize;
    }

    public void setJdbcFetchSize(int jdbcFetchSize) {
        this.jdbcFetchSize = jdbcFetchSize;
    }

    public boolean isStreamGraphApi() {
        return streamGraphApi;
    }

    public void setStreamGraphApi(boolean streamGraphApi) {
        this.streamGraphApi = streamGraphApi;
    }

    public boolean isAnnotateGeneratedSql() {
        return annotateGeneratedSql;
    }

    public void setAnnotateGeneratedSql(boolean annotateGeneratedSql) {
        this.annotateGeneratedSql = annotateGeneratedSql;
    }

}
