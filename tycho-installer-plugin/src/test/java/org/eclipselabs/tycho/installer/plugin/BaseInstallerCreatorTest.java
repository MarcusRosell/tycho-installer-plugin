package org.eclipselabs.tycho.installer.plugin;

import java.io.File;

import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Before;

public class BaseInstallerCreatorTest {
	private final String installerName = "Test App";

	private File productDir;

	protected File installerDir;

	protected InstallerConfig config;

	@Before
	public void setup() throws Exception {
		productDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/test-app/Test App");
		installerDir = new File(productDir, "installer");
		installerDir.delete();
		installerDir.mkdirs();
		File productFile = ResourceExtractor.simpleExtractResources(getClass(),
				"/test-app/test.product");
		Product product = new Product(productFile, "eclipselabs.org");
		config = new InstallerConfig(installerName, productDir, installerDir,
				product);
	}
}
