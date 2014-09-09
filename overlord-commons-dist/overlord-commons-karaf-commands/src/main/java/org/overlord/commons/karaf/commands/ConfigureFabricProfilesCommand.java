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
public class ConfigureFabricProfilesCommand extends OsgiCommandSupport {

    private static final Logger logger = LoggerFactory.getLogger(ConfigureFabricProfilesCommand.class);

    private static final String FABRIC_PROFILES_WINDOWS_DIR = "fabric\\import\\fabric\\configs\\versions\\1.0\\profiles"; //$NON-NLS-1$
    private static final String FABRIC_PROFILES_UNIX_DIR = "fabric/import/fabric/configs/versions/1.0/profiles"; //$NON-NLS-1$

    private static String FABRIC_PROFILES_DIR;
    private static String OVERLORD_COMMONS_PROFILE_PATH;
    private static String SRAMP_PROFILE_PATH;

    @Argument(index = 0, name = "password", required = true, multiValued = false)
    protected String password = null;

    @Argument(index = 1, name = "jmsUser", required = false, multiValued = false)
    protected String jmsUser = null;

    @Argument(index = 2, name = "jmsPassword", required = false, multiValued = false)
    protected String jmsPassword = null;

    static {
        if (File.separator.equals("/")) { //$NON-NLS-1$
            FABRIC_PROFILES_DIR = FABRIC_PROFILES_UNIX_DIR;
            OVERLORD_COMMONS_PROFILE_PATH = "overlord/commons.profile"; //$NON-NLS-1$
            SRAMP_PROFILE_PATH = "overlord/sramp.profile"; //$NON-NLS-1$
        } else {
            FABRIC_PROFILES_DIR = FABRIC_PROFILES_WINDOWS_DIR;
            OVERLORD_COMMONS_PROFILE_PATH = "overlord\\commons.profile"; //$NON-NLS-1$
            SRAMP_PROFILE_PATH = "overlord/sramp.profile"; //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.karaf.shell.console.AbstractAction#doExecute()
     */
    @Override
    protected Object doExecute() throws Exception {
        String fuse_config_path = getConfigPath(OVERLORD_COMMONS_PROFILE_PATH);
        String file = fuse_config_path + CommandConstants.OverlordProperties.FILE_KEYSTORE_NAME;
        logger.info(Messages.getString("generate.saml.keystore.command.correctly.begin")); //$NON-NLS-1$
        // This 3 lines generate/overwrite the keystore file.
        File keystore = new File(file);
        GenerateSamlKeystoreUtil util = new GenerateSamlKeystoreUtil();
        util.generate(password, keystore);
        // Once the keystore file is generated the references to the saml
        // password existing in the overlord.properties file should be updated.
        updateOverlordProperties();
        updateSrampProperties();
        logger.info(Messages.getString("generate.saml.keystore.command.correctly.created")); //$NON-NLS-1$
        return null;
    }

    /**
     * Gets the fuse config path.
     *
     * @return the fuse config path
     */
    protected String getConfigPath(String profile) {
        String karaf_home = System.getProperty("karaf.home"); //$NON-NLS-1$
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(karaf_home);
        if (!karaf_home.endsWith(File.separator)) {
            fuse_config_path.append(File.separator);
        }
        fuse_config_path.append(FABRIC_PROFILES_DIR).append(File.separator).append(profile).append(File.separator);
        return fuse_config_path.toString();
    }

    protected void updateSrampProperties() throws Exception {
        String filePath = getSrampPropertiesFilePath();
        File srampFile = new File(filePath);
        if (srampFile.exists()) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(filePath);
                Properties props = new Properties();
                props.load(in);
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(filePath);
                    if (jmsPassword != null && !jmsPassword.isEmpty()) {
                        props.setProperty(CommandConstants.SrampProperties.SRAMP_EVENTS_JMS_PASSWORD, jmsPassword);
                    }
                    if (jmsUser != null && !jmsUser.isEmpty()) {
                        props.setProperty(CommandConstants.SrampProperties.SRAMP_EVENTS_JMS_USER, jmsUser);
                    }

                    props.store(out, null);

                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }

        }
    }

    /**
     * Update the overlord properties with the new password introduced.
     *
     * @throws Exception
     *             the exception
     */
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

    /**
     * Gets the overlord properties file path.
     *
     * @return the overlord properties file path
     */
    private String getOverlordPropertiesFilePath() {
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(getConfigPath(OVERLORD_COMMONS_PROFILE_PATH))
                .append(CommandConstants.OverlordProperties.OVERLORD_PROPERTIES_FILE_NAME);
        return fuse_config_path.toString();
    }

    /**
     * Gets the overlord properties file path.
     *
     * @return the overlord properties file path
     */
    private String getSrampPropertiesFilePath() {
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(getConfigPath(SRAMP_PROFILE_PATH)).append(CommandConstants.SrampProperties.SRAMP_PROPERTIES_FILE_NAME);
        return fuse_config_path.toString();
    }
}
