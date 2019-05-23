package com.adobe.acs.commons.properties.util;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;

public class TemplateReplacementUtil {

    private static final String PLACHOLDER_BEGIN = "{{";
    private static final String PLACHOLDER_END = "}}";

    /**
     * Checks the current string for whether or not it contains a placeholder.
     *
     * @param string The string to check for a placeholder
     * @return Whether or not the string contains the placeholder values
     */
    public static boolean hasPlaceholder(String string) {
        return string.contains(PLACHOLDER_BEGIN) && string.contains(PLACHOLDER_END)
                && string.indexOf(PLACHOLDER_BEGIN) < string.indexOf(PLACHOLDER_END);
    }

    /**
     * Takes the current string and returns the first placeholder value. Ex: {{value}}
     *
     * @param string The full input string
     * @return The substring of the placeholder
     */
    public static String getPlaceholder(String string) {
        return string.substring(
                string.indexOf(PLACHOLDER_BEGIN),
                string.indexOf(PLACHOLDER_END) + PLACHOLDER_END.length());
    }

    /**
     * Takes the current string and returns the placeholder value. Ex: {{value}}
     *
     * @param string The full input string
     * @return A list of placeholders
     */
    public static List<String> getPlaceholders(String string) {
        String[] placeholders = StringUtils.substringsBetween(
                StringUtils.defaultString(string),
                PLACHOLDER_BEGIN,
                PLACHOLDER_END);

        // StringUtils strips off the delimiters so add them back
        if (placeholders != null) {
            for (int i = 0; i < placeholders.length; i++) {
                placeholders[i] = PLACHOLDER_BEGIN + placeholders[i] + PLACHOLDER_END;
            }
        }

        return placeholders != null ? Lists.newArrayList(placeholders) : Lists.newArrayList();
    }

    /**
     * Takes the current placeholder value and returns the key inside of it. This is used in
     * conjunction with the {@link com.adobe.acs.commons.properties.PropertyAggregatorService}
     * properties.
     *
     * @param placeholder The placeholder input
     * @return The key present inside the placeholder
     */
    public static String getKey(String placeholder) {
        return placeholder.replace(PLACHOLDER_BEGIN,"").replace(PLACHOLDER_END,"");
    }
}