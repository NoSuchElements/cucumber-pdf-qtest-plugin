package com.nosuchelements.json;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.nosuchelements.naming.FeatureReportFileNameBuilder;
import tech.grasshopper.pojo.Feature;

/**
 * Splits a multi-feature Cucumber JSON report into individual JSON files,
 * one per feature.
 */
public class FeatureSplitter {

    private final Gson gson;
    private final FeatureReportFileNameBuilder fileNameBuilder;

    public FeatureSplitter() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.fileNameBuilder = new FeatureReportFileNameBuilder();
    }

    public List<Path> splitFeaturesToJsonFiles(List<Feature> features,
            Path outputDirectory) throws IOException {

        Files.createDirectories(outputDirectory);
        List<Path> generatedFiles = new ArrayList<>();

        for (Feature feature : features) {
            String pdfName = fileNameBuilder.buildFileName(feature);
            String jsonName = pdfName.replace(".pdf", ".json");

            Path jsonFile = outputDirectory.resolve(jsonName);
            Feature[] singleFeatureArray = new Feature[] { feature };

            try (FileWriter writer = new FileWriter(jsonFile.toFile())) {
                gson.toJson(singleFeatureArray, writer);
            }

            generatedFiles.add(jsonFile);
        }
        return generatedFiles;
    }
}
