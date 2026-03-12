package com.nosuchelements.naming;

import java.util.List;

import tech.grasshopper.pojo.Feature;
import tech.grasshopper.pojo.Tag;

/**
 * Builds PDF file names per feature file.
 *
 * Naming convention:
 *   {feature-file-base-name}@{QTEST_TC_XXXX}.pdf
 *
 * If no @QTEST_TC_ tag is found, falls back to:
 *   {feature-file-base-name}.pdf
 */
public class FeatureReportFileNameBuilder {

    private static final String QTEST_TAG_PREFIX = "@QTEST_TC_";
    private static final String PDF_EXTENSION = ".pdf";
    private static final String FEATURE_EXTENSION = ".feature";

    private final String qtestTagPrefix;

    public FeatureReportFileNameBuilder() {
        this(QTEST_TAG_PREFIX);
    }

    public FeatureReportFileNameBuilder(String qtestTagPrefix) {
        this.qtestTagPrefix = qtestTagPrefix;
    }

    /**
     * Build the PDF file name for a given feature.
     *
     * @param feature the Cucumber Feature object
     * @return the PDF file name (e.g., "datatable-docstring@QTEST_TC_1201.pdf")
     */
    public String buildFileName(Feature feature) {
        String baseName = extractBaseNameFromUri(feature.getUri());
        String qtestId = findQTestTag(feature.getTags());

        if (qtestId != null) {
            return baseName + "@" + qtestId + PDF_EXTENSION;
        }
        return baseName + PDF_EXTENSION;
    }

    /**
     * Extract the base file name from the feature URI.
     * Example: "file:src/test/resources/stepdefs/datatable-docstring.feature"
     *          -> "datatable-docstring"
     */
    private String extractBaseNameFromUri(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "unknown-feature";
        }

        String path = uri;
        if (path.startsWith("file:")) {
            path = path.substring(5);
        }

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0) {
            path = path.substring(lastSlash + 1);
        }
        int lastBackslash = path.lastIndexOf('\\');
        if (lastBackslash >= 0) {
            path = path.substring(lastBackslash + 1);
        }

        if (path.endsWith(FEATURE_EXTENSION)) {
            path = path.substring(0, path.length() - FEATURE_EXTENSION.length());
        }

        return sanitize(path);
    }

    /**
     * Find the @QTEST_TC_ tag from the feature's tag list.
     * Returns the tag value without the leading '@'.
     */
    private String findQTestTag(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        for (Tag tag : tags) {
            String name = tag.getName();
            if (name != null && name.startsWith(qtestTagPrefix)) {
                return name.substring(1);
            }
        }
        return null;
    }

    /**
     * Sanitize the file name to remove invalid characters.
     */
    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9._&-]", "_");
    }
}
