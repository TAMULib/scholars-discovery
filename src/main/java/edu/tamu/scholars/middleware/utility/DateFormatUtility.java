package edu.tamu.scholars.middleware.utility;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.context.i18n.LocaleContextHolder;

public class DateFormatUtility {

    private static final String[] datePatterns = {
        "yyyy",
        "E MMM dd HH:mm:ss z yyyy",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "dd-MM-yy",
        "MM-dd-yyyy",
        "yyyy-MM-dd HH:mm:ss",
        "EEEEE MMMMM yyyy HH:mm:ss.SSSZ"
    };

    public static String parseOutYear(String value) throws IllegalArgumentException, ParseException {
        return String.valueOf(parse(value).getYear());
    }

    public static ZonedDateTime parse(String value) throws IllegalArgumentException, ParseException {
        Locale locale = LocaleContextHolder.getLocale();
        Date date = DateUtils.parseDate(value, locale, datePatterns);
        return date.toInstant().atZone(ZoneId.systemDefault());
    }

}
