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
import static com.amitinside.maven.fatarchive.deployer.plugin.Configurer.Params.SOURCE_DIRECTORY;
import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public final class NexusDeployer {

    private final File sourceLocation;
    private final String[] extensionsToUnarchive;
    private final MavenProject mavenProject;
    private final MavenSession mavenSession;
    private final BuildPluginManager pluginManager;
    private final Log log;

    private final Map<File, GAV> artifacts = Maps.newHashMap();

    private NexusDeployer(final MavenProject mavenProject, final MavenSession mavenSession,
            final BuildPluginManager pluginManager, final Log log) {
        sourceLocation = (File) Configurer.INSTANCE.get(SOURCE_DIRECTORY);
        extensionsToUnarchive = (String[]) Configurer.INSTANCE.get(EXTENSION_TO_UNARCHIVE);
        this.mavenProject = mavenProject;
        this.mavenSession = mavenSession;
        this.pluginManager = pluginManager;
        this.log = log;
    }

    public static NexusDeployer newInstance(final MavenProject mavenProject, final MavenSession mavenSession,
            final BuildPluginManager pluginManager, final Log log) {
        return new NexusDeployer(mavenProject, mavenSession, pluginManager, log);
    }

    public void build() throws Exception {
        extractFatArchives();
        prepareGAV();
        deployContainedArchives();
        outputGAVs();
        FileUtils.deleteDirectory(sourceLocation);
    }

    private void deployContainedArchives() {
        artifacts.forEach((k, v) -> executeDeployFileMavenMojo(k));
    }

    private void extractFatArchives() throws IOException {
        try (Stream<Path> paths = Files.walk(sourceLocation.toPath())) {
            //@formatter:off
            paths.filter(Files::isRegularFile)
                 .map(Path::toFile)
                 .filter(f -> Arrays.stream(extensionsToUnarchive)
                                    .anyMatch(e -> f.getName().endsWith(e)))
                 .forEach(this::extract);
           //@formatter:on
        }
    }

    private void extract(final File file) {
        try {
            final ZipFile zipFile = new ZipFile(file);
            zipFile.extractAll(sourceLocation.getPath());
        } catch (final ZipException e) {
            // suppress due to the usage in stream
        }
    }

    private void prepareGAV() throws IOException {
        try (Stream<Path> paths = Files.walk(sourceLocation.toPath())) {
            //@formatter:off
            paths.filter(Files::isRegularFile)
                 .map(Path::toFile)
                 .filter(f -> Arrays.stream(extensionsToUnarchive)
                                    .noneMatch(e -> f.getName().endsWith(e)))
                 .forEach(this::prepare);
           //@formatter:on
        }
    }

    private void prepare(final File file) {
        try (final JarFile jar = new JarFile(file)) {
            final Manifest manifest = jar.getManifest();
            final Attributes attrs = manifest.getMainAttributes();
            String bsn = attrs.getValue("Bundle-SymbolicName");
            if (bsn.contains(";")) {
                bsn = bsn.substring(0, bsn.indexOf(';') - 1);
            }
            final String version = attrs.getValue("Bundle-Version");
            final GAV gav = new GAV();
            gav.groupID = "com.thirdparty";
            gav.version = version;
            gav.artifactID = bsn;
            artifacts.put(file, gav);
        } catch (final IOException e) {
            log.error("File Not Found");
        }
    }

    private void outputGAVs() throws IOException {
        final StringBuilder builder = new StringBuilder();
        artifacts.forEach((k, v) -> {
            builder.append(System.lineSeparator());
            builder.append(v.groupID + ":" + v.artifactID + ":" + v.version);
        });
        final File file = new File(sourceLocation + "composites.txt");
        file.createNewFile();
        FileUtils.writeStringToFile(file, builder.toString(), StandardCharsets.UTF_8);
    }

    private static class GAV {
        String groupID;
        String artifactID;
        String version;
    }

    private void executeDeployFileMavenMojo(final File file) {
        final GAV gav = artifacts.get(file);
        log.info("Deploying Artifact => " + file.getName());
        //@formatter:off
        try {
            executeMojo(
                    plugin(
                        groupId("org.apache.maven.plugins"),
                        artifactId("maven-install-plugin"),
                        version("2.5.2")
                    ),
                    goal("install-file"),
                    configuration(
                        element(name("groupId"), gav.groupID),
                        element(name("artifactId"), gav.artifactID),
                        element(name("version"), gav.version),
                        element(name("packaging"), "jar"),
                        element(name("file"), file.getAbsolutePath()),
                        element(name("generatePom"), Boolean.toString(false))
                    ),
                    executionEnvironment(
                        mavenProject,
                        mavenSession,
                        pluginManager
                    )
                );
        } catch (final MojoExecutionException e) {
            log.error(Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e);
        }
        //@formatter:on
    }

}
