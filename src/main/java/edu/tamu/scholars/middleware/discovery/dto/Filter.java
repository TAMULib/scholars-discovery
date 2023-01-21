package edu.tamu.scholars.middleware.discovery.dto;

public class Filter {

    private final String field;

    private final String value;

    private Filter(String field, String value) {
        super();
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
    
    public static Filter of(String field, String value) {
        return new Filter(field, value);
    }

}
