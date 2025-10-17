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

/*!
 * @example GettingStartedWebOnPrem.java
 *
 * This example shows how to use 51Degrees On-premise IP Intelligence in a web application to determine location and network details from IP addresses.
 *
 * You will learn:
 * 
 * 1. How to configure a Pipeline in a web application that uses 51Degrees On-premise IP Intelligence
 * 2. How evidence from the web request is automatically passed to the Pipeline
 * 3. How to retrieve the results in your web application
 * 
 * This example is available in full on [GitHub](https://github.com/51Degrees/ip-intelligence-java-examples/blob/master/web/getting-started.onprem/src/main/java/fiftyone/ipintelligence/examples/web/GettingStartedWebOnPrem.java).
 *
 * This example requires an enterprise IP Intelligence data file (.ipi). 
 * To obtain an enterprise data file for testing, please [contact us](https://51degrees.com/contact-us).
 *
 * Required Maven Dependencies:
 * - [com.51degrees:ip-intelligence](https://central.sonatype.com/artifact/com.51degrees/ip-intelligence)
 * - [com.51degrees:pipeline-web](https://central.sonatype.com/artifact/com.51degrees/pipeline-web)
 *
 * ## Overview
 *
 * The `PipelineFilter` is used to intercept requests and perform IP Intelligence. The results
 * will be stored in the HttpServletRequest object.
 *
 * The results of detection can be accessed by using a FlowDataProvider which
 * is responsible for managing the lifecycle of the flowData - do NOT dispose
 * ```{java}
 * FlowData flowData = flowDataProvider.getFlowData(request);
 * IPIntelligenceData ipData = flowData.get(IPIntelligenceData.class);
 * ...
 * ```
 *
 * IP Intelligence operates entirely server-side, so all results are available immediately
 * after processing through the Pipeline API shown above.
 */

package fiftyone.ipintelligence.examples.web;

import fiftyone.ipintelligence.shared.IPIntelligenceData;
import fiftyone.pipeline.core.data.FlowData;
import fiftyone.pipeline.core.flowelements.FlowElement;
import fiftyone.pipeline.web.services.FlowDataProviderCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static fiftyone.common.testhelpers.LogbackHelper.configureLogback;
import static fiftyone.ipintelligence.examples.shared.DataFileHelper.ENTERPRISE_DATA_FILE_REL_PATH;
import static fiftyone.ipintelligence.examples.shared.PropertyHelper.asStringProperty;
import static fiftyone.ipintelligence.examples.shared.PropertyHelper.asIntegerProperty;
import static fiftyone.ipintelligence.examples.shared.PropertyHelper.asFloatProperty;
import static fiftyone.ipintelligence.examples.shared.PropertyHelper.asIPAddressProperty;
import static fiftyone.ipintelligence.examples.shared.PropertyHelper.tryGet;
import static fiftyone.pipeline.util.FileFinder.getFilePath;

/**
 * This example shows how to use 51Degrees On-premise IP Intelligence to determine location and network details from IP addresses in a web application.
 * 
 * You will learn:
 * 
 * 1. How to configure a Pipeline that uses 51Degrees On-premise IP Intelligence in a web application
 * 2. How the PipelineFilter automatically processes requests and makes results available
 * 3. How to retrieve the results in your web application
 * 
 * This is the getting started Web/On-Prem example showing use of the 51Degrees
 * supplied filter which automatically creates and configures an IP Intelligence pipeline.
 * 
 * The configuration file for the pipeline is at src/main/webapp/WEB-INF/51Degrees-OnPrem.xml
 * 
 * This example requires an enterprise IP Intelligence data file (.ipi). 
 * To obtain an enterprise data file for testing, please [contact us](https://51degrees.com/contact-us).
 */
public class GettingStartedWebOnPrem extends HttpServlet {
    private static final long serialVersionUID = 1734154705981153540L;
    public static Logger logger = LoggerFactory.getLogger(GettingStartedWebOnPrem.class);

    public static void main(String[] args) throws Exception {
        configureLogback(getFilePath("logback.xml"));
        logger.info("Running Example {}", GettingStartedWebOnPrem.class);

        // Set data file location using DataFileHelper
        try {
            String dataFilePath = fiftyone.ipintelligence.examples.shared.DataFileHelper.getDataFileLocation(ENTERPRISE_DATA_FILE_REL_PATH);
            System.setProperty("TestDataFile", dataFilePath);
            logger.info("Using data file: {}", dataFilePath);
        } catch (Exception e) {
            logger.warn("Data file not found at expected location: {}", ENTERPRISE_DATA_FILE_REL_PATH);
            logger.warn("Will attempt to use default path from XML configuration");
            logger.debug("Error finding data file", e);
        }

        // start Jetty with this WebApp
        EmbedJetty.runWebApp(getResourceBase(), 8082);
    }

    public static String getResourceBase() {
        // FileFinder always finds the file when running from examples root
        java.io.File webXml = getFilePath("WEB-INF/web.xml");
        // web.xml is in WEB-INF/, so parent is webapp directory
        return webXml.getParentFile().getParent();
    }

    FlowDataProviderCore flowDataProvider = new FlowDataProviderCore.Default();

