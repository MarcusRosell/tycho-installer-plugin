package org.eclipselabs.tycho.installer.plugin;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Os;
import org.eclipse.tycho.core.utils.PlatformPropertiesUtils;
import org.eclipselabs.tycho.installer.plugin.macosx.DmgInstallerCreator;
import org.eclipselabs.tycho.installer.plugin.win.MsiInstallerCreator;

import com.google.common.base.Joiner;

/**
 * This goal creates native installers from a eclipse product build with tycho.
 * <ul>
 * <li>When running on Mac OS X it will create a dmg installer via the
 * <code>hdiutil</code> program.</li>
 * <li>When running on Windows it will create a msi installer via the <a
 * href="http://wix.sourceforge.net/">WiX</a> program.</li>
 * </ul>
 * This goal will fail if the required native programs can't be found.
 * 
 * @goal create-installer
 * @requiresProject true
 */
public class CreateInstallerMojo extends AbstractMojo {
	/**
	 * @parameter default-value="${project}"
	 * @readonly
	 */
	protected MavenProject mavenProject;

	/**
	 * The name of the maufacturer, needed for creating a msi installer.
	 * 
	 * @parameter
	 * @required
	 */
	private String manufacturer;

	/**
	 * The directory where the tycho build product can be found. If not
	 * specified it will use the default location used by the eclipse-repository
	 * package type when using the materialize-products goal appended with the
	 * root folder if it is specified.
	 * 
	 * @parameter
	 */
	private File productDir;

	/**
	 * Must be set to the same value as the rootFolder property of the
	 * tycho-p2-director-plugin.
	 * 
	 * @parameter
	 */
	private String rootFolder;

	/**
	 * Path to the .product file. The .product file is used to retrieve the
	 * metadata for the installer.
	 * 
	 * @parameter 
	 *            default-value="${project.basedir}/${project.artifactId}.product"
	 * @required
	 */
	private File productFile;

	/**
	 * Directory where the created installer is saved.
	 * 
	 * @parameter default-value="${project.build.directory}/installer"
	 * @required
	 */
	private File installerDir;

	/**
	 * The base name of the installer, the resulting installer will have this
	 * name with the corresponding file suffix .dmg or .msi.
	 * 
	 * @parameter
	 * @required
	 */
	private String installerName;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		InstallerCreator installerCreator = getInstallerCreator();
		if (installerCreator == null) {
			getLog().info("Installer can't be created when running on this OS!");
			return;
		}
		installerCreator.verifyToolSetup();
		try {
			String buildQualifier = mavenProject.getProperties().getProperty(
					"buildQualifier");
			Product product = new Product(productFile, manufacturer,
					buildQualifier);
			if (productDir == null) {
				String os = PlatformPropertiesUtils.getOS(System
						.getProperties());
				String ws = PlatformPropertiesUtils.getWS(System
						.getProperties());
				String arch = PlatformPropertiesUtils.getArch(System
						.getProperties());
				Joiner joiner = Joiner.on(File.separator);
				String productPath = rootFolder != null ? joiner.join(
						"products", product.id, os, ws, arch, rootFolder)
						: joiner.join("products", product.id, os, ws, arch);
				productDir = new File(mavenProject.getBuild().getDirectory(),
						productPath);
			}
			if (!installerDir.mkdirs()) {
				throw new MojoExecutionException(
						"Can't create installer target directory "
								+ installerDir);
			}
			InstallerConfig config = new InstallerConfig(installerName,
					productDir, installerDir, product);
			getLog().debug(config.toString());

			installerCreator.createInstaller(config);
		} catch (Exception e) {
			throw new MojoExecutionException(
					"Error while executing create-installer!", e);
		}
	}

	private InstallerCreator getInstallerCreator() {
		if (Os.isFamily(Os.FAMILY_MAC)) {
			return new DmgInstallerCreator(getLog());
		}
		if (Os.isFamily(Os.FAMILY_WINDOWS)) {
			return new MsiInstallerCreator(getLog());
		}
		return null;
	}
}
