package org.eclipselabs.tycho.installer.plugin;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;

public class ProductTest {

	@Test
	public void loadProduct() throws Exception {
		File productFile = ResourceExtractor.simpleExtractResources(getClass(), "/test-app/test.product");
		Product product = new Product(productFile, "eclipselabs.org");
		assertEquals("Test App", product.name);
		assertEquals("1.0.0.qualifier", product.version);
		assertEquals("Test App", product.launcherName);
		assertEquals("This is a simple license text.", product.licenseText.trim());
	}
}
