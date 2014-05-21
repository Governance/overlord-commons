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

/**
 * Reads the configuration from the User Home directory.
 * 
 * @author David Virgil Naranjo
 */
public class UserHomeConfigurator extends AbstractConfigurator{

    /*
     * (non-Javadoc)
     * 
     * @see org.overlord.commons.config.configurator.AbstractConfigurator#
     * getServerConfigUrl(java.lang.String)
     */
    @Override
    protected URL getServerConfigUrl(String standardConfigFileName) throws MalformedURLException {
        String userHomeDir = System.getProperty("user.home"); //$NON-NLS-1$
        if (userHomeDir != null) {
            File dirFile = new File(userHomeDir);
            if (dirFile.isDirectory()) {
                File cfile = new File(dirFile, standardConfigFileName);
                if (cfile.isFile())
                    return cfile.toURI().toURL();
            }
        }
        return null;
    }
}
