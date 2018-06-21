/*******************************************************************************
 * Copyright (c) 2018 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.maven.fatarchive.deployer.plugin;

import static com.amitinside.maven.fatarchive.deployer.plugin.Configurer.Params.EXTENSION_TO_UNARCHIVE;
import static com.amitinside.maven.fatarchive.deployer.plugin.Configurer.Params.MAVEN_LOCATION;
import static com.amitinside.maven.fatarchive.deployer.plugin.Configurer.Params.POM_LOCATION;
import static com.amitinside.maven.fatarchive.deployer.plugin.Configurer.Params.SOURCE_DIRECTORY;
import static com.amitinside.maven.fatarchive.deployer.plugin.util.MojoHelper.getMavenEnvironmentVariable;
import static com.amitinside.maven.fatarchive.deployer.plugin.util.MojoHelper.getUserHome;
import static com.amitinside.maven.fatarchive.deployer.plugin.util.MojoHelper.replaceVariable;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "deployToNexus")
public class FatArchiveDeployerMojo extends AbstractMojo {

    @Component
    private MavenProject mavenProject;

    @Component
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Parameter
    private String mavenLocation;

    @Parameter
    private String[] extensionsToUnarchive;

    private File sourceDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String mavenHome = getMavenEnvironmentVariable();
        if (mavenHome == null) {
            try {
                resolveMavenLocation();
            } catch (final IOException e) {
                throw new MojoFailureException(e.getMessage());
            }
            mavenHome = mavenLocation;
            if (mavenHome.trim().isEmpty()) {
                //@formatter:off
                getLog().error("No Maven Environment Variable Found. "
                             + "Please set environment variable or "
                             + "set it explicitly as a configuration "
                             + "parameter");
                //@formatter:on
                return;
            }
        }
        mavenLocation = mavenHome;
        try {
            createSourceDirectory();
            storeConfugurationParameters();
            LocalMavenRepositoryBrowser.newInstance().copyArtefact();
            NexusDeployer.newInstance(mavenProject, mavenSession, pluginManager, getLog()).build();
        } catch (final Exception e) {
            throw new MojoFailureException(e.getMessage());
        }
    }

    private void resolveMavenLocation() throws IOException {
        mavenLocation = resolveLocation(mavenLocation).getCanonicalPath();
    }

    private File resolveLocation(String location) {
        final String userHomeVar = "${user.home}";
        final String baseDirVar = "${project.basedir}";
        if (location.contains(userHomeVar)) {
            location = StringUtils.replace(location, baseDirVar, replaceVariable(mavenProject, userHomeVar));
        }
        if (location.contains(baseDirVar)) {
            location = StringUtils.replace(location, baseDirVar, replaceVariable(mavenProject, baseDirVar));
        }
        File file = new File(location);
        if (!file.isAbsolute()) {
            file = new File(getUserHome(), location);
        }
        return file;
    }

    private void createSourceDirectory() {
        final String userDir = System.getProperty("user.dir");
        new File(userDir + File.separator + "fatarchive_build").mkdirs();
        sourceDirectory = new File(userDir + File.separator + "fatarchive_build");
    }

    private void storeConfugurationParameters() {
        final Configurer configurer = Configurer.INSTANCE;
        configurer.put(MAVEN_LOCATION, mavenLocation);
        configurer.put(POM_LOCATION, mavenProject.getFile().getPath());
        configurer.put(EXTENSION_TO_UNARCHIVE, extensionsToUnarchive);
        configurer.put(SOURCE_DIRECTORY, sourceDirectory);
    }

}