    /**
     * Process the HTTP request and generate the IP Intelligence results page
     * @param request  servlet request
     * @param response servlet response
     * @throws Exception when things go wrong
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        // Get the IP address parameter from the request for custom lookup
        String inputIpAddress = request.getParameter("client-ip");
        
        // The detection has already been carried out by the PipelineFilter
        // which is responsible for the lifecycle of the flowData - do NOT dispose
        FlowData flowData = flowDataProvider.getFlowData(request);
        
        // Determine target IP for display (fallback if evidence doesn't contain it)
        String targetIp = inputIpAddress != null && !inputIpAddress.trim().isEmpty() 
            ? inputIpAddress.trim() 
            : request.getRemoteAddr();
        
        // Get IP Intelligence data
        IPIntelligenceData ipiData = flowData.get(IPIntelligenceData.class);
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            // Load and process the HTML template
            String resourceBase = getResourceBase();
            String htmlTemplate = loadTemplate(resourceBase + "/WEB-INF/html/index.html");
            String processedHtml = substituteTemplateValues(htmlTemplate, ipiData, targetIp, flowData);
            out.println(processedHtml);
        } finally {
            if (flowData != null) {
                try {
                    flowData.close();
                } catch (Exception e) {
                    logger.warn("Error closing flow data", e);
                }
            }
        }
    }
    
    /**
     * Load HTML template from file system
     */
    private String loadTemplate(String templatePath) throws IOException {
        // FileFinder always finds the file when running from examples root
        Path path = getFilePath(templatePath).toPath();
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
    
    /**
     * Replace template variables with IP Intelligence data
     */
    private String substituteTemplateValues(String template, IPIntelligenceData ipiData, String inputIp, FlowData flowData) {
        return template
            .replace("${DATA_FILE_WARNING}", "") // Add warning logic if needed
            .replace("${INPUT_IP_ADDRESS}", inputIp != null ? inputIp : "")
            .replace("${REGISTERED_NAME}", asStringProperty(tryGet(ipiData::getRegisteredName)))
            .replace("${REGISTERED_OWNER}", asStringProperty(tryGet(ipiData::getRegisteredOwner)))
            .replace("${REGISTERED_COUNTRY}", asStringProperty(tryGet(ipiData::getRegisteredCountry)))
            .replace("${IP_RANGE_START}", asIPAddressProperty(tryGet(ipiData::getIpRangeStart)))
            .replace("${IP_RANGE_END}", asIPAddressProperty(tryGet(ipiData::getIpRangeEnd)))
            .replace("${COUNTRY}", asStringProperty(tryGet(ipiData::getCountry)))
            .replace("${COUNTRY_CODE}", asStringProperty(tryGet(ipiData::getCountryCode)))
            .replace("${COUNTRY_CODE3}", asStringProperty(tryGet(ipiData::getCountryCode3)))
            .replace("${REGION}", asStringProperty(tryGet(ipiData::getRegion)))
            .replace("${STATE}", asStringProperty(tryGet(ipiData::getState)))
            .replace("${TOWN}", asStringProperty(tryGet(ipiData::getTown)))
            .replace("${LATITUDE}", asFloatProperty(tryGet(ipiData::getLatitude)))
            .replace("${LONGITUDE}", asFloatProperty(tryGet(ipiData::getLongitude)))
            .replace("${AREAS}", asStringProperty(tryGet(ipiData::getAreas)))
            .replace("${AREAS_JS}", escapeForJs(asStringProperty(tryGet(ipiData::getAreas))))
            .replace("${TIME_ZONE_OFFSET}", asIntegerProperty(tryGet(ipiData::getTimeZoneOffset)))
            .replace("${EVIDENCE_ROWS}", buildEvidenceRows(flowData))
            .replace("${RESPONSE_HEADER_ROWS}", "") // IP Intelligence doesn't set response headers
            .replace("${LITE_DATA_WARNING}", ""); // Add warning logic if needed
    }
    
    /**
     * Build evidence table rows showing all evidence, with distinction between used vs present
     */
    private String buildEvidenceRows(FlowData flowData) {
        StringBuilder evidenceRows = new StringBuilder();
        
        // Get the IP Intelligence engine to check which evidence was actually used
        FlowElement<?, ?> engine = getIPIntelligenceEngine(flowData);
        
        // Get all evidence from FlowData
        for (Map.Entry<String, Object> evidenceEntry : flowData.getEvidence().asKeyMap().entrySet()) {
            String key = evidenceEntry.getKey();
            Object value = evidenceEntry.getValue();
            String valueStr = value != null ? value.toString() : "null";
            
            // Check if this evidence was actually used by the engine
            boolean wasUsed = engine != null && engine.getEvidenceKeyFilter().include(key);
            String cssClass = wasUsed ? "lightgreen" : "lightyellow";
            String keyDisplay = wasUsed ? "<b>" + key + "</b>" : key;
            
            evidenceRows.append(String.format(
                "<tr class=\"%s\"><td>%s</td><td>%s</td></tr>", 
                cssClass, keyDisplay, valueStr));
        }
        
        return evidenceRows.toString();
    }
    
    /**
     * Get the IP Intelligence on-premise engine from the pipeline
     */
    private FlowElement<?, ?> getIPIntelligenceEngine(FlowData flowData) {
        // Get IP Intelligence on-premise engine
        return flowData.getPipeline().getElement(
            fiftyone.ipintelligence.engine.onpremise.flowelements.IPIntelligenceOnPremiseEngine.class);
    }
    
    /**
     * Escape string for JavaScript
     */
    private String escapeForJs(String value) {
        return value != null ? value.replace("'", "\\'").replace("\"", "\\\"") : "";
    }
    

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        // any failure causes 500 error
        try {
            processRequest(request, response);
            response.setStatus(200);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }
}

