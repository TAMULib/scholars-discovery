package edu.tamu.scholars.middleware.model;

public enum GroupOp {

    // @formatter:off
    AND("AND"),
    OR("OR");
    // @formatter:on

    private final String key;

    GroupOp(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
