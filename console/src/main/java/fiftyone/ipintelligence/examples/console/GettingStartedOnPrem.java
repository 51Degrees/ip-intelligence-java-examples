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

import fiftyone.ipintelligence.examples.shared.DataFileHelper;
import fiftyone.ipintelligence.examples.shared.EvidenceHelper;
import fiftyone.ipintelligence.engine.onpremise.flowelements.IPIntelligenceOnPremiseEngine;
import fiftyone.ipintelligence.shared.IPIntelligenceData;
import fiftyone.pipeline.core.configuration.PipelineOptions;
import fiftyone.pipeline.core.configuration.PipelineOptionsFactory;
import fiftyone.pipeline.core.data.FlowData;
import fiftyone.pipeline.core.data.IWeightedValue;
import fiftyone.pipeline.core.flowelements.Pipeline;
import fiftyone.pipeline.engines.data.AspectPropertyValue;
import fiftyone.pipeline.engines.fiftyone.flowelements.FiftyOnePipelineBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static fiftyone.common.testhelpers.LogbackHelper.configureLogback;
import static fiftyone.ipintelligence.examples.shared.PropertyHelper.asString;
import static fiftyone.pipeline.util.FileFinder.getFilePath;

/**
 * Provides an illustration of the fundamental elements of carrying out IP Intelligence using
 * "on premise" detection - meaning the IP Intelligence data is stored on your server
 * and the detection software executes exclusively on your server.
 * <p>
 * This example shows how to use pipeline configuration file, as opposed to the fluent builder
 * illustrated in {@link GettingStartedCloud}. The configuration file is
 * <code>src/main/resources/gettingStartedOnPrem.xml</code>.
 * <p>
 * The concepts of "pipeline", "flow data", "evidence" and "results" are illustrated.
 */
public class GettingStartedOnPrem {
    private static final Logger logger = LoggerFactory.getLogger(GettingStartedOnPrem.class);

    /* In this example, by default, the 51degrees "Lite" file needs to be somewhere in the project
    space, or you may specify another file as a command line parameter.

    Note that the Lite data file is only used for illustration, and has limited accuracy and
    capabilities. Find out about the Enterprise data file here: https://51degrees.com/pricing */
    public static String LITE_V_4_1_HASH = "51Degrees-LiteV41.ipi";
    public static String ENTERPRISE_HASH = "51Degrees-EnterpriseIpiV41.ipi";

    public static void main(String[] args) throws Exception {
        configureLogback(getFilePath("logback.xml"));

        //for best IP Intelligence results use the ENTERPRISE_HASH data file
        String dataFile = args.length > 0 ? args[0] : LITE_V_4_1_HASH;
        // prepare 'evidence' for use in pipeline (see below)
        List<Map<String, String>> evidence = EvidenceHelper.setUpEvidence();
        run(dataFile, evidence, System.out);
    }

    /**
     * Run the example
     * @param dataFile a IP Intelligence data file
     * @param evidenceList a List&lt;Map&lt;String, String>> representing evidence
     * @param outputStream somewhere for the results
     */
    public static void run(String dataFile,
                           List<Map<String, String>> evidenceList,
                           OutputStream outputStream) throws Exception {
        logger.info("Running GettingStarted example");
        try {
            String dataFileLocation = getFilePath(dataFile).getAbsolutePath();
            // the location of the test data file is interpolated in the pipeline
            // configuration file
            System.setProperty("TestDataFile", dataFileLocation);
        } catch (Exception e) {
            DataFileHelper.cantFindDataFile(dataFile);
            throw e;
        }

        /* In this example, we use the IPIntelligencePipelineBuilder and configure it from
        options contained in the file "gettingStartedOnPrem.xml".

        For more information about pipelines in general see the documentation at
        http://51degrees.com/documentation/_concepts__configuration__builders__index.html

        Note that we wrap the creation of a pipeline in a try/resources to control its lifecycle */
        // the configuration file is in the resources directory
        File optionsFile = getFilePath("gettingStartedOnPrem.xml");
        // load the options and if no resource key has been set in the file
        // use the one supplied to this method
        PipelineOptions pipelineOptions = PipelineOptionsFactory.getOptionsFromFile(optionsFile);
        // Build a new Pipeline from the configuration.
        try (Pipeline pipeline = new FiftyOnePipelineBuilder()
                .buildFromConfiguration(pipelineOptions)) {


            // carry out some sample detections
            for (Map<String, String> evidence : evidenceList) {
                analyzeEvidence(evidence, pipeline, outputStream);
            }

            /* Get the 'engine' element within the pipeline that performs IP Intelligence. We
            can use this to get details about the data file as well as meta-data describing
            things such as the available properties. */
            IPIntelligenceOnPremiseEngine engine = pipeline.getElement(IPIntelligenceOnPremiseEngine.class);
            DataFileHelper.logDataFileInfo(engine);

            logger.info("All done");
        }
    }

