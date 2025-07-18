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

import fiftyone.ipintelligence.engine.onpremise.flowelements.IPIntelligenceOnPremiseEngine;
import fiftyone.ipintelligence.shared.IPIntelligenceData;
import fiftyone.pipeline.cloudrequestengine.flowelements.CloudRequestEngine;
import fiftyone.pipeline.core.data.FlowData;
import fiftyone.pipeline.core.flowelements.FlowElement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static fiftyone.ipintelligence.examples.shared.PropertyHelper.asString;
import static fiftyone.ipintelligence.examples.shared.PropertyHelper.tryGet;
import static fiftyone.pipeline.util.FileFinder.getFilePath;

public class HtmlContentHelper {
    /**
     * Helper to output HTML headers
     * @param out the PrintWriter to write to
     * @param title title of the page
     */
    public static void doHtmlPreamble(PrintWriter out, String title) {
        // language=html
        out.append("<!DOCTYPE html>" +
                "<html lang='en'>\n" +
                "<head>\n"+
                "<meta charset=\"UTF-8\">\n" +
                "<title>" + title + "</title>\n" +
                "<style> body {margin: 2em; font-family: sans-serif;}"+
                "table {font-size: smaller; background-color: lightblue} " +
                "tr {background-color: lightyellow} "+
                "td {padding: 5px} "+
                ".lightred {background-color: lightpink}"+
                ".lightyellow {background-color: lightyellow}"+
                ".lightgreen {background-color: lightgreen}\n" +
                ".smaller {font-size: smaller}</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>51Degrees IP Intelligence Example ("+ title +")</h1>\n" +
                "\n");
    }


    public static void doResponseHeaders(PrintWriter out, HttpServletResponse response) {
        // language=html
        out.append("<div id=\"response\">\n" +
                "<h2>Response Headers</h2>\n" +
                "<p>The following response headers were set:</p>\n" +
                "<table>");
        for (String headerName: response.getHeaderNames()) {
            out.append("<tr><td>")
                    .append(headerName)
                    .append("</td><td>")
                    .append(response.getHeader(headerName))
                    .append("</td></tr>\n");
        }
        out.append("</table></div>");

        if (response.getHeaderNames().contains("Accept-CH") == false) {
            out.append(
         "<div class=\"example-alert\">WARNING: There is no Accept-CH header in the response. " +
                 "This may indicate that your browser does not support User-Agent Client Hints. " +
                 "This is not necessarily a problem, but if you are wanting to try out detection " +
                 "using User-Agent Client Hints, then make sure that your browser "+
                 "<a href=\"https://developer.mozilla" +
                 ".org/en-US/docs/Web/API/User-Agent_Client_Hints_API#browser_compatibility" +
                 "\">supports them</a>.</div>");
        }

    }

    public static void doEvidence(PrintWriter out, HttpServletRequest request, FlowData flowData) {
        FlowElement<?, ?> engine = getFlowElement(flowData);
        // set up the table
        out.println(
                "  <div id=\"evidence\">\n" +
                        "  <h2>Evidence Used </h2>\n");

        doUachInfo(out);

        out.println("<table>\n");
        // list by other evidence entries
        for (Map.Entry<String, Object> evidence : flowData.getEvidence().asKeyMap().entrySet()) {
            if (engine.getEvidenceKeyFilter().include(evidence.getKey())) {
                out.println("<tr>");
                out.println("<td>" + evidence.getKey() + "</td><td>" + evidence.getValue() + "</td></tr>");
            }
        }
        out.println("</table>\n" +
                "</div>\n");
    }

    private static FlowElement<?, ?> getFlowElement(FlowData flowData) {
        // we assume a CloudRequest or a HashEngine ... but not both
        FlowElement<?,?> engine = flowData.getPipeline().getElement(CloudRequestEngine.class);
        if (Objects.isNull(engine)) {
            engine = flowData.getPipeline().getElement(IPIntelligenceOnPremiseEngine.class);
            if (Objects.isNull(engine)) {
                throw new IllegalStateException("No IP Intelligence engine found");
            }
        }
        return engine;
    }

