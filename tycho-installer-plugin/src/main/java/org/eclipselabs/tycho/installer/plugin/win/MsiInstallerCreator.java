package org.eclipselabs.tycho.installer.plugin.win;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.eclipselabs.tycho.installer.plugin.AbstractInstallerCreator;
import org.eclipselabs.tycho.installer.plugin.InstallerConfig;
import org.eclipselabs.tycho.installer.plugin.Product;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter2;

public class MsiInstallerCreator extends AbstractInstallerCreator {
    private final static List<String> COMMON_ARGS = Arrays.asList("-ext", "WixUIExtension", "-ext", "WixUtilExtension");
    private StringTemplateGroup templateGroup;

    public MsiInstallerCreator(Log log) {
        super(log);
        initTemplates();
    }

    private void initTemplates() {
        String template = "product-wxs.stg";
        final InputStream inputStream = getClass().getResourceAsStream(template);
        if (inputStream == null) {
            throw new IllegalStateException("Error: " + template + " not found");
        }
        try {
            final Reader reader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));
            try {
                templateGroup = new StringTemplateGroup(reader, DefaultTemplateLexer.class);
            } finally {
                Closeables.closeQuietly(reader);
            }
        } finally {
            Closeables.closeQuietly(inputStream);
        }
    }

    @Override
    public void verifyToolSetup() throws IllegalStateException {
    	try {
    		Commandline heatCmd = buildCmd("heat", Collections.<String>emptyList());
			executeCmd(heatCmd, false);

    		Commandline candleCmd = buildCmd("candle", Collections.<String>emptyList());
			executeCmd(candleCmd, false);

    		Commandline lightCmd = buildCmd("light", Collections.<String>emptyList());
			executeCmd(lightCmd, false);
		} catch (CommandLineException e) {
			throw new IllegalStateException(e);
		}
    }

    @Override
    public void createInstaller(InstallerConfig config) throws Exception {
        File msiProductDir = new File(config.productDir.getParentFile(), "msi");
        msiProductDir.mkdirs();

        File productFilesWxsFile = new File(msiProductDir, config.installerName + "-files.wxs");
        harvestDir(config.productDir, false, "ProductFiles", "APPLICATIONROOTDIRECTORY", productFilesWxsFile);

        File productWxsFile = new File(msiProductDir, config.installerName + ".wxs");
        generateProductWxsFile(config.product, productWxsFile);

        File licenseFile = createLicenseFile(msiProductDir, config.product.licenseText);
        List<File> compiledWxsFiles = compileWxsFiles(productFilesWxsFile, productWxsFile);
        createMsiInstaller(config, config.productDir, compiledWxsFiles, licenseFile);
    }

    private void harvestDir(File dir, boolean includeRootDir, String componentGroup, String directoryRef, File outFile)
            throws CommandLineException {
        List<String> args = new ArrayList<String>();
        Collections.addAll(args, "dir", dir.getAbsolutePath());
        Collections.addAll(args, "-template", "fragment", "-gg", "-sfrag", "-scom");
        if (!includeRootDir)
            args.add("-srd");
        Collections.addAll(args, "-cg", componentGroup);
        Collections.addAll(args, "-dr", directoryRef);
        Collections.addAll(args, "-out", outFile.getAbsolutePath());

        Commandline cmd = buildCmd("heat", args);
        executeCmd(cmd);
    }

    @VisibleForTesting
    void generateProductWxsFile(Product product, File productWxsFile) throws IOException {
        StringTemplate template = templateGroup.getInstanceOf("productWxsFile");
        template.setAttribute("product", product);
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            fileOutputStream = new FileOutputStream(productWxsFile);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
            outputStreamWriter.append(template.toString());
        } finally {
            Closeables.closeQuietly(outputStreamWriter);
            Closeables.closeQuietly(fileOutputStream);
        }
    }

    private List<File> compileWxsFiles(File... wxsFiles) throws CommandLineException {
        List<File> compiledFiles = new ArrayList<File>();
        for (File wxsFile : wxsFiles) {
            String wxsName = wxsFile.getName();
            String extension = FileUtils.getExtension(wxsName);
            String compiledName = wxsName.substring(0, wxsName.length() - extension.length()) + "wixobj";
            File outFile = new File(wxsFile.getParentFile(), compiledName);
            compiledFiles.add(outFile);

            List<String> args = new ArrayList<String>();
            Collections.addAll(args, "-out", outFile.getAbsolutePath());
            args.add(wxsFile.getAbsolutePath());
            args.addAll(COMMON_ARGS);

            Commandline cmd = buildCmd("candle", args);
            executeCmd(cmd);
        }

        return compiledFiles;
    }

    private void createMsiInstaller(InstallerConfig config, File productDir,
            List<File> compiledWxsFiles, File licenseFile) throws Exception {
        List<String> args = new ArrayList<String>();

        args.add("-dWixUILicenseRtf=" + licenseFile.getAbsolutePath());
        Collections.addAll(args, "-out", new File(config.installerDir, config.installerName + ".msi").getAbsolutePath());

        for (File compiledWxsFile : compiledWxsFiles) {
            args.add(compiledWxsFile.getAbsolutePath());
        }
        args.addAll(COMMON_ARGS);
        Collections.addAll(args, "-b", productDir.getAbsolutePath());

        Commandline cmd = buildCmd("light", args);
        executeCmd(cmd);
    }

    private File createLicenseFile(File dir, String licenseText) throws Exception {
        File licenseFile = new File(dir, "license.rtf");
        Document document = new Document();
        FileOutputStream fileOutputStream = new FileOutputStream(licenseFile);
        try {
            RtfWriter2.getInstance(document, fileOutputStream);
            document.open();
            document.add(new Paragraph(licenseText));
            document.close();
        } finally {
            Closeables.closeQuietly(fileOutputStream);
        }
        return licenseFile;
    }
}
