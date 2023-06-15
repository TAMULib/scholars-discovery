package edu.tamu.scholars.middleware.discovery.utility;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;

public class DateUtility {

    private DateUtility() {

    }

    public static int ageInYearsFromEpochSecond(long timestamp) {
        return Period.between(
            Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate(),
            Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()   
        ).getYears();
    }
    
}
