package org.eclipselabs.tycho.installer.plugin.win;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.util.Os;
import org.eclipselabs.tycho.installer.plugin.BaseInstallerCreatorTest;
import org.eclipselabs.tycho.installer.plugin.Product;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

/**
 * This is a test case with Windows specific tests and we check that via {@link Assume#assumeTrue(boolean)}.
 * It would be nicer to use {@linkplain http://code.google.com/p/junit-ext/}, because then the tests
 * would be marked as ignored and we could declaratively mark the test as Windows specific.
 */
public class MsiInstallerCreatorTest extends BaseInstallerCreatorTest {
    private MsiInstallerCreator msiInstallerCreator = new MsiInstallerCreator(new SystemStreamLog());

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void verfifyToolSetup() {
        assumeTrue(Os.isFamily(Os.FAMILY_WINDOWS));

        msiInstallerCreator.verifyToolSetup();
    }

    @Test
    public void generateProductWxsFile() throws Exception {
        File productWxsFile = tempFolder.newFile("product.wxs");
        Product product = new Product("Test App", "1.0.0",
                "eclipselabs.org", "License Text", "Test App", "UPGRADE_GUID");

        msiInstallerCreator.generateProductWxsFile(product, productWxsFile);

        FileInputStream fileInputStream = null;
        InputStream inputStream = null;
        try {
            fileInputStream = new FileInputStream(productWxsFile);

            String generated = read(fileInputStream).trim();
            String expected = readFromJar("expected-product.wxs").trim();

            assertEquals(expected, generated);
        } finally {
            Closeables.closeQuietly(fileInputStream);
            Closeables.closeQuietly(inputStream);
        }
    }

    private String readFromJar(String filename) throws IOException {
        final InputStream inputStream = getClass().getResourceAsStream(filename);
        if (inputStream == null) {
            throw new FileNotFoundException(filename);
        }
        return read(inputStream);
    }

    private String read(final InputStream inputStream) throws IOException {
        try {
            return CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
        } finally {
            Closeables.closeQuietly(inputStream);
        }
    }

    @Test
    public void createInstaller() throws Exception {
        assumeTrue(Os.isFamily(Os.FAMILY_WINDOWS));

        msiInstallerCreator.createInstaller(config);
        File msiInstallerFile = new File(installerDir.getAbsolutePath() + ".msi");
        assertTrue("Expected that file '" + msiInstallerFile + "' exists!" , msiInstallerFile.exists());
    }
}
