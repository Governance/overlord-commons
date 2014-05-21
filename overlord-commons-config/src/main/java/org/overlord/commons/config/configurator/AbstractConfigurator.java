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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

/**
 * Contains the main configurator code. It tries to read the configuration from
 * the server api, if it is implemented. If not, it reads the configuration from
 * a file located in the server installation path. This file location changes
 * depends on the configurator.
 * 
 * @author David Virgil Naranjo
 */
public abstract class AbstractConfigurator implements Configurator {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.overlord.commons.config.configurator.Configurator#addConfiguration
     * (org.apache.commons.configuration.CompositeConfiguration,
     * java.lang.String, java.lang.String, java.lang.Long)
     */
    @Override
    public void addConfiguration(CompositeConfiguration config, String configFileOverride,
            String standardConfigFileName,
            Long refreshDelay) throws ConfigurationException {

        if (!setConfigurationFromServerApi(config, configFileOverride, standardConfigFileName)) {
            URL url = findConfig(configFileOverride, standardConfigFileName);
            if (url != null) {
                PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(url);
                config.addConfiguration(propertiesConfiguration);
                FileChangedReloadingStrategy fileChangedReloadingStrategy = new FileChangedReloadingStrategy();
                fileChangedReloadingStrategy.setRefreshDelay(refreshDelay);
                propertiesConfiguration.setReloadingStrategy(fileChangedReloadingStrategy);
            }
        }

    }



    /**
     * Gets the server config url.
     *
     * @param standardConfigFileName
     *            the standard config file name
     * @return the server config url
     * @throws MalformedURLException
     *             the malformed url exception
     */
    protected abstract URL getServerConfigUrl(String standardConfigFileName) throws MalformedURLException;

    /**
     * Sets the configuration from server api.
     *
     * @param config
     *            the config
     * @param configFileOverride
     *            the config file override
     * @param standardConfigFileName
     *            the standard config file name
     * @return true, if successful
     */
    protected boolean setConfigurationFromServerApi(Configuration config, String configFileOverride,
            String standardConfigFileName) {
        return false;
    }

    /**
     * Try to find the configuration file. This will look for the config file in
     * a number of places.
     *
     * @param configFileOverride
     *            the config file override
     * @param standardConfigFileName
     *            the standard config file name
     * @return the url
     */
    protected URL findConfig(String configFileOverride, String standardConfigFileName) {
        try {
            if (configFileOverride != null) {
                // Check on the classpath
                URL fromClasspath = Thread.currentThread().getContextClassLoader()
                        .getResource(configFileOverride);
                if (fromClasspath != null)
                    return fromClasspath;

                // Check on the file system
                File file = new File(configFileOverride);
                if (file.isFile())
                    return file.toURI().toURL();

            }
            else{
                return getServerConfigUrl(standardConfigFileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }




}
