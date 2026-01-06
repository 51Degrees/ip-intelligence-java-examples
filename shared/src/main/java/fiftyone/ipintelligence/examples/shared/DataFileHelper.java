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

import fiftyone.ipintelligence.engine.onpremise.flowelements.IPIntelligenceOnPremiseEngine;
import fiftyone.ipintelligence.engine.onpremise.flowelements.IPIntelligenceOnPremiseEngineBuilder;
import fiftyone.ipintelligence.shared.testhelpers.FileUtils;
import fiftyone.pipeline.engines.data.AspectEngineDataFile;
import fiftyone.pipeline.engines.fiftyone.data.FiftyOneDataFile;
import fiftyone.pipeline.util.FileFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

import static fiftyone.pipeline.util.FileFinder.getFilePath;

public class DataFileHelper {
    static Logger logger = LoggerFactory.getLogger(DataFileHelper.class);

    /**
     * Relative path to the enterprise IP Intelligence data file from the examples repository root.
     * This path is used by both console and web examples for testing.
     * The data file is downloaded to this location by CI scripts.
     */
    public static final String ENTERPRISE_DATA_FILE_REL_PATH = "ip-intelligence-data/51Degrees-EnterpriseIpiV41.ipi";

public static class DatafileInfo {
        FiftyOneDataFile fileInfo;
        String tier;

        public DatafileInfo(FiftyOneDataFile fileInfo, String dataSourceTier) {
            this.fileInfo = fileInfo;
            this.tier = dataSourceTier;
        }

        public AspectEngineDataFile getFileInfo(){
            return fileInfo;
        }
        public String getTier() {
            return tier;
        }
    }
    public static DatafileInfo getDatafileMetaData(String dataFileLocation) throws Exception {
        try(IPIntelligenceOnPremiseEngine ddhe = new IPIntelligenceOnPremiseEngineBuilder()
                .setAutoUpdate(false)
                .build(dataFileLocation,false)) {
            return new DatafileInfo((FiftyOneDataFile) ddhe.getDataFileMetaData(), ddhe.getDataSourceTier());
        }
    }

    public static void logDataFileInfo(IPIntelligenceOnPremiseEngine engine) {
        // Lite or Enterprise
        String dataTier = engine.getDataSourceTier();
        // date of creation
        Date fileDate = engine.getDataFilePublishedDate();
        String dataFileLocation = engine.getDataFileMetaData().getDataFilePath();

        long daysOld = ChronoUnit.DAYS.between(fileDate.toInstant(), Instant.now());
        String displayDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(fileDate);
        logger.info("Used a '{}' data file, created {}, {} days ago, from location '{}'",
                dataTier, displayDate, daysOld, dataFileLocation);
        if (dataTier.equals("Lite")) {
            logger.warn("This example is using the 'Lite ' data file. " +
                    "This is used for illustration, and has limited " +
                    "accuracy and capabilities. Find out about the " +
                    "Enterprise data file on our pricing page: " +
                    "https://51degrees.com/pricing");
        }
        if (daysOld > 28) {
            logger.warn("This example is using a data file that is more " +
                    "than {} days old. A more recent data file " +
                    "may be needed for correct results", daysOld);
        }
    }

    public static void cantFindDataFile(String dataFile) {
        logger.error("Could not find the data file '{}' which must be " +
                "somewhere in the project space to be found.", dataFile);
    }

    /**
     * Tries to find the passed file, or if null a default file
     * @param evidenceFilename a filename to find
     * @return a File object
     * @throws Exception if the file was not found
     */

    @SuppressWarnings("RedundantThrows")
    public static File getEvidenceFile(String evidenceFilename) throws Exception {
        if (Objects.isNull(evidenceFilename)) {
            evidenceFilename = FileUtils.EVIDENCE_FILE_NAME;
        }

        File evidenceFile;
        try {
            evidenceFile = FileFinder.getFilePath(evidenceFilename);
        } catch (Exception e) {
            logger.error("Could not find evidence file {}", evidenceFilename);
            throw e;
        }
        return evidenceFile;
    }

    /**
     * Tries to find the passed file, or if null a default file. Handles both absolute and relative paths.
     * @param dataFilename a filename to find (can be absolute or relative path)
     * @return a full pathname
     * @throws Exception if the file was not found
     */
    @SuppressWarnings("RedundantThrows")
    public static String getDataFileLocation(String dataFilename) throws Exception {
        if (Objects.isNull(dataFilename)) {
            dataFilename = ENTERPRISE_DATA_FILE_REL_PATH;
        }
        
        // Check if it's an absolute path
        Path dataPath = Paths.get(dataFilename);
        if (dataPath.isAbsolute()) {
            // It's an absolute path, check if file exists
            if (Files.exists(dataPath)) {
                return dataPath.toString();
            } else {
                throw new IOException("Data file not found at absolute path: " + dataFilename);
            }
        } else {
            // It's a relative path, try FileFinder first (searches project directories)
            try {
                return getFilePath(dataFilename).getAbsolutePath();
            } catch (Exception e) {
                // If FileFinder fails, try as relative to current working directory
                Path relativePath = Paths.get(System.getProperty("user.dir"), dataFilename);
                if (Files.exists(relativePath)) {
                    return relativePath.toString();
                } else {
                    // Try just the relative path as-is
                    if (Files.exists(dataPath)) {
                        return dataPath.toAbsolutePath().toString();
                    }
                    DataFileHelper.cantFindDataFile(dataFilename);
                    throw new IOException("Data file not found at relative path: " + dataFilename + 
                                        ". Searched in project directories and current working directory.");
                }
            }
        }
    }
}
