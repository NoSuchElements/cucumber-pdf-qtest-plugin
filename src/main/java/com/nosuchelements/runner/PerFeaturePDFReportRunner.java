package com.nosuchelements.runner;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.nosuchelements.naming.FeatureReportFileNameBuilder;
import tech.grasshopper.data.PDFCucumberReportDataGenerator;
import tech.grasshopper.pdf.PDFCucumberReport;
import tech.grasshopper.pdf.config.ParameterConfig;
import tech.grasshopper.pdf.data.ReportData;
import tech.grasshopper.pdf.section.details.executable.MediaCleanup.CleanupType;
import tech.grasshopper.pdf.section.details.executable.MediaCleanup.MediaCleanupOption;
import tech.grasshopper.pojo.Feature;

/**
 * Standalone runner to generate per-feature PDF reports from
 * a Cucumber JSON report file.
 *
 * Usage:
 *   java -cp ... com.nosuchelements.runner.PerFeaturePDFReportRunner \
 *        path/to/cucumber-json.json \
 *        output/directory
 */
public class PerFeaturePDFReportRunner {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println(
                    "Usage: PerFeaturePDFReportRunner <json-file> [output-dir]");
            System.exit(1);
        }

        String jsonFilePath = args[0];
        String outputDir = args.length > 1 ? args[1] : "report/per-feature";

        Path outputPath = Paths.get(outputDir);
        Files.createDirectories(outputPath);

        Gson gson = new GsonBuilder().create();
        Feature[] features;

        try (FileReader reader = new FileReader(jsonFilePath)) {
            features = gson.fromJson(reader, Feature[].class);
        }

        if (features == null || features.length == 0) {
            System.err.println("No features found in the JSON report.");
            System.exit(1);
        }

        FeatureReportFileNameBuilder nameBuilder =
                new FeatureReportFileNameBuilder();

        int count = 0;
        for (Feature feature : features) {
            String pdfFileName = nameBuilder.buildFileName(feature);

            List<Feature> singleFeature = new ArrayList<>();
            singleFeature.add(feature);

            PDFCucumberReportDataGenerator generator =
                    new PDFCucumberReportDataGenerator();
            ReportData reportData = generator.generateReportData(singleFeature);

            ParameterConfig paramConfig = ParameterConfig.builder()
                    .title(feature.getName())
                    .build();

            PDFCucumberReport pdfReport = new PDFCucumberReport(
                    reportData, outputDir,
                    MediaCleanupOption.builder()
                            .cleanUpType(CleanupType.ALL).build());
            pdfReport.setParameterConfig(paramConfig);
            pdfReport.createReport();

            count++;
            System.out.printf("Generated: %s/%s%n", outputDir, pdfFileName);
        }

        System.out.printf("%nDone. Generated %d PDF reports.%n", count);
    }
}
