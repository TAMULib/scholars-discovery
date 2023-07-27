package edu.tamu.scholars.middleware.discovery.service.component;

import java.lang.reflect.Field;

import edu.tamu.scholars.middleware.discovery.annotation.FieldType;

// TODO: make class members private and add getters and setters
// use public static method and private constructor
// check if any class members can be final
public class NamedTypedField {
    public String name;
    public FieldType fieldType;
    public Field field;
}
