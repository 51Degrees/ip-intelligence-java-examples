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
import fiftyone.pipeline.core.data.WeightedValue;
import fiftyone.pipeline.engines.data.AspectPropertyValue;
import fiftyone.pipeline.engines.data.AspectPropertyValueDefault;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PropertyHelperTest {

    @Test
    public void testAsStringBoolean() {
        String expected = "true";
        ArrayList<IWeightedValue<String>> weightedValues = new ArrayList<IWeightedValue<String>>() {
            {
                add(new WeightedValue<>(1, expected));
            }
        };
        AspectPropertyValueDefault<List<IWeightedValue<String>>> aspectProperty =
                new AspectPropertyValueDefault<>(weightedValues);
        assertEquals(expected, PropertyHelper.asStringProperty(aspectProperty));
    }

    @Test
    public void testAsStringStringArray() {
        String firstElement = "one";
        String secondElement = "two";
        String delimiter = ", ";

        ArrayList<IWeightedValue<String>> weightedValues = new ArrayList<IWeightedValue<String>>() {
            {
                add(new WeightedValue<>(1, firstElement));
                add(new WeightedValue<>(1, secondElement));
            }
        };
        AspectPropertyValueDefault<List<IWeightedValue<String>>> aspectProperty =
                new AspectPropertyValueDefault<>(weightedValues);
        assertEquals(StringUtils.joinWith(delimiter, firstElement,secondElement)
                , PropertyHelper.asStringProperty(aspectProperty));
    }


    @Test
    public void testAsNoValue() {
        AspectPropertyValue<List<Boolean>> test = new AspectPropertyValueDefault<>();
        assertTrue(PropertyHelper.asStringProperty(null).startsWith("Unknown"));
    }
}