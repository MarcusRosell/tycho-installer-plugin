package org.eclipselabs.tycho.installer.plugin;

import java.io.File;

public class InstallerConfig {
    public final String installerName;
    public final File productDir;
    public final File installerDir;
    public final Product product;

    public InstallerConfig(String installerName, File productDir, File installerDir, Product product) {
        this.installerName = installerName;
        this.productDir = productDir;
        this.installerDir = installerDir;
        this.product = product;
    }

	@Override
	public String toString() {
		return "InstallerConfig [installerName=" + installerName
				+ ", productDir=" + productDir + ", installerDir="
				+ installerDir + ", product=" + product + "]";
	}
}
