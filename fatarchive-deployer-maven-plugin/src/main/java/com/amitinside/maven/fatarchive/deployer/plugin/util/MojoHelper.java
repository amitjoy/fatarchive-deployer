/*******************************************************************************
 * Copyright (c) 2018 Amit Kumar Mondal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.amitinside.maven.fatarchive.deployer.plugin.util;

import static com.google.common.base.Preconditions.*;

import java.util.Map;
import java.util.Properties;

import org.apache.maven.project.MavenProject;

import com.google.common.collect.Maps;

public final class MojoHelper {

    private MojoHelper() {
        throw new IllegalAccessError("Cannot instantiate");
    }

    public static String getMavenEnvironmentVariable() {
        if (System.getenv("M2_HOME") != null) {
            return System.getenv("M2_HOME");
        } else if (System.getenv("MAVEN_HOME") != null) {
            return System.getenv("MAVEN_HOME");
        } else if (System.getenv("M3_HOME") != null) {
            return System.getenv("M3_HOME");
        } else if (System.getenv("MVN_HOME") != null) {
            return System.getenv("MVN_HOME");
        } else if (System.getProperty("maven.home") != null) {
            return System.getProperty("maven.home");
        } else {
            return null;
        }
    }

    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    public static String replaceVariable(final MavenProject project, String variable) {
        checkNotNull(project, "Maven Project instance cannot be null");
        checkNotNull(variable, "Variable cannot be null");
        checkArgument(!variable.isEmpty(), "Variable cannot be empty");

        variable = variable.substring(2, variable.length() - 1);
        if ("${user.home}".equalsIgnoreCase(variable)) {
            return getUserHome();
        }
        if ("${project.basedir}".equalsIgnoreCase(variable)) {
            return project.getBasedir().getPath();
        }
        return null;
    }

    public static String getVersion(final Properties properties, final String property) {
        checkNotNull(properties, "Properties instance cannot be null");
        checkNotNull(property, "Property cannot be null");

        if (property.indexOf('$') == -1) {
            return property;
        }
        final Map<String, String> props = Maps.fromProperties(properties);
        final String parsedProperty = property.substring(2, property.length() - 1);
        return props.get(parsedProperty);
    }

}
