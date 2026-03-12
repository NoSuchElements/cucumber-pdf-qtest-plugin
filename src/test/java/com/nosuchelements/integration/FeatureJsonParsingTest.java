package com.nosuchelements.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.nosuchelements.naming.FeatureReportFileNameBuilder;
import tech.grasshopper.pojo.Feature;

/**
 * Integration test that validates JSON parsing and per-feature
 * file name generation using the actual cucumber-json.json test data.
 */
public class FeatureJsonParsingTest {

    private Feature[] features;
    private FeatureReportFileNameBuilder nameBuilder;

    @Before
    public void setUp() {
        Gson gson = new GsonBuilder().create();
        Reader reader = new InputStreamReader(
                getClass().getClassLoader()
                        .getResourceAsStream("cucumber-json.json"));
        features = gson.fromJson(reader, Feature[].class);
        nameBuilder = new FeatureReportFileNameBuilder();
    }

    @Test
    public void testJsonParsesAllFeatures() {
        assertNotNull("Features should not be null", features);
        assertEquals("Should parse 9 features", 9, features.length);
    }

    @Test
    public void testAllFeatureNamesPresent() {
        List<String> expectedNames = Arrays.asList(
                "DataTable And DocString",
                "Exception",
                "Hook and step failures",
                "No tag feature",
                "Scenario And Scenario Outline",
                "Screenshots",
                "Scenarios With No Step Definitions",
                "Two Screenshots");

        for (String expected : expectedNames) {
            boolean found = false;
            for (Feature f : features) {
                if (f.getName().contains(expected) || expected.contains(f.getName())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Feature '" + expected + "' should be found", found);
        }
    }

    @Test
    public void testPerFeatureFileNameGeneration() {
        Map<String, String> expectedMappings = new HashMap<>();
        expectedMappings.put("datatable-docstring", "datatable-docstring@QTEST_TC_1201.pdf");
        expectedMappings.put("exceptions", "exceptions@QTEST_TC_1202.pdf");
        expectedMappings.put("failure", "failure@QTEST_TC_1203.pdf");
        expectedMappings.put("lengthynames", "lengthynames@QTEST_TC_1204.pdf");
        expectedMappings.put("notags", "notags.pdf");
        expectedMappings.put("screenshots", "screenshots@QTEST_TC_1206.pdf");
        expectedMappings.put("skipdef", "skipdef@QTEST_TC_1207.pdf");
        expectedMappings.put("twoimages", "twoimages@QTEST_TC_1208.pdf");

        for (Feature feature : features) {
            String fileName = nameBuilder.buildFileName(feature);
            assertNotNull("File name should not be null for: " + feature.getName(),
                    fileName);
            assertTrue("File name should end with .pdf: " + fileName,
                    fileName.endsWith(".pdf"));

            if (feature.getTags() != null) {
                boolean hasQTest = feature.getTags().stream()
                        .anyMatch(t -> t.getName().startsWith("@QTEST_TC_"));
                if (hasQTest) {
                    assertTrue("File name should contain QTEST_TC_: " + fileName,
                            fileName.contains("QTEST_TC_"));
                }
            }
        }
    }

    @Test
    public void testEachFeatureHasUri() {
        for (Feature feature : features) {
            assertNotNull("Feature URI should not be null for: " + feature.getName(),
                    feature.getUri());
            assertTrue("Feature URI should contain .feature: " + feature.getUri(),
                    feature.getUri().contains(".feature"));
        }
    }

    @Test
    public void testEachFeatureHasElements() {
        for (Feature feature : features) {
            assertNotNull("Feature elements should not be null for: " + feature.getName(),
                    feature.getElements());
            assertTrue("Feature should have at least one element: " + feature.getName(),
                    feature.getElements().size() > 0);
        }
    }

    @Test
    public void testQTestTagCount() {
        int qtestTaggedFeatures = 0;
        for (Feature feature : features) {
            if (feature.getTags() != null) {
                boolean hasQTest = feature.getTags().stream()
                        .anyMatch(t -> t.getName().startsWith("@QTEST_TC_"));
                if (hasQTest) {
                    qtestTaggedFeatures++;
                }
            }
        }
        assertEquals("Should have 8 features with QTEST tags", 8, qtestTaggedFeatures);
    }

    @Test
    public void testNoDuplicateFileNames() {
        Map<String, String> fileNames = new HashMap<>();
        for (Feature feature : features) {
            String fileName = nameBuilder.buildFileName(feature);
            if (fileNames.containsKey(fileName)) {
                assertTrue("Duplicate file name found: " + fileName
                        + " for features: " + fileNames.get(fileName)
                        + " and " + feature.getName(), false);
            }
            fileNames.put(fileName, feature.getName());
        }
    }
}