    /**
     * Taking a map of evidence as a parameter, process it in the pipeline
     * supplied and output the IP Intelligence results to the output stream.
     * @param evidence a map representing HTTP headers
     * @param pipeline a pipeline set up to process the evidence
     * @param out somewhere to send the detection results
     */
    private static void analyzeEvidence(Map<String, String> evidence,
                                        Pipeline pipeline,
                                        OutputStream out) throws Exception {
        PrintWriter writer = new PrintWriter(out);
        /* FlowData is a data structure that is used to convey information required for detection
        and the results of the detection through the pipeline. Information required for
        detection is called "evidence" and usually consists of a number of HTTP Header field
        values, in this case represented by a Map<String, String> of header name/value entries.

        FlowData is wrapped in a try/resources block in order to ensure that the unmanaged
        resources allocated by the native IP Intelligence library are freed */
        try (FlowData data = pipeline.createFlowData()) {

            // list the evidence
            writer.println("Input values:");
            for (Map.Entry<String, String> entry : evidence.entrySet()) {
                writer.format("\t%s: %s\n", entry.getKey(), entry.getValue());
            }

            // Add the evidence values to the flow data
            data.addEvidence(evidence);

            // Process the flow data.
            data.process();

            writer.println("Results:");
            /* Now that it has been processed, the flow data will have been populated with the result.

            In this case, we want information about the device, which we can get by asking for a
            result matching the "IPIntelligenceData" interface. */
            IPIntelligenceData device = data.get(IPIntelligenceData.class);

            /* Display the results of the detection, which are called device properties. See the
            property dictionary at https://51degrees.com/developers/property-dictionary for
            details of all available properties. */
            AspectPropertyValue<List<IWeightedValue<String>>> value = device.getRegisteredName();
            if (value != null && value.hasValue()) {
                for (IWeightedValue<?> weightedValue : value.getValue()) {
                    writer.println("\tRegisteredName: " + weightedValue.getValue() + "; Weighting: " + weightedValue.getWeighting());
                }
            }
            value = device.getRegisteredCountry();
            if (value != null && value.hasValue()) {
                for (IWeightedValue<?> weightedValue : value.getValue()) {
                    writer.println("\tRegisteredCountry: " + weightedValue.getValue() + "; Weighting: " + weightedValue.getWeighting());
                }
            }
            value = device.getRegisteredCountry();
            if (value != null && value.hasValue()) {
                for (IWeightedValue<?> weightedValue : value.getValue()) {
                    writer.println("\tRegisteredCountry: " + weightedValue.getValue() + "; Weighting: " + weightedValue.getWeighting());
                }
            }
        }
        writer.println();
        writer.flush();
    }
}
/*!
 * @example console/GettingStartedOnPrem.java
 * @include{doc} example-getting-started-onpremise.txt
 * <p>
 * This example is available in full on [GitHub](https://github.com/51Degrees/ip-intelligence-java-examples/blob/master/console/src/main/java/fiftyone/devicedetection/examples/console/GettingStartedOnPrem.java).
 * @include{doc} example-require-datafile.txt
 */
