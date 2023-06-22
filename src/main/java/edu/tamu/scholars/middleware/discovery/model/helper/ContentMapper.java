package edu.tamu.scholars.middleware.discovery.model.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.tamu.scholars.middleware.discovery.model.Individual;

public class ContentMapper {
    private final Map<String, Collection<Object>> content;

    private ContentMapper(Map<String, Collection<Object>> content) {
        this.content = content;
    }

    public String getValue(String property) {
        List<Object> values = new ArrayList<>(this.content.get(property));

        StringBuilder value = new StringBuilder();

        if (!values.isEmpty()) {
            value.append(String.valueOf(values.get(0)));
        }

        return value.toString();
    }

    public static ContentMapper from(Individual individual) {
        return new ContentMapper(individual.getContent());
    }

}
