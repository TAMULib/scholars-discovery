package edu.tamu.scholars.middleware.export.utility;

import java.util.Date;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

import edu.tamu.scholars.middleware.discovery.model.Individual;
import edu.tamu.scholars.middleware.discovery.model.helper.IndividualHelper;

public class FilenameUtility {

    // non global constants
    private final static String SPACE = " ";
    private final static String UNDERSCORE = "_";

    private FilenameUtility() {

    }

    public static String exportTransform(String type) {
        return temporalize(mechanize(type));
    }

    public static String exportTransform(Individual individual) {
        return mechanize(IndividualHelper.as(individual).getLabel());
    }

    private static String mechanize(String value) {
        Locale locale = LocaleContextHolder.getLocale();
        return value.toLowerCase(locale)
            .replace(SPACE, UNDERSCORE);
    }

    private static String temporalize(String value) {
        return String.format("%s_%s", value, String.valueOf(new Date().getTime()));
    }

}
