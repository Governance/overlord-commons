/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.commons.config.configurator;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;

/**
 * Allows to read the Configuration properties from a specific server instance.
 *
 * @author David Virgil Naranjo
 */
public interface Configurator {

    /**
     * Adds the configuration.
     *
     * @param config
     *            the config
     * @param configFileOverride
     *            the config file override
     * @param standardConfigFileName
     *            the standard config file name
     * @param refreshDelay
     *            the refresh delay
     * @throws ConfigurationException
     *             the configuration exception
     */
    public void addConfiguration(CompositeConfiguration config, String configFileOverride,
            String standardConfigFileName,
            Long refreshDelay) throws ConfigurationException;
}
