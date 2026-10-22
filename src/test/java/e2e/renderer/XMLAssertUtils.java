package e2e.renderer;

import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Snapshot assertions for XBuilder renderer tests.
 *
 * This class intentionally does NOT send documents to SUNAT; network integration
 * tests belong to the XSender project. Credentials are test-only demo values and
 * must not be used against production.
 */
public class XMLAssertUtils {

    public static final String INVOICE_XSD = "xsd/2.1/maindoc/UBL-Invoice-2.1.xsd";
    public static final String CREDIT_NOTE_XSD = "xsd/2.1/maindoc/UBL-CreditNote-2.1.xsd";
    public static final String DEBIT_NOTE_XSD = "xsd/2.1/maindoc/UBL-DebitNote-2.1.xsd";
    public static final String DESPATCH_ADVICE_XSD = "xsd/2.1/maindoc/UBL-DespatchAdvice-2.1.xsd";
    public static final String VOIDED_DOCUMENTS_XSD = "xsd/2.0/maindoc/UBLPE-VoidedDocuments-1.0.xsd";
    public static final String SUMMARY_DOCUMENTS_XSD = "xsd/2.0/maindoc/UBLPE-SummaryDocuments-1.0.xsd";
    public static final String PERCEPTION_XSD = "xsd/2.0/maindoc/UBLPE-Perception-1.0.xsd";
    public static final String RETENTION_XSD = "xsd/2.0/maindoc/UBLPE-Retention-1.0.xsd";

    public static void assertSnapshot(String expected, Class<?> clasz, String snapshotFile) {
        String rootDir = clasz.getName().replaceAll("\\.", "/");

        // Update snapshots and if updated do not verify since it doesn't make sense anymore
        boolean updateSnapshots = Boolean.parseBoolean(System.getProperty("xbuilder.snapshot.update", "false"));
        if (updateSnapshots) {
            try {
                Path directoryPath = Paths.get("src", "test", "resources").resolve(rootDir);
                Files.createDirectories(directoryPath);

                Path filePath = directoryPath.resolve(snapshotFile);
                Files.write(filePath, expected.getBytes());

                return;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        InputStream snapshotInputStream = Thread
                .currentThread()
                .getContextClassLoader()
                .getResourceAsStream(rootDir + "/" + snapshotFile);
        assertNotNull(snapshotInputStream, "Could not find snapshot file " + snapshotFile);

        Diff myDiff = DiffBuilder
                .compare(snapshotInputStream)
                .withTest(expected)
                .ignoreComments()
                .ignoreWhitespace()
                .build();

        assertFalse(myDiff.hasDifferences(), expected + "\n" + myDiff);
    }

    public static void assertSnapshot(String expected, String expectedReverse, Class<?> clasz, String snapshotFile) {
        assertSnapshot(expected, clasz, snapshotFile);
        assertSnapshot(expectedReverse, clasz, snapshotFile);
    }
}
