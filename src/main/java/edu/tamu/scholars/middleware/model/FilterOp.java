package edu.tamu.scholars.middleware.model;

public enum FilterOp {

    // @formatter:off
    BETWEEN("BETWEEN"),
    CONTAINS("CONTAINS"),
    ENDS_WITH("ENDS_WITH"),
    EQUALS("EQUALS"),
    EXPRESSION("EXPRESSION"),
    FUZZY("FUZZY"),
    NOT_EQUALS("NOT_EQUALS"),
    STARTS_WITH("STARTS_WITH"),
    RAW("RAW");
    // @formatter:on

    private final String key;

    FilterOp(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
