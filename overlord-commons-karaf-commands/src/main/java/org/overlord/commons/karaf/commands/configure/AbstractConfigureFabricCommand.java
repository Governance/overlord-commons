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
package org.overlord.commons.karaf.commands.configure;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.felix.gogo.commands.Argument;
import org.overlord.commons.codec.AesEncrypter;
import org.overlord.commons.karaf.commands.AbstractFabricCommand;
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

abstract public class AbstractConfigureFabricCommand extends AbstractFabricCommand {

    private static String OVERLORD_COMMONS_PROFILE_PATH;


    @Argument(index = 0, name = "password", required = true, multiValued = false)
    protected String password = null;

    boolean allowedPasswordOverwrite;

    private static final Logger logger = LoggerFactory.getLogger(AbstractConfigureFabricCommand.class);
    static {
        if (File.separator.equals("/")) { //$NON-NLS-1$
            OVERLORD_COMMONS_PROFILE_PATH = "overlord/commons.profile"; //$NON-NLS-1$
        } else {
            OVERLORD_COMMONS_PROFILE_PATH = "overlord\\commons.profile"; //$NON-NLS-1$
        }
    }



    public AbstractConfigureFabricCommand() {
        allowedPasswordOverwrite = false;
    }

    @Override
    protected Object doExecute() throws Exception {
        String fuse_config_path = getOverlordProfilePath();
        String file = fuse_config_path + CommandConstants.OverlordProperties.FILE_KEYSTORE_NAME;
        File keystore = new File(file);
        if (allowedPasswordOverwrite || !keystore.exists()) {
            logger.info(Messages.getString("generate.saml.keystore.command.correctly.begin")); //$NON-NLS-1$
            GenerateSamlKeystoreUtil util = new GenerateSamlKeystoreUtil();
            util.generate(password, keystore);
            // Once the keystore file is generated the references to the saml
            // password existing in the overlord.properties file should be
            // updated.
            updateOverlordProperties();
            logger.info(Messages.getString("generate.saml.keystore.command.correctly.created")); //$NON-NLS-1$
        } else {
            String message = Messages.getString("overlord.commons.fabric.configured.already");//$NON-NLS-1$
            logger.info(message);
            System.out.println(message);
        }
        return null;
    }

    /**
     * Gets the fabric overlord profile path
     *
     * @return the fuse config path
     */
    public String getOverlordProfilePath() {
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(getFabricProfilesPath()).append(OVERLORD_COMMONS_PROFILE_PATH).append(File.separator);
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
        String aesEncryptedValue = AesEncrypter.encrypt(password);
        StringBuilder aesEncrypterBuilder = new StringBuilder();
        aesEncrypterBuilder.append("${crypt:").append(aesEncryptedValue).append("}"); //$NON-NLS-1$ //$NON-NLS-2$
        aesEncryptedValue = aesEncrypterBuilder.toString();
        try {
            out = new FileOutputStream(filePath);
            props.setProperty(CommandConstants.OverlordProperties.OVERLORD_BASE_URL, CommandConstants.OverlordProperties.OVERLORD_BASE_URL_VALUE);
            props.setProperty(CommandConstants.OverlordProperties.OVERLORD_SAML_KEYSTORE,
                    CommandConstants.OverlordProperties.OVERLORD_SAML_KEYSTORE_FABRIC_VALUE);
            props.setProperty(CommandConstants.OverlordProperties.OVERLORD_SAML_ALIAS, CommandConstants.OverlordProperties.OVERLORD_SAML_ALIAS_VALUE);
            props.setProperty(CommandConstants.OverlordProperties.OVERLORD_KEYSTORE_ALIAS_PASSWORD_KEY, aesEncryptedValue);
            props.setProperty(CommandConstants.OverlordProperties.OVERLORD_KEYSTORE_PASSWORD_KEY, aesEncryptedValue);
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
    protected String getOverlordPropertiesFilePath() {
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(getOverlordProfilePath())
                .append(CommandConstants.OverlordProperties.OVERLORD_PROPERTIES_FILE_NAME);
        return fuse_config_path.toString();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAllowedPasswordOverwrite() {
        return allowedPasswordOverwrite;
    }

    public void setAllowedPasswordOverwrite(boolean allowedPasswordOverwrite) {
        this.allowedPasswordOverwrite = allowedPasswordOverwrite;
    }


}
