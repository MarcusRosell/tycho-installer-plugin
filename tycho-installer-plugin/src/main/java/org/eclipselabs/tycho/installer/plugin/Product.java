package org.eclipselabs.tycho.installer.plugin;

import java.io.File;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;

public class Product {
    public final String name;
    public final String version;
    public final String manufacturer;
    public final String launcherName;
    public final String upgradeCode;
    public final String licenseText;

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
		this.version = replaceQualifier(valueFromXPath(productConfig, "/product/@version"));
		this.launcherName = valueFromXPath(productConfig, "/product/launcher/@name");
		this.manufacturer = manufacturer;
		this.upgradeCode = UUID.nameUUIDFromBytes(name.getBytes()).toString();
		this.licenseText = valueFromXPath(productConfig, "/product/license/text");
	}

	private String replaceQualifier(String osgiVersion) {
		return osgiVersion.replace(".qualifier", ".0");
	}

	private String valueFromXPath(Document productConfig, String xpath) throws XPathExpressionException {
		return javax.xml.xpath.XPathFactory.newInstance().newXPath().evaluate(xpath, productConfig);
	}

    @Override
    public String toString() {
        return "Product [name=" + name + ", version=" + version + ", manufacturer=" + manufacturer + ", launcherName="
                + launcherName + ", upgradeCode=" + upgradeCode + "]";
    }
}
