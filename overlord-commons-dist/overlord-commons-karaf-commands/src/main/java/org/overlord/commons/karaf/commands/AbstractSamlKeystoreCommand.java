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

import org.apache.felix.gogo.commands.Argument;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.overlord.commons.karaf.commands.i18n.Messages;
import org.overlord.commons.karaf.commands.saml.GenerateSamlKeystoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class AbstractSamlKeystoreCommand extends OsgiCommandSupport {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSamlKeystoreCommand.class);

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
    
    abstract protected String getConfigPath();
    
    abstract protected void updateOverlordProperties() throws Exception;
}
