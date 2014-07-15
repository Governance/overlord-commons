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

import io.fabric8.api.FabricService;
import io.fabric8.api.Profile;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * Reads the configuration from a Fuse instance.  Supports both normal Fuse and also
 * Fuse with Fabric8.
 *
 * @author David Virgil Naranjo
 */
@Component(name = "Fuse Configurator", immediate = true)
@Service(value = org.overlord.commons.config.configurator.Configurator.class)
public class FuseConfigurator extends AbstractConfigurator {

    private FabricService fabricService;

    /**
     * Instantiates a new fuse/fabric8 configurator.
     */
    public FuseConfigurator() {
    }

    /**
     * Lazy load the fabric service.
     */
    private FabricService getFabricService() {
        if (fabricService == null) {
            try {
                fabricService = ServiceRegistryUtil.getSingleService(FabricService.class);
            } catch (Throwable t) {
            }
        }
        return fabricService;
    }

    /**
     * @see org.overlord.commons.config.configurator.Configurator#accept()
     */
    @Override
    public boolean accept() {
        String karafDir = System.getProperty("karaf.home"); //$NON-NLS-1$
        return karafDir != null || getFabricService() != null;
    }
    
    /**
     * @see org.overlord.commons.config.configurator.AbstractConfigurator#provideConfiguration(java.lang.String, java.lang.Long)
     */
    @Override
    public Configuration provideConfiguration(String configName, Long refreshDelay)
            throws ConfigurationException {
        if (getFabricService() != null) {
            Map<String, String> properties = getProperties(configName);
            return new MapConfiguration(properties);
        } else {
            return super.provideConfiguration(configName, refreshDelay);
        }
    }

    /**
     * Gets the properties from Fabric8.
     * 
     * @param urlFile
     * @return the properties
     */
    protected Map<String, String> getProperties(String urlFile) {
        if (getFabricService() != null && getFabricService().getCurrentContainer() != null
                && getFabricService().getCurrentContainer().getOverlayProfile() != null) {

            Profile profile = getFabricService().getCurrentContainer().getOverlayProfile();
            String file_name = ""; //$NON-NLS-1$
            if (urlFile.contains(".")) { //$NON-NLS-1$
                file_name = urlFile.substring(0, urlFile.lastIndexOf(".")); //$NON-NLS-1$
            } else {
                file_name = urlFile;
            }
            Map<String, String> toReturn = profile.getConfiguration(file_name);
            return toReturn;
        }
        return null;
    }

    /**
     * @see org.overlord.commons.config.configurator.AbstractConfigurator#findConfigUrl(java.lang.String)
     */
    @Override
    protected URL findConfigUrl(String configName) {
        String karafDir = System.getProperty("karaf.home"); //$NON-NLS-1$
        if (karafDir != null) {
            File dirFile = new File(karafDir, "etc"); //$NON-NLS-1$
            return findConfigUrlInDirectory(dirFile, configName);
        }
        return null;
    }

}