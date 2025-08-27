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

package fiftyone.ipintelligence.examples.web;

import fiftyone.ipintelligence.shared.IPIntelligenceData;
import fiftyone.pipeline.core.configuration.PipelineOptions;
import fiftyone.pipeline.core.configuration.PipelineOptionsFactory;
import fiftyone.pipeline.core.data.FlowData;
import fiftyone.pipeline.web.Constants;
import fiftyone.pipeline.web.services.FlowDataProviderCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fiftyone.common.testhelpers.LogbackHelper.configureLogback;
import static fiftyone.ipintelligence.examples.web.HtmlContentHelper.*;
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

        // start Jetty with this WebApp
        EmbedJetty.runWebApp(getResourceBase(), 8081);
    }

    public static String getResourceBase() {
        final String basePath = "web/getting-started.onprem/src/main/webapp";
        {
            final Path path = Paths.get(basePath);
            if (Files.exists(path) && Files.isDirectory(path)) {
                return path.toString();
            }
        }
        {
            final Path path2 = Paths.get("ip-intelligence-java-examples", basePath);
            if (Files.exists(path2) && Files.isDirectory(path2)) {
                return path2.toString();
            }
        }
        return basePath;
    }

     FlowDataProviderCore flowDataProvider = new FlowDataProviderCore.Default();

    /**
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws Exception when things go wrong
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // the detection has already been carried out by the Filter
        // which is responsible to the lifecycle of the flowData - do NOT dispose
        FlowData flowData = flowDataProvider.getFlowData(request);
        // retrieve the device data from the flowdata
        IPIntelligenceData device = flowData.get(IPIntelligenceData.class);

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            doHtmlPreamble(out, "Web Integration On-Premise Example");

            // request main 51Degrees Client Side Script - this is automatically
            // served by inclusion of the PipelineFilter which intercepts the request
            // and serves dynamically generated JavaScript
            out.println("<script src=\"" + Constants.CORE_JS_NAME+ "\"></script>");

            String resourceBase = getResourceBase();

             // include description of example
            doStaticText(out, resourceBase + "/WEB-INF/html/example-description.html");

            // include a script to display the results of the client side detection
            doStaticText(out, resourceBase + "/WEB-INF/html/client-side-js-include.html");
            // find out where our data file is from the configuration
            PipelineOptions pipelineOptions =
                    PipelineOptionsFactory.getOptionsFromFile(getFilePath(resourceBase) +
                            "/WEB-INF/51Degrees-OnPrem.xml");
            doIPIntelligenceData(out, device, flowData,
                    pipelineOptions.findAndSubstitute("IPIntelligenceOnPremiseEngine", "DataFile"));

            doStaticText(out, resourceBase + "/WEB-INF/html/apple-detection.html");
            doEvidence(out, request, flowData);
            doResponseHeaders(out, response);
            doHtmlPostamble(out);
        }
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

/*!
 * @example GettingStartedWebOnPrem.java
 *
 *
 * This example is available in full on [GitHub](https://github.com/51Degrees/ip-intelligence-java-examples/blob/master/web/getting-started.onprem/src/main/java/fiftyone/ipintelligence/examples/web/GettingStartedWebOnPrem.java).
 *
 *
 * ## Overview
 *
 * The `PipelineFilter` is used to intercept requests and perform IP Intelligence. The results
 * will be stored in the HttpServletRequest object.
 * The filter will also handle setting response headers (e.g. Accept-CH for User-Agent
 * Client Hints) and serving requests for client-side JavaScript and JSON resources.
 *
 * The results of detection can be accessed by using a FlowDataProvider which
 * is responsible for managing the lifecycle of the flowData - do NOT dispose
 * ```{java}
 * FlowData flowData = flowDataProvider.getFlowData(request);
 * IPIntelligenceData device = flowData.get(IPIntelligenceData.class);
 * ...
 * ```
 *
 * Results can also be accessed in client-side code by using the `fod` object. Note that the global
 * object name can be changed by using the setObjectName option on the
 * [JavaScriptBuilderElementBuilder](//51degrees.com/pipeline-java/classfiftyone_1_1pipeline_1_1javascriptbuilder_1_1flowelements_1_1_java_script_builder_element_builder.html)
 *
 * ```{java}
 * window.onload = function () {
 *     fod.complete(function(data) {
 *         var hardwareName = data.device.hardwarename;
 *         alert(hardwareName.join(", "));
 *     }
 * }
 * ```
 *
 */
