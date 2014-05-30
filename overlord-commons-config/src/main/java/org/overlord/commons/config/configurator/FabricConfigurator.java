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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * Reads the configuration from a Fabric instance. It allows to read the
 * properties filtered.
 *
 * @author David Virgil Naranjo
 */

@Component(name = "The Fabric Configurator", immediate = true)
@Service(value = org.overlord.commons.config.configurator.Configurator.class)
public final class FabricConfigurator extends AbstractConfigurator {

    private FabricService fabricService;

    /**
     * Instantiates a new fabric configurator.
     */
    public FabricConfigurator() {
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
     * Gets the properties.
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
     * @see org.overlord.commons.config.configurator.AbstractConfigurator#setConfigurationFromServerApi(org.apache.commons.configuration.Configuration, java.lang.String, java.lang.String)
     */
    @Override
    protected boolean setConfigurationFromServerApi(Configuration config, String configFileOverride,
            String standardConfigFileName) {
        Map<String, String> props = null;
        if (StringUtils.isNotBlank(standardConfigFileName)) {
            props = this.getProperties(standardConfigFileName);
        } else {
            props = this.getProperties(configFileOverride);
        }

        if (props != null && !props.isEmpty()) {
            for (String key : props.keySet()) {
                config.addProperty(key, props.get(key));
            }
            return true;
        }

        return false;
    }

    /**
     * @see org.overlord.commons.config.configurator.AbstractConfigurator#getServerConfigUrl(java.lang.String)
     */
    @Override
    protected URL getServerConfigUrl(String standardConfigFileName) {
        URL url = null;
        try {
            url = new URL("profile:" + standardConfigFileName); //$NON-NLS-1$
        } catch (MalformedURLException ee) {

        }
        return url;
    }
}