    public static void doIPIntelligenceData(PrintWriter out, IPIntelligenceData ipData, FlowData flowData,
                                    String dataFileLocation) {
        FlowElement<?, ?> engine = getFlowElement(flowData);
        String content = "";
        if (engine instanceof CloudRequestEngine) {
            content = "<p>The following values were detected using the <strong>cloud " +
                    "IP Intelligence engine</strong>.</p>";
        } else {
            // Lite or Enterprise
            String dataTier = ((IPIntelligenceOnPremiseEngine)engine).getDataSourceTier();
            // date of creation
            Date fileDate = ((IPIntelligenceOnPremiseEngine)engine).getDataFilePublishedDate();

            long daysOld = ChronoUnit.DAYS.between(fileDate.toInstant(), Instant.now());
            String displayDate = new SimpleDateFormat("yyyy-MM-dd").format(fileDate);

            content = String.format("<p>The following values detected using the " +
                    "<strong>on-premise IP Intelligence engine</strong> using a '%s' data file, " +
                    "created %s, %d days ago, from location '%s'.</p>",
                    dataTier, displayDate, daysOld, dataFileLocation);
            if (daysOld > 28) {
                content += String.format(
                        "<p>The data file is more than %d days old. A more recent data file " +
                                " may be needed to correctly detect the latest devices, " +
                                "browsers, etc.</p>", daysOld);
            }
        }
        // language=html
        out.append(
                "<h2>Device Data</h2>\n" +
                        "<div id=\"content\">\n" +
                        content +
                        "    <table>\n" +
                        "        <tr><td>Registered Name</td><td>" + asString(tryGet(ipData::getRegisteredName)) + "</td></tr>\n" +
                        "        <tr><td>Registered Owner</td><td>" + asString(tryGet(ipData::getRegisteredOwner)) + "</td></tr>\n" +
                        "        <tr><td>Registered Country</td><td>" + asString(tryGet(ipData::getRegisteredCountry)) + "</td></tr>\n" +
                        "    </table>\n" +
                        "</div>\n");

        doLiteRubric(out, flowData);
    }

    public static void doUachInfo(PrintWriter out) {
        //language=html
        out.append(

                "    <p>" +
                        "    A browser that supports client hints sends <code>Sec-CH-UA</code>, <code>Sec-CH-UA-Platform</code>"+
                        "    and <code>Sec-CH-UA-Mobile</code> HTTP headers along with the <code>User-Agent</code> header." +
                        "    <p>\n" +
                        "    If the server determines that the browser supports client hints, then" +
                        "    it may request additional client hints headers by setting the" +
                        "    <code>Accept-CH</code> header in the response." +
                        "    <p>\n" +
                        "    Refresh the page to send another request to the server. This time, some" +
                        "    additional client hints headers that have been requested by the server" +
                        "    may be included. The browser remembers the server's request to " +
                        "    add those headers and adds those additional headers on each subsequent request" +
                        "    (which is why this example works best in a \"private\" window if you launch the test" +
                        "    more than once).<p>" +
                        "    \n");
    }

    /**
     * Helper to output text about missing values and what to expect from the Lite file
     * @param out the PrintWriter to write to
     * @param flowData the flowdata to use to determine whether the message needs to be output
     */
    public static void doLiteRubric(PrintWriter out, FlowData flowData) {
        IPIntelligenceOnPremiseEngine ddhe = flowData.getPipeline().getElement(IPIntelligenceOnPremiseEngine.class);
        if (Objects.isNull(ddhe)) {
            return;
        }
        if (ddhe.getDataSourceTier().equals("Lite") == false) {
            return;
        }
        // language=html
        out.append("<div><h2>Lite Data File</h2>" +
                "<p><em><strong>Some values may be unavailable</strong></em> because " +
                "you are using a Lite data file included with this source distribution.\n" +
                "<p>The example requires an Enterprise data file to work fully. " +
                "You can get the Enterprise data file " +
                "<a href='https://51degrees.com/pricing'>here</a></div>\n");
    }

    public static void doHtmlPostamble(PrintWriter out) {
        // language=html
        out.append("\n</body></html>\n");
    }


    static void doStaticText(PrintWriter out, String location) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(
                getFilePath(location)))) {
            br.lines().forEach(out::println);
        }
    }
}
