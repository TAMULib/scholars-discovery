package edu.tamu.scholars.middleware.export.utility;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 */
public class ExtractIdUtility {

    private ExtractIdUtility() {

    }

    /**
     * Converts list of string with '::' prefix into a list of ids.
     */
    public static List<String> extractIds(List<String> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return Collections.emptyList();
        }
        return inputs.stream()
                     .map(s -> s == null || s.isEmpty() ? null : (s.contains("::") ? s.split("::")[1] : s))
                     .collect(Collectors.toList());
    }

}
