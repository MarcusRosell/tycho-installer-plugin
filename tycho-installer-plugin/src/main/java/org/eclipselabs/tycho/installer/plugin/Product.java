package org.eclipselabs.tycho.installer.plugin;

import java.io.File;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;

public class Product {
    private final String name;
    private final String version;
    private final String manufacturer;
    private final String launcherName;
    private final String upgradeCode;
    private final String licenseText;

    @VisibleForTesting
    public Product(String name, String version, String manufacturer, String licenseText, String launcher, String upgradeCode) {
        this.name = name;
        this.version = version;
        this.manufacturer = manufacturer;
        this.upgradeCode = upgradeCode;
        this.launcherName = launcher;
        this.licenseText = licenseText;
    }
    
    public Product(File productFile, String manufacturer) throws Exception {
    	DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    	Document productConfig = documentBuilder.parse(productFile);
		this.name = valueFromXPath(productConfig, "/product/@name");
		this.version = valueFromXPath(productConfig, "/product/@version");
		this.launcherName = valueFromXPath(productConfig, "/product/launcher/@name");
		this.manufacturer = manufacturer;
		this.upgradeCode = UUID.nameUUIDFromBytes(name.getBytes()).toString();
		this.licenseText = valueFromXPath(productConfig, "/product/license/text");
	}

	private String valueFromXPath(Document productConfig, String xpath) throws XPathExpressionException {
		return javax.xml.xpath.XPathFactory.newInstance().newXPath().evaluate(xpath, productConfig);
	}

	public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getMacOsXAppName() {
        return launcherName + ".app";
    }

    public String getUpgradeCode() {
        return upgradeCode;
    }

    public String getExecutable() {
        return launcherName + ".exe";
    }

    public String getLicenseText() {
        return licenseText;
    }
    
    public String getLauncherName() {
    	return launcherName;
    }

    @Override
    public String toString() {
        return "Product [name=" + name + ", version=" + version + ", manufacturer=" + manufacturer + ", launcherName="
                + launcherName + ", upgradeCode=" + upgradeCode + "]";
    }
}
