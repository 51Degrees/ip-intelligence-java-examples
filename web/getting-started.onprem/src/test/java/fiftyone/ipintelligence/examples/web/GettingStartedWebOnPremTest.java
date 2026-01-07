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

package fiftyone.ipintelligence.examples.web;

import fiftyone.ipintelligence.examples.shared.DataFileHelper;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static fiftyone.ipintelligence.examples.shared.DataFileHelper.ENTERPRISE_DATA_FILE_REL_PATH;
import static fiftyone.ipintelligence.examples.web.GettingStartedWebOnPrem.getResourceBase;
import static fiftyone.pipeline.util.FileFinder.getFilePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GettingStartedWebOnPremTest {
    private static Server SERVER;

    @BeforeClass
    public static void startJetty() throws Exception {
        // Use DataFileHelper to find the data file
        String dataFilePath = DataFileHelper.getDataFileLocation(ENTERPRISE_DATA_FILE_REL_PATH);
        System.setProperty("TestDataFile", dataFilePath);

        SERVER = EmbedJetty.startWebApp(getFilePath(getResourceBase()).getAbsolutePath(), 8081);
    }

    @Test
    public void testWebOnPrem() throws Exception {

        HttpURLConnection connection =
                (HttpURLConnection) new URL("http://localhost:8081/").openConnection();

        int code = connection.getResponseCode();

        // Get the appropriate stream based on response code
        InputStream response;
        if (code >= 200 && code < 400) {
            response = connection.getInputStream();
        } else {
            response = connection.getErrorStream();
        }

        String responseBody = "";
        if (response != null) {
            try (Scanner scanner = new Scanner(response)) {
                scanner.useDelimiter("\\A");
                if (scanner.hasNext()) {
                    responseBody = scanner.next();
                }
            }
        }

        // Print response for debugging
        System.out.println("Response code: " + code);
        System.out.println("Response body length: " + responseBody.length());
        if (responseBody.length() > 0) {
            System.out.println("Response body:\n" + responseBody);
        }

        connection.disconnect();

        // Assert we got a successful response
        assertEquals("Expected HTTP 200 OK response", 200, code);
        assertTrue("Response should not be empty", responseBody.length() > 0);
    }

    @AfterClass
    public static void stopJetty() throws Exception {
        SERVER.stop();
    }
}