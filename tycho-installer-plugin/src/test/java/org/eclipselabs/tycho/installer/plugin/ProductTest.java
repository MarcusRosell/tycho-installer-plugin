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
		assertEquals("Test App", product.getName());
		assertEquals("1.0.0.qualifier", product.getVersion());
		assertEquals("Test App", product.getLauncherName());
		assertEquals("Test App.app", product.getMacOsXAppName());
		assertEquals("This is a simple license text.", product.getLicenseText().trim());
	}
}
