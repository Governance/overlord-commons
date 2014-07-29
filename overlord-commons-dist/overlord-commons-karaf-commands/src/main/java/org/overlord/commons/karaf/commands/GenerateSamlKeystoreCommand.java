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
@Command(scope = "overlord", name = "generateSamlKeystore", description = "Generates a keystore file and update the etc folder and the overlord profiles")
public class GenerateSamlKeystoreCommand extends OsgiCommandSupport {

    private static final Logger logger = LoggerFactory.getLogger(GenerateSamlKeystoreCommand.class);

    @Argument(index = 0, name = "password", description = "The command argument", required = true, multiValued = false)
    String password = null;

    public static final String FILE_KEYSTORE_NAME = "overlord-saml.keystore";
    public static final String OVERLORD_PROPERTIES_FILE_NAME = "overlord.properties";
    public static final String FUSE_CONFIG_DIR = "etc";

    public static final String OVERLORD_KEYSTORE_ALIAS_PASSWORD_KEY = "overlord.auth.saml-key-alias-password";
    public static final String OVERLORD_KEYSTORE_PASSWORD_KEY = "overlord.auth.saml-keystore-password";
    public static final String OVERLORD_SAML_ALIAS = "overlord.auth.saml-key-alias";
    public static final String OVERLORD_SAML_ALIAS_VALUE = "overlord";
    public static final String OVERLORD_SAML_KEYSTORE = "overlord.auth.saml-keystore";
    public static final String OVERLORD_SAML_KEYSTORE_VALUE = "${sys:karaf.home}/etc/overlord-saml.keystore";

    /*
     * (non-Javadoc)
     *
     * @see org.apache.karaf.shell.console.AbstractAction#doExecute()
     */
    @Override
    protected Object doExecute() throws Exception {
        String fuse_config_path = getFuseConfigPath();
        String file = fuse_config_path + FILE_KEYSTORE_NAME;
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
    private String getFuseConfigPath() {
        String karaf_home = System.getProperty("karaf.home");
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(karaf_home);
        if (!karaf_home.endsWith(File.separator)) {
            fuse_config_path.append(File.separator);
        }
        fuse_config_path.append(FUSE_CONFIG_DIR).append(File.separator);
        return fuse_config_path.toString();
    }

    /**
     * Gets the overlord properties file path.
     *
     * @return the overlord properties file path
     */
    private String getOverlordPropertiesFilePath() {
        String karaf_home = System.getProperty("karaf.home");
        StringBuilder fuse_config_path = new StringBuilder();
        fuse_config_path.append(karaf_home);
        if (!karaf_home.endsWith(File.separator)) {
            fuse_config_path.append(File.separator);
        }
        fuse_config_path.append(FUSE_CONFIG_DIR).append(File.separator).append(OVERLORD_PROPERTIES_FILE_NAME);
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
                    props.setProperty(OVERLORD_KEYSTORE_ALIAS_PASSWORD_KEY, password);
                    props.setProperty(OVERLORD_KEYSTORE_PASSWORD_KEY, password);
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
                    props.setProperty(OVERLORD_SAML_KEYSTORE, OVERLORD_SAML_KEYSTORE_VALUE);
                    props.setProperty(OVERLORD_SAML_ALIAS, OVERLORD_SAML_ALIAS_VALUE);
                    props.setProperty(OVERLORD_KEYSTORE_ALIAS_PASSWORD_KEY, password);
                    props.setProperty(OVERLORD_KEYSTORE_PASSWORD_KEY, password);
                    props.store(out, null);

                } finally {
                    out.close();
                }
            }
        }
    }
}