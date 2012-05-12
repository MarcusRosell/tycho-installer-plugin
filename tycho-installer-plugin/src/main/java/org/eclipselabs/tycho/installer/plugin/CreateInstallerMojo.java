package org.eclipselabs.tycho.installer.plugin;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Os;
import org.eclipselabs.tycho.installer.plugin.macosx.DmgInstallerCreator;
import org.eclipselabs.tycho.installer.plugin.win.MsiInstallerCreator;

/**
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
     * @parameter
     * @required
     */
    private String manufacturer;

    /**
     * @parameter
     * @required
     */
    private File productDir;

    /**
     * @parameter default-value="${project.basedir}/${project.artifactId}.product"
     * @required
     */
    private File productFile;

    /**
     * @parameter default-value="${project.build.directory}/installer"
     * @required
     */
    private File installerDir;

    /**
     * @parameter
     * @required
     */
    private String installerName;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        InstallerCreator installerCreator = getInstallerCreator();
        if (installerCreator == null) {
            getLog().info("No installer creator found!");
            return;
        }
        installerCreator.verifyToolSetup();
        if (!installerDir.mkdirs()) {
        	throw new MojoExecutionException("Can't create installer target directory " + installerDir);
        }
        try {
            Product product = new Product(productFile, manufacturer);
            InstallerConfig config = new InstallerConfig(installerName, productDir, installerDir, product);
            getLog().info(config.toString());

            installerCreator.createInstaller(config);
        } catch (Exception e) {
            throw new MojoExecutionException("Error while executing create-installer!", e);
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
