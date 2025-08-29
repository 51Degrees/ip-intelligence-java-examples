/* *********************************************************************
 * This Original Work is copyright of 51 Degrees Mobile Experts Limited.
 * Copyright 2025 51 Degrees Mobile Experts Limited, Davidson House,
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
import fiftyone.pipeline.engines.data.AspectPropertyValue;
import fiftyone.pipeline.engines.data.AspectPropertyValueDefault;
import fiftyone.pipeline.engines.exceptions.PropertyMissingException;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class PropertyHelper {
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
     * Helper to get the value of an IP Intelligence string list property - strongly typed
     */
    public static String asStringProperty(AspectPropertyValue<List<IWeightedValue<String>>> property) {
        if (property == null || !property.hasValue()) {
            String message = property != null ? property.getNoValueMessage() : "No data available";
            return "Unknown. " + message;
        } else {
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < property.getValue().size(); i++) {
                if (i > 0) values.append(", ");
                IWeightedValue<String> weightedValue = property.getValue().get(i);
                values.append(weightedValue.getValue());
            }
            return values.toString();
        }
    }
    
    /**
     * Helper to get the value of an IP Intelligence integer list property - strongly typed
     */
    public static String asIntegerProperty(AspectPropertyValue<List<IWeightedValue<Integer>>> property) {
        if (property == null || !property.hasValue()) {
            String message = property != null ? property.getNoValueMessage() : "No data available";
            return "Unknown. " + message;
        } else {
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < property.getValue().size(); i++) {
                if (i > 0) values.append(", ");
                IWeightedValue<Integer> weightedValue = property.getValue().get(i);
                values.append(weightedValue.getValue().toString());
            }
            return values.toString();
        }
    }
    
    /**
     * Helper to get the value of an IP Intelligence float list property - strongly typed
     */
    public static String asFloatProperty(AspectPropertyValue<List<IWeightedValue<Float>>> property) {
        if (property == null || !property.hasValue()) {
            String message = property != null ? property.getNoValueMessage() : "No data available";
            return "Unknown. " + message;
        } else {
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < property.getValue().size(); i++) {
                if (i > 0) values.append(", ");
                IWeightedValue<Float> weightedValue = property.getValue().get(i);
                Object val = weightedValue.getValue();
                // Handle the case where the value might be a String instead of Float
                if (val instanceof Float) {
                    values.append(String.format("%.6f", val));
                } else {
                    values.append(val.toString());
                }
            }
            return values.toString();
        }
    }
    
    /**
     * Helper to get the value of an IP Intelligence InetAddress list property - strongly typed
     */
    public static String asIPAddressProperty(AspectPropertyValue<List<IWeightedValue<java.net.InetAddress>>> property) {
        if (property == null || !property.hasValue()) {
            String message = property != null ? property.getNoValueMessage() : "No data available";
            return "Unknown. " + message;
        } else {
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < property.getValue().size(); i++) {
                if (i > 0) values.append(", ");
                IWeightedValue<java.net.InetAddress> weightedValue = property.getValue().get(i);
                Object val = weightedValue.getValue();
                // Handle the case where the value might be a String instead of InetAddress
                if (val instanceof java.net.InetAddress) {
                    values.append(((java.net.InetAddress) val).getHostAddress());
                } else {
                    values.append(val.toString());
                }
            }
            return values.toString();
        }
    }

}
