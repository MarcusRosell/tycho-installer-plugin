package org.eclipselabs.tycho.installer.plugin.macosx;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.util.Os;
import org.eclipselabs.tycho.installer.plugin.BaseInstallerCreatorTest;
import org.junit.Assume;
import org.junit.Test;

/**
 * This is a test that only runs on Mac OS X and we check that via
 * {@link Assume#assumeTrue(boolean)}. It would be nicer to use
 * {@linkplain http://code.google.com/p/junit-ext/}, because then the tests
 * would be marked as ignored and we could declaratively mark the whole test as
 * Mac OS X specific.
 */
public class DmgInstallerCreatorTest extends BaseInstallerCreatorTest {

	private DmgInstallerCreator dmgInstallerCreator = new DmgInstallerCreator(
			new SystemStreamLog());

	@Test
	public void verfifyToolSetup() {
		assumeTrue(Os.isFamily(Os.FAMILY_MAC));

		dmgInstallerCreator.verifyToolSetup();
	}

	@Test
	public void createInstaller() throws Exception {
		assumeTrue(Os.isFamily(Os.FAMILY_MAC));

		dmgInstallerCreator.createInstaller(config);
		File dmgInstallerFile = new File(installerDir, config.installerName
				+ ".dmg");
		assertTrue("Expected that file '" + dmgInstallerFile + "' exists!",
				dmgInstallerFile.exists());
	}
}
