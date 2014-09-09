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
 * Karaf console command for use within JBoss Fuse. It generates/overwrites the overlord-saml.keystore file in the /etc
 * folder.  Call it w/ the keystore password as an argument.  Ex:
 *
 * overlord:generateSamlKeystore [password]
 *
 * Note that this uses the BouncyCastle library to encrypt the keystore file. It was not possible to directly use
 * sun.security as it does not support OSGi environments.
 *
 * @author David Virgil Naranjo
 */
@Command(scope = "overlord", name = "generateSamlKeystore")
public class GenerateSamlKeystoreCommand extends OsgiCommandSupport {

    private static final Logger logger = LoggerFactory.getLogger(GenerateSamlKeystoreCommand.class);

    private static final String FUSE_CONFIG_DIR = "etc"; //$NON-NLS-1$

    @Argument(index = 0, name = "password", required = true, multiValued = false)
    protected String password = null;

    /*
     * (non-Javadoc)
     *
     * @see org.apache.karaf.shell.console.AbstractAction#doExecute()
     */
    @Override
    protected Object doExecute() throws Exception {
        String fuse_config_path = getConfigPath();
        String file = fuse_config_path + CommandConstants.OverlordProperties.FILE_KEYSTORE_NAME;
        logger.info(Messages.getString("generate.saml.keystore.command.correctly.begin")); //$NON-NLS-1$
        // This 3 lines generate/overwrite the keystore file.
        File keystore = new File(file);
        GenerateSamlKeystoreUtil util = new GenerateSamlKeystoreUtil();
        util.generate(password, keystore);
        // Once the keystore file is generated the references to the saml
        // password existing in the overlord.properties file should be updated.
        updateOverlordProperties();
        logger.info(Messages.getString("generate.saml.keystore.command.correctly.created")); //$NON-NLS-1$
        return null;
    }

    /**
     * Gets the fuse config path.
     *
     * @return the fuse config path
     */
    protected String getConfigPath() {
        String karaf_home = System.getProperty("karaf.home"); //$NON-NLS-1$
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(karaf_home);
        if (!karaf_home.endsWith(File.separator)) {
            fuse_config_path.append(File.separator);
        }
        fuse_config_path.append(FUSE_CONFIG_DIR).append(File.separator);
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
                    if (!props.containsKey(CommandConstants.OverlordProperties.OVERLORD_SAML_KEYSTORE)) {
                        props.setProperty(CommandConstants.OverlordProperties.OVERLORD_SAML_KEYSTORE,
                            CommandConstants.OverlordProperties.OVERLORD_SAML_KEYSTORE_VALUE);
                    }
                    if (!props.containsKey(CommandConstants.OverlordProperties.OVERLORD_SAML_ALIAS)) {
                        props.setProperty(CommandConstants.OverlordProperties.OVERLORD_SAML_ALIAS,
                            CommandConstants.OverlordProperties.OVERLORD_SAML_ALIAS_VALUE);
                    }
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
                    props.setProperty(CommandConstants.OverlordProperties.OVERLORD_SAML_KEYSTORE,
                            CommandConstants.OverlordProperties.OVERLORD_SAML_KEYSTORE_VALUE);
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
        String karaf_home = System.getProperty("karaf.home"); //$NON-NLS-1$
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(karaf_home);
        if (!karaf_home.endsWith(File.separator)) {
            fuse_config_path.append(File.separator);
        }
        fuse_config_path.append(FUSE_CONFIG_DIR).append(File.separator).append(CommandConstants.OverlordProperties.OVERLORD_PROPERTIES_FILE_NAME);
        return fuse_config_path.toString();
    }
}
