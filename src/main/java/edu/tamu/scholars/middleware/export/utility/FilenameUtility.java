package edu.tamu.scholars.middleware.export.utility;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.discovery.model.Organization;
import edu.tamu.scholars.middleware.discovery.model.Person;

public class FilenameUtility {

    private final static String UNDERSCORE = "_";
    private final static String NAME = "name";
    private final static String LAST_NAME = "lastName";
    private final static String FIRST_NAME = "firstName";

    private FilenameUtility() {

    }

    public static String normalizeExportFilename(String label) {
        return prefixWithTimestampAfterUnderscore(localizeWhileUnderscoreReplaceSpaceWithUnderscore(label));
    }

    public static String normalizeExportFilename(AbstractIndexDocument refDoc) {
        return normalizeExportFilename((Individual) refDoc);
    }

    public static String normalizeExportFilename(Individual individual) {
        Map<String, Collection<Object>> content = individual.getContent();
        StringBuilder label = new StringBuilder();

        String clazz = individual.getClazz();

        if (clazz.equals(Organization.class.getSimpleName()) && content.containsKey(NAME)) {
            label.append(content.get(NAME).iterator().next())
                .append(UNDERSCORE);
        } else if (clazz.equals(Person.class.getSimpleName()) && content.containsKey(LAST_NAME)) {
             label.append(content.get(LAST_NAME).iterator().next())
                .append(UNDERSCORE);

            if (content.containsKey(FIRST_NAME)) {
                label.append(content.get(FIRST_NAME).iterator().next())
                    .append(UNDERSCORE);
            }
        }

        return localizeWhileUnderscoreReplaceSpaceWithUnderscore(label
            .append(individual.getId())
            .toString());
    }

    private static String localizeWhileUnderscoreReplaceSpaceWithUnderscore(String value) {
        Locale locale = LocaleContextHolder.getLocale();
        return value.toLowerCase(locale)
            .replace(StringUtils.SPACE, UNDERSCORE);
    }

    private static String prefixWithTimestampAfterUnderscore(String value) {
        return String.format("%s_%s", value, String.valueOf(new Date().getTime()));
    }

}
