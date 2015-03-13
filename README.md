#Introduction
The tycho-installer-plugin is a maven plugin that creates native installers for eclipse products created with eclipse-repository packaging type of [Tycho](http://eclipse.org/tycho). It supports creating dmg installer when running on Mac OS X and msi installer when running on windows.

#Prerequisites
This plugin requies that you run it on the native platform for which you want to create an installer:

On Mac OS X it is using the hdiutil program, which must be installed in the /usr/bin folder.
On windows you have to install [WiX](http://wix.sourceforge.net/) and the binaries have to be added to the PATH environment variable.
Usage

#Example
You find a fully working example in the test-project folder in this projects git repository.

#How to use this plugin.

The `tycho-installer-plugin` can be fetched from this maven repository (https://repository-katmatt.forge.cloudbees.com/snapshot/) by adding the following code to your `pom.xml`:

```
<pluginRepositories>
    <pluginRepository>
        <id>tycho-installer-plugin-repo</id>
        <layout>default</layout>
        <url>https://repository-katmatt.forge.cloudbees.com/snapshot/</url>                                     
    </pluginRepository>
</pluginRepositories>
```

#Configuration

The easiest way to use this plugin is to add it to the `pom.xml`  of your product by adding a profile `create-installer`  with which you enable building an installer:

```
<profiles>
    <profile>
        <id>create-installer</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.eclipselabs.tycho.installer.plugin</groupId>
                    <artifactId>tycho-installer-plugin</artifactId>
                    <version>0.9.0-SNAPSHOT</version>
                    <executions>
                        <execution>
                            <id>create-installer</id>
                            <phase>package</phase>
                            <goals>
                                <goal>create-installer</goal>
                            </goals>
                        </execution>
                    </executions>
                    <configuration>
                        <manufacturer>My company</manufacturer>
                        <productFile>${project.basedir}/my.product</productFile>
                        <rootFolder>my-root-folder</rootFolder>
                        <installerName>MyInstaller-${project.version}</installerName>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```
To create an installer you then have to call maven with activated `create-installer` profile:
```
mvn clean install -Pcreate-installer
```
