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
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.overlord.commons.karaf.commands.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Karaf console command for use within JBoss Fuse. It generates/overwrites the
 * overlord-saml.keystore file and configures and modify the admin user inside
 * of the users.properties.
 *
 * overlord:changePassword [password]
 *
 * Note that this uses the BouncyCastle library to encrypt the keystore file. It
 * was not possible to directly use sun.security as it does not support OSGi
 * environments.
 *
 * @author David Virgil Naranjo
 */
@Command(scope = "overlord", name = "changePassword")
public class ChangePasswordCommand extends OsgiCommandSupport {

    protected String karafHome = System.getProperty("karaf.home"); //$NON-NLS-1$

    protected String karafConfigDir = "etc"; //$NON-NLS-1$

    private static final Logger logger = LoggerFactory.getLogger(ChangePasswordCommand.class);

    @Argument(index = 0, name = "password", required = true, multiValued = false)
    protected String password = null;

    protected String karafConfigPath;

    private boolean creationAllowed;


    /**
     * Instantiates a new change password command.
     */
    public ChangePasswordCommand() {
        StringBuilder sb = new StringBuilder(karafHome);
        if (!karafHome.endsWith(File.separator)) {
            sb.append(File.separator);
        }
        sb.append(karafConfigDir).append(File.separator);
        karafConfigPath = sb.toString();
        creationAllowed = false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.karaf.shell.console.AbstractAction#doExecute()
     */
    @Override
    protected Object doExecute() throws Exception {
        logger.debug(Messages.getString("overlord.change.password.start")); //$NON-NLS-1$

        File destFile = new File(karafConfigPath + "overlord.properties"); //$NON-NLS-1$

        // Note: We're using the existence of overlord.properties to identify
        // that Overlord has been installed, period.
        if (destFile.exists() || creationAllowed) {
            // Setup users.properties
            Properties usersProperties = new Properties();
            File srcFile = new File(karafConfigPath + "users.properties"); //$NON-NLS-1$
            usersProperties.load(new FileInputStream(srcFile));
            // If admin is already setup, gracefully handle it.
            String admin = (String) usersProperties.get("admin"); //$NON-NLS-1$
            admin = admin == null ? "" : admin; //$NON-NLS-1$
            // username=password,role1,role2,role3...
            String[] split = admin.split("/s*,/s*"); //$NON-NLS-1$
            String pass;
            String[] roles;
            pass = "{CRYPT}" + DigestUtils.sha256Hex(password) + "{CRYPT}"; //$NON-NLS-1$ //$NON-NLS-2$
            if (split.length > 1) {
                // password and role(s) already setup. In this case we ll
                // maintain the previous grants
                roles = Arrays.copyOfRange(split, 1, split.length);
            } else {
                roles = new String[] { "overlorduser", "overlordadmin", "admin.sramp" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            usersProperties.setProperty("admin", pass + "," + StringUtils.join(roles, ",")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            usersProperties.store(new FileOutputStream(srcFile), ""); //$NON-NLS-1$
            logger.info(Messages.getString("overlord.change.password.user.added")); //$NON-NLS-1$

            logger.debug(Messages.getString("overlord.change.password.generating.keystore")); //$NON-NLS-1$
            GenerateSamlKeystoreCommand keystoreCommand = new GenerateSamlKeystoreCommand();
            keystoreCommand.setBundleContext(bundleContext);
            keystoreCommand.setKeystorePassword(password);
            keystoreCommand.execute(session);
            logger.debug(Messages.getString("overlord.change.password.end")); //$NON-NLS-1$
        } else {
            String message = Messages.getString("overlord.change.no.previous.installation"); //$NON-NLS-1$
            logger.info(message);
            System.out.println(message);
        }

        return null;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password
     *            the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCreationAllowed() {
        return creationAllowed;
    }

    public void setCreationAllowed(boolean creationAllowed) {
        this.creationAllowed = creationAllowed;
    }

}
