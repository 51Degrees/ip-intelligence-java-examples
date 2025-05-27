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

package fiftyone.ipintelligence.examples.console;

import fiftyone.ipintelligence.shared.testhelpers.KeyUtils;
import org.junit.Ignore;
import org.junit.Test;

import static fiftyone.ipintelligence.examples.console.UpdateDataFile.run;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;

@Ignore
public class UpdateDataFileTest {
    @Test
    public void updateExampleTest () throws Exception {
        String licenseKey = KeyUtils.getNamedKey(UpdateDataFile.UPDATE_EXAMPLE_LICENSE_KEY_NAME);
        assumeFalse("Skipping test, no license key found",
                KeyUtils.isInvalidKey(licenseKey));
        try {
            run(null, licenseKey, false);
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("ERR_429")) {
                assumeFalse("Download failed because of too many requests error", true);
            }
            e.printStackTrace();
            fail(e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.getMessage());
        }
    }

}