/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.commons.config;

import java.util.Set;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.overlord.commons.config.configurator.Configurator;
import org.overlord.commons.config.vault.VaultLookup;
import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * Factory used to create instances of {@link Configuration}, used by various
 * overlord projects.
 *
 * @author eric.wittmann@redhat.com
 */
public class ConfigurationFactory {

    private static boolean globalLookupsRegistered = false;


    /**
     * Shared method used to locate and load configuration information from a number of
     * places, aggregated into a single {@link Configuration} instance.
     * @param configFileOverride
     * @param standardConfigFileName
     * @param refreshDelay
     * @param defaultConfigPath
     * @param defaultConfigLoader
     * @throws ConfigurationException
     */
    public static Configuration createConfig(String configFileOverride, String standardConfigFileName,
            Long refreshDelay, String defaultConfigPath, Class<?> defaultConfigLoader) {
        registerGlobalLookups();
        try {
            CompositeConfiguration config = new CompositeConfiguration();
            config.addConfiguration(new SystemPropertiesConfiguration());

            Set<Configurator> configurators = ServiceRegistryUtil.getServices(Configurator.class);
            if (!configurators.isEmpty()) {
                for (Configurator configurator : configurators) {
                    configurator.addConfiguration(config, configFileOverride, standardConfigFileName,
                            refreshDelay);
                }
            }

            if (defaultConfigPath != null) {
                config.addConfiguration(new PropertiesConfiguration(defaultConfigLoader.getResource(defaultConfigPath)));
            }
            return config;
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers global lookups for overlord configuration.  This allows custom
     * property interpolation to take place.
     */
    private synchronized static void registerGlobalLookups() {
        if (!globalLookupsRegistered) {
            ConfigurationInterpolator.registerGlobalLookup("vault", new VaultLookup()); //$NON-NLS-1$
            globalLookupsRegistered = true;
        }
    }


}
