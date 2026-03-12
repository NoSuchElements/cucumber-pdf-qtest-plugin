package com.nosuchelements.naming;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import tech.grasshopper.pojo.Feature;
import tech.grasshopper.pojo.Tag;

public class FeatureReportFileNameBuilderTest {

    private FeatureReportFileNameBuilder builder;

    @Before
    public void setUp() {
        builder = new FeatureReportFileNameBuilder();
    }

    @Test
    public void testFeatureWithQTestTag() {
        Feature feature = createFeature(
                "DataTable And DocString",
                "file:src/test/resources/stepdefs/datatable-docstring.feature",
                Arrays.asList(createTag("@tabledoc"), createTag("@QTEST_TC_1201")));

        String fileName = builder.buildFileName(feature);
        assertEquals("datatable-docstring@QTEST_TC_1201.pdf", fileName);
    }

    @Test
    public void testFeatureWithoutQTestTag() {
        Feature feature = createFeature(
                "No tag feature",
                "file:src/test/resources/stepdefs/notags.feature",
                Collections.emptyList());

        String fileName = builder.buildFileName(feature);
        assertEquals("notags.pdf", fileName);
    }

    @Test
    public void testFeatureWithExceptionTag() {
        Feature feature = createFeature(
                "Exception",
                "file:src/test/resources/stepdefs/exceptions.feature",
                Arrays.asList(createTag("@exception"), createTag("@QTEST_TC_1202")));

        String fileName = builder.buildFileName(feature);
        assertEquals("exceptions@QTEST_TC_1202.pdf", fileName);
    }

    @Test
    public void testFeatureWithFailureTag() {
        Feature feature = createFeature(
                "Hook and step failures",
                "file:src/test/resources/stepdefs/failure.feature",
                Arrays.asList(createTag("@failure"), createTag("@QTEST_TC_1203")));

        String fileName = builder.buildFileName(feature);
        assertEquals("failure@QTEST_TC_1203.pdf", fileName);
    }

    @Test
    public void testFeatureWithLengthyNamesTag() {
        Feature feature = createFeature(
                "Long long feature & Scenario Names",
                "file:src/test/resources/stepdefs/lengthynames.feature",
                Arrays.asList(createTag("@longlong"), createTag("@lengthyfeature"),
                        createTag("@QTEST_TC_1204")));

        String fileName = builder.buildFileName(feature);
        assertEquals("lengthynames@QTEST_TC_1204.pdf", fileName);
    }

    @Test
    public void testFeatureWithScenarioOutlineTag() {
        Feature feature = createFeature(
                "Scenario And Scenario Outline",
                "file:src/test/resources/stepdefs/scenario&outline.feature",
                Arrays.asList(createTag("@both"), createTag("@QTEST_TC_1205")));

        String fileName = builder.buildFileName(feature);
        assertEquals("scenario&outline@QTEST_TC_1205.pdf", fileName);
    }

    @Test
    public void testFeatureWithScreenshotsTag() {
        Feature feature = createFeature(
                "Screenshots",
                "file:src/test/resources/stepdefs/screenshots.feature",
                Arrays.asList(createTag("@website"), createTag("@skip"),
                        createTag("@QTEST_TC_1206")));

        String fileName = builder.buildFileName(feature);
        assertEquals("screenshots@QTEST_TC_1206.pdf", fileName);
    }

    @Test
    public void testFeatureWithSkipDefTag() {
        Feature feature = createFeature(
                "Scenarios With No Step Definitions",
                "file:src/test/resources/stepdefs/skipdef.feature",
                Arrays.asList(createTag("@skip"), createTag("@QTEST_TC_1207")));

        String fileName = builder.buildFileName(feature);
        assertEquals("skipdef@QTEST_TC_1207.pdf", fileName);
    }

    @Test
    public void testFeatureWithTwoImagesTag() {
        Feature feature = createFeature(
                "Two Screenshots",
                "file:src/test/resources/stepdefs/twoimages.feature",
                Arrays.asList(createTag("@twoimg"), createTag("@QTEST_TC_1208")));

        String fileName = builder.buildFileName(feature);
        assertEquals("twoimages@QTEST_TC_1208.pdf", fileName);
    }

    @Test
    public void testNullUri() {
        Feature feature = createFeature("Test", null,
                Arrays.asList(createTag("@QTEST_TC_9999")));

        String fileName = builder.buildFileName(feature);
        assertEquals("unknown-feature@QTEST_TC_9999.pdf", fileName);
    }

    @Test
    public void testEmptyUri() {
        Feature feature = createFeature("Test", "",
                Arrays.asList(createTag("@QTEST_TC_9999")));

        String fileName = builder.buildFileName(feature);
        assertEquals("unknown-feature@QTEST_TC_9999.pdf", fileName);
    }

    @Test
    public void testNullTags() {
        Feature feature = createFeature("Test",
                "file:src/test/resources/stepdefs/test.feature", null);

        String fileName = builder.buildFileName(feature);
        assertEquals("test.pdf", fileName);
    }

    @Test
    public void testCustomQTestPrefix() {
        FeatureReportFileNameBuilder customBuilder =
                new FeatureReportFileNameBuilder("@CUSTOM_TC_");

        Feature feature = createFeature("Test",
                "file:src/test/resources/stepdefs/test.feature",
                Arrays.asList(createTag("@CUSTOM_TC_5555")));

        String fileName = customBuilder.buildFileName(feature);
        assertEquals("test@CUSTOM_TC_5555.pdf", fileName);
    }

    private Feature createFeature(String name, String uri, List<Tag> tags) {
        Feature feature = new Feature();
        feature.setName(name);
        feature.setUri(uri);
        feature.setTags(tags);
        return feature;
    }

    private Tag createTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        return tag;
    }
}
