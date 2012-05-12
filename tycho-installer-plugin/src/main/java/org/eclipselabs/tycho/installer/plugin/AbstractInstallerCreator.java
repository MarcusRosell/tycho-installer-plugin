package org.eclipselabs.tycho.installer.plugin;

import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

public abstract class AbstractInstallerCreator implements InstallerCreator {
    private final StreamConsumer outStreamConsumer;
    private final StreamConsumer errStreamConsumer;

    protected AbstractInstallerCreator(final Log log) {
        outStreamConsumer = new StreamConsumer() {

            @Override
            public void consumeLine(String line) {
                log.info(line);
            }
        };
        errStreamConsumer = new StreamConsumer() {

            @Override
            public void consumeLine(String line) {
                log.error(line);
            }
        };
    }

    protected void executeCmd(Commandline cmd) throws CommandLineException {
        if (CommandLineUtils.executeCommandLine(cmd, outStreamConsumer, errStreamConsumer) != 0)
            throw new CommandLineException("Error executing cmd:" + cmd);
    }

    protected Commandline buildCmd(String executable, List<String> args) {
        Commandline cmd = new Commandline();
        cmd.setExecutable(executable);
        cmd.addArguments(args.toArray(new String[args.size()]));
        return cmd;
    }
}
