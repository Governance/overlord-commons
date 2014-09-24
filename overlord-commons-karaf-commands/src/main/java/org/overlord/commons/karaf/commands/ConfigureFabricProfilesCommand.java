package org.overlord.commons.karaf.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.felix.gogo.commands.Command;

/**
 * Karaf console command for use within JBoss Fuse. It generates/overwrites the overlord-saml.keystore file and
 * configures all properties within the commons and sramp Fabric profiles.  Call it w/ the keystore password as
 * an argument.  Ex:
 *
 * overlord:configureFabric [password]
 *
 * Note that this uses the BouncyCastle library to encrypt the keystore file. It was not possible to directly use
 * sun.security as it does not support OSGi environments.
 *
 * @author David Virgil Naranjo
 */
@Command(scope = "overlord", name = "configureFabric")
public class ConfigureFabricProfilesCommand extends AbstractSamlKeystoreCommand {

    private static final String FABRIC_PROFILES_WINDOWS_DIR = "fabric\\import\\fabric\\configs\\versions\\1.0\\profiles"; //$NON-NLS-1$
    private static final String FABRIC_PROFILES_UNIX_DIR = "fabric/import/fabric/configs/versions/1.0/profiles"; //$NON-NLS-1$

    private static String FABRIC_PROFILES_DIR;
    private static String OVERLORD_COMMONS_PROFILE_PATH;

    static {
        if (File.separator.equals("/")) { //$NON-NLS-1$
            FABRIC_PROFILES_DIR = FABRIC_PROFILES_UNIX_DIR;
            OVERLORD_COMMONS_PROFILE_PATH = "overlord/commons.profile"; //$NON-NLS-1$
        } else {
            FABRIC_PROFILES_DIR = FABRIC_PROFILES_WINDOWS_DIR;
            OVERLORD_COMMONS_PROFILE_PATH = "overlord\\commons.profile"; //$NON-NLS-1$
        }
    }

    /**
     * Gets the fuse config path.
     *
     * @return the fuse config path
     */
    @Override
    protected String getConfigPath() {
        String karaf_home = System.getProperty("karaf.home"); //$NON-NLS-1$
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(karaf_home);
        if (!karaf_home.endsWith(File.separator)) {
            fuse_config_path.append(File.separator);
        }
        fuse_config_path.append(FABRIC_PROFILES_DIR).append(File.separator).append(OVERLORD_COMMONS_PROFILE_PATH).append(File.separator);
        return fuse_config_path.toString();
    }

    /**
     * Update the overlord properties with the new password introduced.
     *
     * @throws Exception
     *             the exception
     */
    @Override
    protected void updateOverlordProperties() throws Exception {
        String filePath = getOverlordPropertiesFilePath();
        File overlordFile = new File(filePath);
        if (overlordFile.exists()) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(filePath);
                Properties props = new Properties();
                props.load(in);
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(filePath);
                    if (props.contains(CommandConstants.OverlordProperties.OVERLORD_PORT)) {
                        props.remove(CommandConstants.OverlordProperties.OVERLORD_PORT);
                    }
                    props.setProperty(CommandConstants.OverlordProperties.OVERLORD_KEYSTORE_ALIAS_PASSWORD_KEY, keystorePassword);
                    props.setProperty(CommandConstants.OverlordProperties.OVERLORD_KEYSTORE_PASSWORD_KEY, keystorePassword);
                    props.store(out, null);

                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }

        } else {
            // Create a new overlord.properties file
            boolean created = overlordFile.createNewFile();
            if (created) {
                Properties props = new Properties();
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(filePath);
                    props.setProperty(CommandConstants.OverlordProperties.OVERLORD_BASE_URL,
                            CommandConstants.OverlordProperties.OVERLORD_BASE_URL_VALUE);
                    props.setProperty(CommandConstants.OverlordProperties.OVERLORD_SAML_KEYSTORE,
                            CommandConstants.OverlordProperties.OVERLORD_SAML_KEYSTORE_FABRIC_VALUE);
                    props.setProperty(CommandConstants.OverlordProperties.OVERLORD_SAML_ALIAS,
                            CommandConstants.OverlordProperties.OVERLORD_SAML_ALIAS_VALUE);
                    props.setProperty(CommandConstants.OverlordProperties.OVERLORD_KEYSTORE_ALIAS_PASSWORD_KEY, keystorePassword);
                    props.setProperty(CommandConstants.OverlordProperties.OVERLORD_KEYSTORE_PASSWORD_KEY, keystorePassword);
                    props.store(out, null);

                } finally {
                    out.close();
                }
            }
        }
    }

    /**
     * Gets the overlord properties file path.
     *
     * @return the overlord properties file path
     */
    private String getOverlordPropertiesFilePath() {
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(getConfigPath())
                .append(CommandConstants.OverlordProperties.OVERLORD_PROPERTIES_FILE_NAME);
        return fuse_config_path.toString();
    }
}
