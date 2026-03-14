/* *********************************************************************
 * This Original Work is copyright of 51 Degrees Mobile Experts Limited.
 * Copyright 2026 51 Degrees Mobile Experts Limited, Davidson House,
 * Forbury Square, Reading, Berkshire, United Kingdom RG1 3EU.
 *
 * This Original Work is licensed under the European Union Public Licence
 * (EUPL) v.1.2 and is subject to its terms as set out below.
 *
 * If a copy of the EUPL was not distributed with this file, You can obtain
 * one at https://opensource.org/licenses/EUPL-1.2.
 *
 * The 'Compatible Licences' set out in the Appendix to the EUPL (as may be
 * amended by the European Commission) shall be deemed incompatible for
 * the purposes of the Work and the provisions of the compatibility
 * clause in Article 5 of the EUPL shall not apply.
 *
 * If using the Work as, or as part of, a network application, by
 * including the attribution notice(s) required under Article 5 of the EUPL
 * in the end user terms of the application under an appropriate heading,
 * such notice(s) shall fulfill the requirements of that article.
 * ********************************************************************* */

package fiftyone.ipintelligence.examples.shared;

import fiftyone.pipeline.core.data.IWeightedValue;
import fiftyone.pipeline.core.data.WktString;
import fiftyone.pipeline.engines.data.AspectPropertyValue;
import fiftyone.pipeline.engines.data.AspectPropertyValueDefault;
import fiftyone.pipeline.engines.exceptions.PropertyMissingException;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class PropertyHelper {

    /**
     * Unwrap a value that may be a List of IWeightedValue at runtime
     * (due to Java generics type erasure, the on-premise engine may return
     * List&lt;IWeightedValue&lt;T&gt;&gt; even when the declared type is T).
     * For single-element lists, returns just the value.
     * For multi-element lists, returns values joined with ", ".
     * @param value the raw value from AspectPropertyValue.getValue()
     * @return the unwrapped string representation
     */
    @SuppressWarnings("unchecked")
    private static String unwrapValue(Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                return "Unknown";
            }
            return list.stream()
                    .map(item -> {
                        if (item instanceof IWeightedValue) {
                            Object inner = ((IWeightedValue<?>) item).getValue();
                            return inner != null ? inner.toString() : "Unknown";
                        }
                        return item != null ? item.toString() : "Unknown";
                    })
                    .collect(Collectors.joining(", "));
        }
        return value != null ? value.toString() : "Unknown";
    }
    /**
     * Try to carry out a 'get' on a property getter, and catch a
     * {@link PropertyMissingException} to avoid the example breaking if the
     * resource key, or data file are not configured correctly by the user.
     *
     * @param supplier to use e.g. IPIntelligenceData::getIsMobile()
     * @return value
     */
    public static <T> AspectPropertyValue<T> tryGet(Supplier<AspectPropertyValue<T>> supplier) {
        try {
            return supplier.get();
        } catch (PropertyMissingException e) {
            String message =
                    "The property '" + e.getPropertyName() + "' is not " +
                    "available in this data file. See data file options " +
                    "<a href=\"https://51degrees.com/pricing\">here</a>";
            AspectPropertyValue<T> result = new AspectPropertyValueDefault<>();
            result.setNoValueMessage(message);
            return result;
        }
    }

    /**
     * Helper to get the value of an IP Intelligence string property.
     * Handles runtime List&lt;IWeightedValue&gt; values due to type erasure.
     */
    public static String asStringProperty(AspectPropertyValue<String> property) {
        if (property == null || !property.hasValue()) {
            String message = property != null ? property.getNoValueMessage() : "No data available";
            return "Unknown. " + message;
        } else {
            return unwrapValue(property.getValue());
        }
    }

    /**
     * Helper to get the value of an IP Intelligence integer property.
     * Handles runtime List&lt;IWeightedValue&gt; values due to type erasure.
     */
    public static String asIntegerProperty(AspectPropertyValue<Integer> property) {
        if (property == null || !property.hasValue()) {
            String message = property != null ? property.getNoValueMessage() : "No data available";
            return "Unknown. " + message;
        } else {
            return unwrapValue(property.getValue());
        }
    }

    /**
     * Helper to get the value of an IP Intelligence float property.
     * Handles runtime List&lt;IWeightedValue&gt; values due to type erasure.
     */
    public static String asFloatProperty(AspectPropertyValue<Float> property) {
        if (property == null || !property.hasValue()) {
            String message = property != null ? property.getNoValueMessage() : "No data available";
            return "Unknown. " + message;
        } else {
            return unwrapValue(property.getValue());
        }
    }

    /**
     * Helper to get the value of an IP Intelligence InetAddress property.
     * Handles runtime List&lt;IWeightedValue&gt; values due to type erasure.
     */
    public static String asIPAddressProperty(AspectPropertyValue<java.net.InetAddress> property) {
        if (property == null || !property.hasValue()) {
            String message = property != null ? property.getNoValueMessage() : "No data available";
            return "Unknown. " + message;
        } else {
            return unwrapValue(property.getValue());
        }
    }

    /**
     * Helper to get the value of an IP Intelligence WktString property.
     * Handles runtime List&lt;IWeightedValue&gt; values due to type erasure.
     */
    public static String asWktStringProperty(AspectPropertyValue<WktString> property) {
        if (property == null || !property.hasValue()) {
            String message = property != null ? property.getNoValueMessage() : "No data available";
            return "Unknown. " + message;
        } else {
            return unwrapValue(property.getValue());
        }
    }

    /**
     * Generic helper to get the value of a property as a string.
     * Used primarily for testing and simple property display.
     * @param value the property value
     * @param <T> the type
     * @return a string representation of the value or a "no value" message
     */
    public static <T> String asString(AspectPropertyValue<T> value) {
        if (value.hasValue()) {
            return unwrapValue(value.getValue());
        }
        return "Unknown. " + value.getNoValueMessage();
    }

}
