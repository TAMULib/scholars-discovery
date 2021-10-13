package edu.tamu.scholars.middleware.graphql.provider;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.leangen.graphql.metadata.DefaultValue;
import io.leangen.graphql.metadata.strategy.value.DefaultValueProvider;

public class DefaultHighlightProvider implements DefaultValueProvider {

    public DefaultValue getDefaultValue(AnnotatedElement targetElement, AnnotatedType type, DefaultValue initialValue) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("fields", new ArrayList<String>());
        return new DefaultValue(values);
    }

}
