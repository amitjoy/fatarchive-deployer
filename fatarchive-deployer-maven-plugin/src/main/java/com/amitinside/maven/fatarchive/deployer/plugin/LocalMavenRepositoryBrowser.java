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
import static com.amitinside.maven.fatarchive.deployer.plugin.Configurer.Params.POM_LOCATION;
import static com.amitinside.maven.fatarchive.deployer.plugin.Configurer.Params.SOURCE_DIRECTORY;
import static com.amitinside.maven.fatarchive.deployer.plugin.util.MojoHelper.getUserHome;
import static com.google.common.base.Preconditions.checkArgument;
import static java.io.File.separator;
import static org.apache.commons.io.FileUtils.copyFileToDirectory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.amitinside.maven.fatarchive.deployer.plugin.util.MojoHelper;

public final class LocalMavenRepositoryBrowser {

    private static final String MAVEN_LOCAL_REPOSITORY = ".m2" + separator + "repository";

    private final String pomLocation;
    private final File sourceLocation;
    private final List<String> extensionsToUnarchive;

    private LocalMavenRepositoryBrowser() {
        pomLocation = Configurer.INSTANCE.getAsString(POM_LOCATION);
        sourceLocation = (File) Configurer.INSTANCE.get(SOURCE_DIRECTORY);
        final String[] extensions = (String[]) Configurer.INSTANCE.get(EXTENSION_TO_UNARCHIVE);
        extensionsToUnarchive = Arrays.asList(extensions);

        checkArgument(!pomLocation.trim().isEmpty(), "POM Location cannot be empty");
    }

    public static LocalMavenRepositoryBrowser newInstance() {
        return new LocalMavenRepositoryBrowser();
    }

    public void copyArtefact() throws IOException, XmlPullParserException {
        final MavenXpp3Reader reader = new MavenXpp3Reader();
        final Model model = reader.read(new FileReader(pomLocation));

        for (final Dependency dep : model.getDependencies()) {
            final String type = dep.getType();
            if (extensionsToUnarchive.contains(type)) {
                final File dependency = getResource(model, dep);
                copyFileToDirectory(dependency, sourceLocation);
            }
        }
    }

    private static File getResource(final Model pom, final Dependency dependency) {
        final String groupId = dependency.getGroupId();
        final String artifactId = dependency.getArtifactId();
        final String versionProperty = dependency.getVersion();
        final String type = dependency.getType();
        final String version = MojoHelper.getVersion(pom.getProperties(), versionProperty);

        final String home = getUserHome();

        final StringBuilder locationBuilder = new StringBuilder();
        locationBuilder.append(home);
        locationBuilder.append(separator);
        locationBuilder.append(MAVEN_LOCAL_REPOSITORY);
        locationBuilder.append(separator);

        final String[] groupParts = groupId.split("\\.");

        for (final String groupPart : groupParts) {
            locationBuilder.append(groupPart);
            locationBuilder.append(separator);
        }
        locationBuilder.append(artifactId);
        locationBuilder.append(separator);
        locationBuilder.append(version);
        locationBuilder.append(separator);
        locationBuilder.append(artifactId);
        locationBuilder.append('-');
        locationBuilder.append(version);
        locationBuilder.append('.');
        locationBuilder.append(type);

        return new File(locationBuilder.toString());
    }

}
