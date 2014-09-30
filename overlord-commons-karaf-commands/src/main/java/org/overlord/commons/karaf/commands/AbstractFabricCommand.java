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
package org.overlord.commons.karaf.commands;

import java.io.File;

import org.apache.karaf.shell.console.OsgiCommandSupport;

public abstract class AbstractFabricCommand extends OsgiCommandSupport {

    private static final String FABRIC_PROFILES_WINDOWS_DIR = "fabric\\import\\fabric\\configs\\versions\\1.0\\profiles"; //$NON-NLS-1$
    private static final String FABRIC_PROFILES_UNIX_DIR = "fabric/import/fabric/configs/versions/1.0/profiles"; //$NON-NLS-1$

    protected static String FABRIC_PROFILES_DIR;
    static {
        if (File.separator.equals("/")) { //$NON-NLS-1$
            FABRIC_PROFILES_DIR = FABRIC_PROFILES_UNIX_DIR;
        } else {
            FABRIC_PROFILES_DIR = FABRIC_PROFILES_WINDOWS_DIR;
        }
    }

    protected String getFabricProfilesPath() {
        String karaf_home = System.getProperty("karaf.home"); //$NON-NLS-1$
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(karaf_home);
        if (!karaf_home.endsWith(File.separator)) {
            fuse_config_path.append(File.separator);
        }
        fuse_config_path.append(FABRIC_PROFILES_DIR).append(File.separator);
        return fuse_config_path.toString();
    }

}
