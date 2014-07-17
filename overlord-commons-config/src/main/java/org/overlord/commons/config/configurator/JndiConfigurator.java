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

package org.overlord.commons.config.configurator;

import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;

/**
 * Looks for configuration information in JNDI.
 *
 * @author eric.wittmann@redhat.com
 */
public class JndiConfigurator implements Configurator {
    
    /**
     * Constructor.
     */
    public JndiConfigurator() {
    }

    /**
     * @see org.overlord.commons.config.configurator.Configurator#accept()
     */
    @Override
    public boolean accept() {
        try {
            new InitialContext();
            return true;
        } catch (NamingException e) {
            return false;
        }
    }

    /**
     * @see org.overlord.commons.config.configurator.Configurator#provideConfiguration(java.lang.String, java.lang.Long)
     */
    @Override
    public Configuration provideConfiguration(String configName, Long refreshDelay)
            throws ConfigurationException {
        if (configName.endsWith(".properties")) { //$NON-NLS-1$
            configName = configName.substring(0, configName.lastIndexOf(".properties")); //$NON-NLS-1$
        }
        
        try {
            Context ctx = new InitialContext();
            @SuppressWarnings("unchecked")
            Map<String, String> properties = (Map<String, String>) ctx.lookup("java:/comp/env/overlord-config/" + configName); //$NON-NLS-1$
            return new MapConfiguration(properties);
        } catch (Exception e) {
            // This is usually OK.  TODO log this exception
            return null;
        }
    }

}
