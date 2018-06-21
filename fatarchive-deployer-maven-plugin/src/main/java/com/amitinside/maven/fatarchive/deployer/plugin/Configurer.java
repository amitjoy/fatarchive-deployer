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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.EnumMap;

import com.google.common.collect.Maps;

public enum Configurer {

    INSTANCE;

    private final EnumMap<Params, Object> configuration = Maps.newEnumMap(Params.class);

    public void put(final Params key, final Object value) {
        checkNotNull(key, "Key cannot be null");
        checkNotNull(value, "Value cannot be null");

        configuration.put(key, value);
    }

    /**
     * Returns the associated value as string
     *
     * @param key the key to check
     * @return the associated value if found or empty
     * @throws NullPointerException if the key is {@code null}
     */
    public String getAsString(final Params key) {
        checkNotNull(key, "Key cannot be null");

        final Object object = configuration.get(key);
        if (object instanceof String) {
            return String.valueOf(object);
        }
        return "";
    }

    public Object get(final Params key) {
        return configuration.get(key);
    }

    public enum Params {
        MAVEN_LOCATION,
        POM_LOCATION,
        EXTENSION_TO_UNARCHIVE,
        SOURCE_DIRECTORY,
        NEXUS_URL,
        REPO_ID
    }

}
