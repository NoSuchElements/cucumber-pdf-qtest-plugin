package com.nosuchelements;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.nosuchelements.naming.FeatureReportFileNameBuilder;
import tech.grasshopper.data.PDFCucumberReportDataGenerator;
import tech.grasshopper.json.JsonFileConverter;
import tech.grasshopper.json.JsonPathCollector;
import tech.grasshopper.logging.CucumberPDFReportLogger;
import tech.grasshopper.pdf.PDFCucumberReport;
import tech.grasshopper.pdf.config.ParameterConfig;
import tech.grasshopper.pdf.data.ReportData;
import tech.grasshopper.pdf.section.details.executable.MediaCleanup.CleanupType;
import tech.grasshopper.pdf.section.details.executable.MediaCleanup.MediaCleanupOption;
import tech.grasshopper.pojo.Feature;
import tech.grasshopper.processor.HierarchyProcessor;
import tech.grasshopper.properties.ReportProperties;

@Mojo(name = "pdfreportperfeature")
public class CucumberPDFQTestPlugin extends AbstractMojo {

    @Parameter(property = "pdfreport.cucumberJsonReportDirectory", required = true)
    private String cucumberJsonReportDirectory;

    @Parameter(property = "pdfreport.cucumberPdfReportDirectory", defaultValue = ReportProperties.REPORT_DIRECTORY)
    private String cucumberPdfReportDirectory;

    @Parameter(property = "pdfreport.reportDirectoryTimeStamp", defaultValue = ReportProperties.REPORT_DIRECTORY_TIMESTAMP)
    private String cucumberPdfReportDirectoryTimeStamp;

    @Parameter(property = "pdfreport.strictCucumber6Behavior", defaultValue = "true")
    private String strictCucumber6Behavior;

    @Parameter(property = "pdfreport.title")
    private String title;

    @Parameter(property = "pdfreport.titleColor")
    private String titleColor;

    @Parameter(property = "pdfreport.passColor")
    private String passColor;

    @Parameter(property = "pdfreport.failColor")
    private String failColor;

    @Parameter(property = "pdfreport.skipColor")
    private String skipColor;

    @Parameter(property = "pdfreport.displayFeature")
    private String displayFeature;

    @Parameter(property = "pdfreport.displayScenario")
    private String displayScenario;

    @Parameter(property = "pdfreport.displayDetailed")
    private String displayDetailed;

    @Parameter(property = "pdfreport.displayExpanded")
    private String displayExpanded;

    @Parameter(property = "pdfreport.displayAttached")
    private String displayAttached;

    @Parameter(property = "pdfreport.skipHooks")
    private String skipHooks;

    @Parameter(property = "pdfreport.skipScenarioHooks")
    private String skipScenarioHooks;

    @Parameter(property = "pdfreport.skipStepHooks")
    private String skipStepHooks;

    @Parameter(property = "pdfreport.qtestTagPrefix", defaultValue = "@QTEST_TC_")
    private String qtestTagPrefix;

    private JsonPathCollector jsonPathCollector;
    private JsonFileConverter jsonFileConverter;
    private ReportProperties reportProperties;
    private HierarchyProcessor hierarchyProcessor;
    private CucumberPDFReportLogger logger;

    @Inject
    public CucumberPDFQTestPlugin(JsonPathCollector jsonPathCollector,
            JsonFileConverter jsonFileConverter, ReportProperties reportProperties,
            HierarchyProcessor hierarchyProcessor, CucumberPDFReportLogger logger) {
        this.jsonPathCollector = jsonPathCollector;
        this.jsonFileConverter = jsonFileConverter;
        this.reportProperties = reportProperties;
        this.hierarchyProcessor = hierarchyProcessor;
        this.logger = logger;
    }

    public void execute() {
        try {
            logger.initializeLogger(getLog());
            logger.info("STARTED CUCUMBER PDF PER-FEATURE REPORT GENERATION PLUGIN");

            reportProperties.setStrictCucumber6Behavior(strictCucumber6Behavior);
            reportProperties.setReportDirectory(cucumberPdfReportDirectory,
                    cucumberPdfReportDirectoryTimeStamp);

            List<Path> jsonFilePaths = jsonPathCollector
                    .retrieveFilePaths(cucumberJsonReportDirectory);
            List<Feature> allFeatures = jsonFileConverter
                    .retrieveFeaturesFromReport(jsonFilePaths);
            hierarchyProcessor.process(allFeatures);

            FeatureReportFileNameBuilder nameBuilder =
                    new FeatureReportFileNameBuilder(qtestTagPrefix);

            int successCount = 0;
            int failureCount = 0;
            
            for (Feature feature : allFeatures) {
                String pdfFileName = nameBuilder.buildFileName(feature);
                
                try {
                    // Create File object with custom name for this feature
                    File reportFile = new File(reportProperties.getReportDirectory(), pdfFileName);

                    List<Feature> singleFeatureList = new ArrayList<>();
                    singleFeatureList.add(feature);

                    PDFCucumberReportDataGenerator generator =
                            new PDFCucumberReportDataGenerator();
                    ReportData reportData = generator.generateReportData(singleFeatureList);

                    String featureTitle = (title != null ? title + " - " : "")
                            + feature.getName();

                    ParameterConfig featureParamConfig = ParameterConfig.builder()
                            .title(featureTitle).titleColor(titleColor)
                            .passColor(passColor).failColor(failColor).skipColor(skipColor)
                            .displayFeature(displayFeature).displayScenario(displayScenario)
                            .displayDetailed(displayDetailed).displayExpanded(displayExpanded)
                            .displayAttached(displayAttached).skipHooks(skipHooks)
                            .skipScenarioHooks(skipScenarioHooks)
                            .skipStepHooks(skipStepHooks).build();

                    // Use File-based constructor for proper per-feature naming
                    PDFCucumberReport pdfReport = new PDFCucumberReport(reportData,
                            reportFile,
                            MediaCleanupOption.builder()
                                    .cleanUpType(CleanupType.ALL).build());
                    pdfReport.setParameterConfig(featureParamConfig);
                    pdfReport.createReport();

                    successCount++;
                    logger.info(String.format("Generated PDF report: %s", pdfFileName));
                    
                } catch (Throwable t) {
                    // Log individual feature failure but continue processing others
                    failureCount++;
                    logger.error(String.format(
                            "Failed to generate PDF for feature '%s' (file: %s): %s",
                            feature.getName(), pdfFileName, t.getMessage()));
                    t.printStackTrace();
                }
            }

            logger.info(String.format(
                    "FINISHED - Generated %d PDF reports successfully, %d failed",
                    successCount, failureCount));
                    
            if (failureCount > 0) {
                logger.error(String.format(
                        "WARNING: %d feature(s) failed to generate PDF reports",
                        failureCount));
            }
            
        } catch (Throwable t) {
            t.printStackTrace();
            logger.error(String.format(
                    "STOPPING CUCUMBER PDF PER-FEATURE REPORT GENERATION - %s",
                    t.getMessage()));
        }
    }
}
