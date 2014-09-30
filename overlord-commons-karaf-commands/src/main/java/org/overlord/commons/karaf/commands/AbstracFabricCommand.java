package org.overlord.commons.karaf.commands;

import java.io.File;

import org.apache.karaf.shell.console.OsgiCommandSupport;

public abstract class AbstracFabricCommand extends OsgiCommandSupport {

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
