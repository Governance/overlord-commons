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
import java.net.URL;

/**
 * Reads the configuration from a Tomcat instance.
 *
 * @author David Virgil Naranjo
 */
public class TomcatConfigurator extends AbstractPropertiesFileConfigurator {
    
    /**
     * Constructor.
     */
    public TomcatConfigurator() {
    }
    
    /**
     * @see org.overlord.commons.config.configurator.Configurator#accept()
     */
    @Override
    public boolean accept() {
        String tomcatDir = System.getProperty("catalina.home"); //$NON-NLS-1$
        return tomcatDir != null;
    }
    
    /**
     * @see org.overlord.commons.config.configurator.AbstractPropertiesFileConfigurator#findConfigUrl(java.lang.String)
     */
    @Override
    protected URL findConfigUrl(String configName) {
        String tomcatDir = System.getProperty("catalina.home"); //$NON-NLS-1$
        if (tomcatDir != null) {
            File dirFile = new File(tomcatDir, "conf"); //$NON-NLS-1$
            return findConfigUrlInDirectory(dirFile, configName);
        }
        return null;
    }

}