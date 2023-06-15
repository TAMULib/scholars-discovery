package edu.tamu.scholars.middleware.utility;

import java.text.ParseException;
import java.time.Instant;
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

    public static String parseYear(String value) throws IllegalArgumentException, ParseException {
        return String.valueOf(parseZonedDateTime(value).getYear());
    }

    public static ZonedDateTime parseZonedDateTime(String value) throws IllegalArgumentException, ParseException {
        return parseDate(value)
            .toInstant()
            .atZone(ZoneId.systemDefault());
    }

    public static Date parseDate(String value) throws IllegalArgumentException, ParseException {
        Locale locale = LocaleContextHolder.getLocale();
        return DateUtils.parseDate(value, locale, datePatterns);
    }

    public static int getYear() {
        return ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
            .getYear();
    }

}
