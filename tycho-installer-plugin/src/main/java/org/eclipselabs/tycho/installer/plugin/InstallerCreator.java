package org.eclipselabs.tycho.installer.plugin;


public interface InstallerCreator {
    /**
     * Verifies that all required tools are setup correctly.
     * @throws IllegalStateException
     */
    void verifyToolSetup() throws IllegalStateException;
    
    void createInstaller(InstallerConfig config) throws Exception;
}
