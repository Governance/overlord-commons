package org.overlord.commons.karaf.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.overlord.commons.karaf.commands.i18n.Messages;
import org.overlord.commons.karaf.commands.saml.GenerateSamlKeystoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jboss Aesh command included in the jboss fuse command console. It
 * generates/ovewrite the overlord-saml.keystore file in the etc folder, using
 * as keypass the param. It uses the library bouncycastle to encrypt the
 * keystore file. It was not possible to use the sun.security package because it
 * was not supported in osgi environments.
 *
 * @author David Virgil Naranjo
 */
@Command(scope = "overlord", name = "configureFabric", description = "Generates a keystore file and the overlord.properties in the overlord.commons profile.")
public class ConfigureFabricProfilesCommand extends OsgiCommandSupport {

    private static final Logger logger = LoggerFactory.getLogger(ConfigureFabricProfilesCommand.class);

    @Argument(index = 0, name = "password", description = "The command argument", required = true, multiValued = false)
    String password = null;

    public static final String FABRIC_PROFILES_WINDOWS_DIR = "fabric\\import\\fabric\\configs\\versions\\1.0\\profiles";
    public static final String FABRIC_PROFILES_UNIX_DIR = "fabric/import/fabric/configs/versions/1.0/profiles";

    public static String FABRIC_PROFILES_DIR;
    public static String OVERLORD_COMMONS_PROFILE_PATH;

    static {
        if (File.separator.equals("/")) {
            FABRIC_PROFILES_DIR = FABRIC_PROFILES_UNIX_DIR;
            OVERLORD_COMMONS_PROFILE_PATH = "overlord/commons.profile";
        } else {
            FABRIC_PROFILES_DIR = FABRIC_PROFILES_WINDOWS_DIR;
            OVERLORD_COMMONS_PROFILE_PATH = "overlord\\commons.profile";
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.karaf.shell.console.AbstractAction#doExecute()
     */
    @Override
    protected Object doExecute() throws Exception {
        String fuse_config_path = getOverlordCommonsProfilesPath();
        String file = fuse_config_path + CommandConstants.OverlordProperties.FILE_KEYSTORE_NAME;
        logger.info(Messages.getString("generate.saml.keystore.command.correctly.begin"));
        // This 3 lines generate/overwrite the keystore file.
        File keystore = new File(file);
        GenerateSamlKeystoreUtil util = new GenerateSamlKeystoreUtil();
        util.generate(password, keystore);
        // Once the keystore file is generated the references to the saml
        // password existing in the overlord.properties file should be updated.
        updateOverlordProperties();
        logger.info(Messages.getString("generate.saml.keystore.command.correctly.created"));
        return null;
    }

    /**
     * Gets the fuse config path.
     *
     * @return the fuse config path
     */
    private String getOverlordCommonsProfilesPath() {
        String karaf_home = System.getProperty("karaf.home");
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(karaf_home);
        if (!karaf_home.endsWith(File.separator)) {
            fuse_config_path.append(File.separator);
        }
        fuse_config_path.append(FABRIC_PROFILES_DIR).append(File.separator).append(OVERLORD_COMMONS_PROFILE_PATH).append(File.separator);
        return fuse_config_path.toString();
    }

    /**
     * Gets the overlord properties file path.
     *
     * @return the overlord properties file path
     */
    private String getOverlordPropertiesFilePath() {
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(getOverlordCommonsProfilesPath())
                .append(CommandConstants.OverlordProperties.OVERLORD_PROPERTIES_FILE_NAME);
        return fuse_config_path.toString();
    }

    /**
     * Update the overlord properties with the new password introduced.
     *
     * @throws Exception
     *             the exception
     */
    private void updateOverlordProperties() throws Exception {
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
                    props.setProperty(CommandConstants.OverlordProperties.OVERLORD_KEYSTORE_ALIAS_PASSWORD_KEY, password);
                    props.setProperty(CommandConstants.OverlordProperties.OVERLORD_KEYSTORE_PASSWORD_KEY, password);
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
                    props.setProperty(CommandConstants.OverlordProperties.OVERLORD_KEYSTORE_ALIAS_PASSWORD_KEY, password);
                    props.setProperty(CommandConstants.OverlordProperties.OVERLORD_KEYSTORE_PASSWORD_KEY, password);
                    props.store(out, null);

                } finally {
                    out.close();
                }
            }
        }
    }
}
