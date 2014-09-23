package org.overlord.commons.karaf.commands.configure;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.felix.gogo.commands.Argument;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.overlord.commons.karaf.commands.CommandConstants;
import org.overlord.commons.karaf.commands.i18n.Messages;
import org.overlord.commons.karaf.commands.saml.GenerateSamlKeystoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Karaf console command for use within JBoss Fuse. It
 * generates/overwrites the overlord-saml.keystore file and configures all
 * properties within the commons and sramp Fabric profiles. Call it w/ the
 * keystore password as an argument.
 * 
 * Note that this uses the BouncyCastle library to encrypt the keystore file. It
 * was not possible to directly use sun.security as it does not support OSGi
 * environments.
 * 
 * @author David Virgil Naranjo
 */

abstract public class AbstractConfigureFabricCommand extends OsgiCommandSupport {

    private static String OVERLORD_COMMONS_PROFILE_PATH;

    private static final String FABRIC_PROFILES_WINDOWS_DIR = "fabric\\import\\fabric\\configs\\versions\\1.0\\profiles"; //$NON-NLS-1$
    private static final String FABRIC_PROFILES_UNIX_DIR = "fabric/import/fabric/configs/versions/1.0/profiles"; //$NON-NLS-1$

    protected static String FABRIC_PROFILES_DIR;

    @Argument(index = 0, name = "password", required = true, multiValued = false)
    protected String password = null;

    private static final Logger logger = LoggerFactory.getLogger(AbstractConfigureFabricCommand.class);
    static {
        if (File.separator.equals("/")) { //$NON-NLS-1$
            OVERLORD_COMMONS_PROFILE_PATH = "overlord/commons.profile"; //$NON-NLS-1$
        } else {
            OVERLORD_COMMONS_PROFILE_PATH = "overlord\\commons.profile"; //$NON-NLS-1$
        }
    }

    static {
        if (File.separator.equals("/")) { //$NON-NLS-1$
            FABRIC_PROFILES_DIR = FABRIC_PROFILES_UNIX_DIR;
        } else {
            FABRIC_PROFILES_DIR = FABRIC_PROFILES_WINDOWS_DIR;
        }
    }


    protected String getFabricProfilesePath() {
        String karaf_home = System.getProperty("karaf.home"); //$NON-NLS-1$
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(karaf_home);
        if (!karaf_home.endsWith(File.separator)) {
            fuse_config_path.append(File.separator);
        }
        fuse_config_path.append(FABRIC_PROFILES_DIR).append(File.separator);
        return fuse_config_path.toString();
    }

    @Override
    protected Object doExecute() throws Exception {
        String fuse_config_path = getFabricProfilePath();
        String file = fuse_config_path + CommandConstants.OverlordProperties.FILE_KEYSTORE_NAME;
        File keystore = new File(file);
        if (!keystore.exists()) {
            logger.info(Messages.getString("generate.saml.keystore.command.correctly.begin")); //$NON-NLS-1$
            GenerateSamlKeystoreUtil util = new GenerateSamlKeystoreUtil();
            util.generate(password, keystore);
            // Once the keystore file is generated the references to the saml
            // password existing in the overlord.properties file should be
            // updated.
            updateOverlordProperties();
            logger.info(Messages.getString("generate.saml.keystore.command.correctly.created")); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Gets the fabric overlord profile path
     *
     * @return the fuse config path
     */
    public String getFabricProfilePath() {
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(getFabricProfilesePath()).append(OVERLORD_COMMONS_PROFILE_PATH).append(File.separator);
        return fuse_config_path.toString();
    }

    /**
     * Update the overlord properties with the new password introduced.
     *
     * @throws Exception
     *             the exception
     */
    protected void updateOverlordProperties() throws Exception {
        String filePath = getOverlordPropertiesFilePath();

        Properties props = new Properties();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            props.setProperty(CommandConstants.OverlordProperties.OVERLORD_BASE_URL, CommandConstants.OverlordProperties.OVERLORD_BASE_URL_VALUE);
            props.setProperty(CommandConstants.OverlordProperties.OVERLORD_SAML_KEYSTORE,
                    CommandConstants.OverlordProperties.OVERLORD_SAML_KEYSTORE_FABRIC_VALUE);
            props.setProperty(CommandConstants.OverlordProperties.OVERLORD_SAML_ALIAS, CommandConstants.OverlordProperties.OVERLORD_SAML_ALIAS_VALUE);
            props.setProperty(CommandConstants.OverlordProperties.OVERLORD_KEYSTORE_ALIAS_PASSWORD_KEY, password);
            props.setProperty(CommandConstants.OverlordProperties.OVERLORD_KEYSTORE_PASSWORD_KEY, password);
            props.store(out, null);

        } finally {
            out.close();

        }
    }

    /**
     * Gets the overlord properties file path.
     *
     * @return the overlord properties file path
     */
    private String getOverlordPropertiesFilePath() {
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(getFabricProfilePath())
                .append(CommandConstants.OverlordProperties.OVERLORD_PROPERTIES_FILE_NAME);
        return fuse_config_path.toString();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
