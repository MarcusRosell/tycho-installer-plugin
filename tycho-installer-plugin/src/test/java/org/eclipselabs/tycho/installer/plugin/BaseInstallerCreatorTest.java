package org.eclipselabs.tycho.installer.plugin;

import java.io.File;

import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Before;

public class BaseInstallerCreatorTest {
	protected File productDir;

	protected String productRootName;
	
	protected File installerFile;
	
	protected InstallerConfig config;
	
	@Before
	public void setup() throws Exception {
		productDir = ResourceExtractor.simpleExtractResources(getClass(),
				"/test-app/Test App");
		productRootName = "Test App";
		installerFile = new File(productDir, productRootName);
		installerFile.delete();
		File productFile = new File(productDir, "../test.product");
		Product product = new Product(productFile, "eclipselabs.org");
		config = new InstallerConfig(productRootName, productDir,
				installerFile, product);
	}
}
