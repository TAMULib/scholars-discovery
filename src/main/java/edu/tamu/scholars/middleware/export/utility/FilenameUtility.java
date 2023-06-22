package edu.tamu.scholars.middleware.export.utility;

import java.util.Date;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

import edu.tamu.scholars.middleware.discovery.model.AbstractIndexDocument;
import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.discovery.model.helper.IndividualHelper;

public class FilenameUtility {

    private final static String SPACE = " ";
    private final static String UNDERSCORE = "_";

    private FilenameUtility() {

    }

    public static String normalizeExportFilename(String label) {
        return prefixWithTimestampAfterUnderscore(localizeWhileUnderscoreReplaceSpaceWithUnderscore(label));
    }

    public static String normalizeExportFilename(AbstractIndexDocument refDoc) {
        return normalizeExportFilename((Individual) refDoc);
    }

    public static String normalizeExportFilename(Individual individual) {
        return localizeWhileUnderscoreReplaceSpaceWithUnderscore(IndividualHelper.as(individual).getLabel());
    }

    private static String localizeWhileUnderscoreReplaceSpaceWithUnderscore(String value) {
        Locale locale = LocaleContextHolder.getLocale();
        return value.toLowerCase(locale)
            .replace(SPACE, UNDERSCORE);
    }

    private static String prefixWithTimestampAfterUnderscore(String value) {
        return String.format("%s_%s", value, String.valueOf(new Date().getTime()));
    }

}
