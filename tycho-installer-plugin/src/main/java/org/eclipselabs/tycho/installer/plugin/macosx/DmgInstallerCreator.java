package org.eclipselabs.tycho.installer.plugin.macosx;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.eclipselabs.tycho.installer.plugin.AbstractInstallerCreator;
import org.eclipselabs.tycho.installer.plugin.InstallerConfig;

public class DmgInstallerCreator extends AbstractInstallerCreator {
    private static final String LAUNCHER_LIBRARY_PATTERN = "org.eclipse.equinox.launcher.cocoa.macosx.x86_64*";

    private static final String LAUNCHER_JAR_PATTERN = "org.eclipse.equinox.launcher*.jar";

    private static final String APPLICATIONS_FOLDER_NAME = "Applications";

    private final File hdiutil = new File("/usr/bin/hdiutil");

    public DmgInstallerCreator(Log log) {
        super(log);
    }

    @Override
    public void verifyToolSetup() throws IllegalStateException {
        if (!hdiutil.exists()) {
            throw new IllegalStateException("hdiutil command wasn't found at:" + hdiutil.getAbsolutePath() + "!");
        }
    }

    @Override
    public void createInstaller(InstallerConfig config) throws Exception {
        File dmgProductDir = new File(new File(config.productDir.getParentFile(), "dmg"), config.productRootName);
        dmgProductDir.mkdirs();

        repackageProduct(config, config.productDir, dmgProductDir);
        createApplicationsLink(dmgProductDir);
        createProductDmg(config, dmgProductDir);
    }

    private void repackageProduct(final InstallerConfig config, File productDir, File dmgProductDir) throws Exception {
        File appDir = new File(productDir, config.product.getMacOsXAppName());
        if (!appDir.exists()) {
            throw new IllegalStateException("No app was found at:'" + appDir + "'!");
        }
        File dmgAppDir = new File(dmgProductDir, appDir.getName());

        FileUtils.copyDirectoryStructure(appDir, dmgAppDir);
        File pluginsDir = new File(productDir, "plugins");
        fixLauncher(new File(dmgAppDir, "Contents/MacOS"), pluginsDir);
        
        File[] filesToCopy = productDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return !name.equals(config.product.getMacOsXAppName());
            }
        });
        for (File fileToCopy : filesToCopy) {
            if (fileToCopy.isDirectory()) {
                File correspondingDmgFolder = new File(dmgAppDir, fileToCopy.getName());
                correspondingDmgFolder.mkdir();
                FileUtils.copyDirectoryStructure(fileToCopy, correspondingDmgFolder);
            } else {
                FileUtils.copyFileToDirectory(fileToCopy, dmgAppDir);
            }
        }
    }

    private void fixLauncher(File launcherDir, File pluginsDir) throws CommandLineException, IOException {
        @SuppressWarnings("unchecked")
        List<String> launcherJarNames = FileUtils.getFileNames(pluginsDir, LAUNCHER_JAR_PATTERN, null, false);
        if (launcherJarNames.size() != 1) {
            throw new IllegalStateException("Can't find " + LAUNCHER_JAR_PATTERN + " file in " + pluginsDir + "!");
        }
        String launcherJarName = launcherJarNames.get(0);
        @SuppressWarnings("unchecked")
        List<String> launcherLibraryNames = FileUtils.getDirectoryNames(pluginsDir, LAUNCHER_LIBRARY_PATTERN, null, false);
        if (launcherLibraryNames.size() != 1) {
            throw new IllegalStateException("Can't find " + LAUNCHER_LIBRARY_PATTERN + " directory in " + pluginsDir + "!");            
        }
        String launcherLibraryName = launcherLibraryNames.get(0);
        File[] programs = launcherDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return !name.endsWith(".ini");
            }
        });
        Commandline cmd = new Commandline();
        cmd.setExecutable("chmod");
        List<String> args = new ArrayList<String>();
        args.add("a+x");
        args.add(programs[0].getAbsolutePath());
        cmd.addArguments(args.toArray(new String[args.size()]));
        executeCmd(cmd);
        File[] iniFiles = launcherDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".ini");
            }
        });
        for (File iniFile : iniFiles) {
            FileReader fileReader = null;
            FileWriter fileWriter = null;
            try {
                fileReader = new FileReader(iniFile);
                String iniString = IOUtil.toString(fileReader);
                String fixedIniString = fixIni(iniString, launcherJarName, launcherLibraryName);
                fileWriter = new FileWriter(iniFile);
                fileWriter.write(fixedIniString);
            } finally {
                IOUtil.close(fileReader);
                IOUtil.close(fileWriter);
            }
        }
    }

    private String fixIni(String iniString, String launcherJarName, String launcherLibraryName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("-startup\n");
        buffer.append("../../plugins/").append(launcherJarName).append("\n");
        buffer.append("--launcher.library\n");
        buffer.append("../../plugins/").append(launcherLibraryName).append("\n");
        String[] iniLines = iniString.split("\n");
        int index = 0;
        if (iniLines.length >= 2) {
            if (iniLines[0].startsWith("-startup")) {
                index = 2;
            }
        }
        if (iniLines.length >= index + 2) {
            if (iniLines[index].startsWith("--launcher.library")) {
                index += 2;
            }
        }
        for (int i = index; i < iniLines.length; i++) {
            buffer.append(iniLines[i]).append("\n");
        }
        return buffer.toString();
    }

    private void createApplicationsLink(File dmgProductDir) throws CommandLineException {
        Commandline cmd = new Commandline();
        cmd.setExecutable("ln");
        File linkToApplications = new File(dmgProductDir, APPLICATIONS_FOLDER_NAME);
        cmd.addArguments(new String[] {
                "-s", "/" + APPLICATIONS_FOLDER_NAME,
                linkToApplications.getAbsolutePath()});
        executeCmd(cmd);
    }


    private void createProductDmg(InstallerConfig config, File dmgProductDir) throws CommandLineException {
        File dmgInstallerFile = new File(config.installerFile.getAbsolutePath() + ".dmg");
        Commandline cmd = new Commandline();
        cmd.setExecutable(hdiutil.getAbsolutePath());
        cmd.addArguments(new String[]{
                "create", "-srcfolder", dmgProductDir.getAbsolutePath(), dmgInstallerFile.getAbsolutePath(),
                "-scrub"
        });
        executeCmd(cmd);
    }
}
