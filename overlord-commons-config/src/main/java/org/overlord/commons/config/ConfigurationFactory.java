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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.overlord.commons.config.vault.VaultLookup;

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
            URL url = findConfig(configFileOverride, standardConfigFileName);
            if (url != null) {
                PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(url);
                FileChangedReloadingStrategy fileChangedReloadingStrategy = new FileChangedReloadingStrategy();
                fileChangedReloadingStrategy.setRefreshDelay(refreshDelay);
                propertiesConfiguration.setReloadingStrategy(fileChangedReloadingStrategy);
                config.addConfiguration(propertiesConfiguration);
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
            ConfigurationInterpolator.registerGlobalLookup("vault", new VaultLookup());
            globalLookupsRegistered = true;
        }
    }

    /**
     * Try to find the configuration file. This will look for the config file in a number of places.
     *
     * @param configFileOverride
     * @param standardConfigFileName
     */
    private static URL findConfig(String configFileOverride, String standardConfigFileName) {
        // If a config file was given (passed in to this method) then try to
        // find it.  If not, then look for a 'standard' config file.
        try {
            if (configFileOverride != null) {
                // Check on the classpath
                URL fromClasspath = Thread.currentThread().getContextClassLoader().getResource(configFileOverride);
                if (fromClasspath != null)
                    return fromClasspath;

                // Check on the file system
                File file = new File(configFileOverride);
                if (file.isFile())
                    return file.toURI().toURL();
            } else {
                // Check the current user's home directory
                String userHomeDir = System.getProperty("user.home"); //$NON-NLS-1$
                if (userHomeDir != null) {
                    File dirFile = new File(userHomeDir);
                    if (dirFile.isDirectory()) {
                        File cfile = new File(dirFile, standardConfigFileName);
                        if (cfile.isFile())
                            return cfile.toURI().toURL();
                    }
                }

                // Next, check for JBoss
                String jbossConfigDir = System.getProperty("jboss.server.config.dir"); //$NON-NLS-1$
                if (jbossConfigDir != null) {
                    File dirFile = new File(jbossConfigDir);
                    if (dirFile.isDirectory()) {
                        File cfile = new File(dirFile, standardConfigFileName);
                        if (cfile.isFile())
                            return cfile.toURI().toURL();
                    }
                }
                String jbossConfigUrl = System.getProperty("jboss.server.config.url"); //$NON-NLS-1$
                if (jbossConfigUrl != null) {
                    File dirFile = new File(jbossConfigUrl);
                    if (dirFile.isDirectory()) {
                        File cfile = new File(dirFile, standardConfigFileName);
                        if (cfile.isFile())
                            return cfile.toURI().toURL();
                    }
                }
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
