# Cucumber PDF QTest Plugin


A re-implementation of the [cucumber-pdf-plugin](https://github.com/NoSuchElements/cucumber-pdf-plugin-feature) that generates **one PDF report per feature file**, with file names appended with the `@QTEST_TC_` tag value.

## Problem

The original `cucumber-pdf-plugin` generates a single monolithic PDF containing all features. This plugin splits the output so each feature file gets its own PDF, named using the `@QTEST_TC_` tag for easy integration with qTest.

## File Naming Convention

```
{feature-file-base-name}@{QTEST_TC_XXXX}.pdf
```

### Verified Feature to PDF Mapping (from test JSON data)

| # | Feature | QTEST Tag | Generated PDF |
|---|---------|-----------|---------------|
| 1 | DataTable And DocString | @QTEST_TC_1201 | `datatable-docstring@QTEST_TC_1201.pdf` |
| 2 | Exception | @QTEST_TC_1202 | `exceptions@QTEST_TC_1202.pdf` |
| 3 | Hook and step failures | @QTEST_TC_1203 | `failure@QTEST_TC_1203.pdf` |
| 4 | Long long feature and Scenario Names | @QTEST_TC_1204 | `lengthynames@QTEST_TC_1204.pdf` |
| 5 | No tag feature | *(none)* | `notags.pdf` |
| 6 | Scenario And Scenario Outline | @QTEST_TC_1205 | `scenario&outline@QTEST_TC_1205.pdf` |
| 7 | Screenshots | @QTEST_TC_1206 | `screenshots@QTEST_TC_1206.pdf` |
| 8 | Scenarios With No Step Definitions | @QTEST_TC_1207 | `skipdef@QTEST_TC_1207.pdf` |
| 9 | Two Screenshots | @QTEST_TC_1208 | `twoimages@QTEST_TC_1208.pdf` |

Features without a `@QTEST_TC_` tag are named using just the feature file name.

## Maven Plugin Usage

Add to your project's `pom.xml`:

```xml
<plugin>
    <groupId>com.nosuchelements</groupId>
    <artifactId>cucumber-pdf-qtest-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <id>per-feature-report</id>
            <phase>post-integration-test</phase>
            <goals>
                <goal>pdfreportperfeature</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <cucumberJsonReportDirectory>${project.build.directory}</cucumberJsonReportDirectory>
        <cucumberPdfReportDirectory>${project.build.directory}/pdf-reports</cucumberPdfReportDirectory>
        <qtestTagPrefix>@QTEST_TC_</qtestTagPrefix>
    </configuration>
</plugin>
```

## Plugin Configuration

| Configuration | Description | Default |
|:---|:---|:---|
| cucumberJsonReportDirectory | Directory location of cucumber json reports. **Required.** | |
| cucumberPdfReportDirectory | Directory prefix of location of generated pdf reports | report |
| cucumberPdfReportDirectoryTimeStamp | Directory suffix timestamp format | dd MM yyyy HH mm ss |
| qtestTagPrefix | Tag prefix used for QTEST identification | @QTEST_TC_ |
| strictCucumber6Behavior | Cucumber 6 strict behavior | true |
| title | Report title prefix | Cucumber PDF Report |
| All color/display options | Same as original plugin | (see original docs) |

## Standalone Usage

```bash
java -cp <classpath> com.nosuchelements.runner.PerFeaturePDFReportRunner \
     path/to/cucumber-json.json \
     output/per-feature
```

## Project Structure

```
src/main/java/com/nosuchelements/
|-- CucumberPDFQTestPlugin.java          # Maven Mojo (goal: pdfreportperfeature)
|-- naming/
|   +-- FeatureReportFileNameBuilder.java # File naming with @QTEST_TC_ tags
|-- json/
|   +-- FeatureSplitter.java             # Split JSON into per-feature files
+-- runner/
    +-- PerFeaturePDFReportRunner.java   # Standalone CLI runner

src/test/java/com/nosuchelements/
|-- naming/
|   +-- FeatureReportFileNameBuilderTest.java  # Unit tests (13 tests)
+-- integration/
    +-- FeatureJsonParsingTest.java            # Integration tests (7 tests)

src/test/resources/
+-- cucumber-json.json                        # Test data (9 features)
```

## Recent Fixes

### Font Corruption Issue (March 2026)

**Problem**: When generating multiple per-feature PDF reports in sequence, the second and subsequent reports would fail with:

```
java.io.IOException: The TrueType font null does not contain a 'cmap' table
```

**Root Cause**: The upstream `cucumber-pdf-report` library uses static font fields in the `ReportFont` class. When multiple `PDFCucumberReport` instances are created in sequence (one per feature), fonts loaded for one `PDDocument` can be invalidated when that document is closed, corrupting the static font references used by subsequent reports.

**Solution**: 
- Changed from directory-based constructor `PDFCucumberReport(reportData, directory, options)` to file-based constructor `PDFCucumberReport(reportData, File, options)`
- Each feature now gets its own explicitly-named `File` object created using `FeatureReportFileNameBuilder`
- Added individual feature error handling with try-catch isolation
- Plugin continues processing remaining features even if one fails
- Enhanced logging to track success/failure counts per feature

**Files Modified**:
- `CucumberPDFQTestPlugin.java` - Main plugin execution
- `PerFeaturePDFReportRunner.java` - Standalone runner

## Troubleshooting

### Font Errors

If you encounter font-related errors:

1. **Verify cucumber-pdf-report version**: Ensure you're using version 2.13.0 or higher in your POM
2. **Check classpath**: Font files must be accessible in the `tech.grasshopper.ttf` resource path
3. **Review logs**: Each feature's success/failure is logged individually
4. **Partial generation**: The plugin continues processing all features even if some fail - check which specific features encountered errors

### Common Issues

**Plugin execution fails**: Ensure `cucumberJsonReportDirectory` points to valid JSON output from Cucumber

**No PDFs generated**: Check that your feature files have `@QTEST_TC_` tags (or expect base filename only)

**Wrong file names**: Verify `qtestTagPrefix` matches your tag format in feature files

## Building

```bash
mvn clean install
```

## Running Tests

```bash
mvn test
```

## License

MIT
