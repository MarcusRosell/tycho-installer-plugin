package org.eclipselabs.tycho.installer.plugin;

import java.io.File;

public class InstallerConfig {
    public final String productRootName;
    public final File productDir;
    public final File installerFile;
    public final Product product;

    public InstallerConfig(String productRootName, File productDir, File installerFile, Product product) {
        this.productRootName = productRootName;
        this.productDir = productDir;
        this.installerFile = installerFile;
        this.product = product;
    }

    @Override
    public String toString() {
        return "InstallerConfig [productRootName=" + productRootName + ", productDir=" + productDir + ", installerFile="
                + installerFile + ", product=" + product + "]";
    }
}
