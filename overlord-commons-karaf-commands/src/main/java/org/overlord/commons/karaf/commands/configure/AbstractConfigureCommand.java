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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.gogo.commands.Argument;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.overlord.commons.karaf.commands.ChangePasswordCommand;
import org.overlord.commons.karaf.commands.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Brett Meyer
 */
public abstract class AbstractConfigureCommand extends OsgiCommandSupport {
    protected String karafHome = System.getProperty("karaf.home"); //$NON-NLS-1$

    protected String karafConfigDir = "etc"; //$NON-NLS-1$

    protected String karafConfigPath;

    private static final Logger logger = LoggerFactory.getLogger(AbstractConfigureCommand.class);

    @Argument(index = 0, name = "password", required = false, multiValued = false)
    protected String password = null;

    public AbstractConfigureCommand() {
        StringBuilder sb = new StringBuilder(karafHome);
        if (!karafHome.endsWith(File.separator)) {
            sb.append(File.separator);
        }
        sb.append(karafConfigDir).append(File.separator);
        karafConfigPath = sb.toString();
    }

    @Override
    protected Object doExecute() throws Exception {
        File destFile = new File(karafConfigPath + "overlord.properties"); //$NON-NLS-1$
        logger.info(Messages.getString("overlord.configure.execution"));
        // Note: We're using the existence of overlord.properties to identify
        // that Overlord has been installed, period.
        if (destFile.exists()) {
            // Already installed an overlord commons installation.
            String message = Messages.getString("overlord.configure.password.previous.installation");
            System.out.println(message);
            logger.info(message);
            return null;
        } else if (StringUtils.isBlank(password)) {
            throw new RuntimeException(Messages.getString("overlord.configure.password.required.first.install"));
        } else {
            logger.debug(Messages.getString("overlord.configure.execution.started"));
            commonActions();

            ChangePasswordCommand passwordCommand = new ChangePasswordCommand();
            passwordCommand.setCreationAllowed(true);
            passwordCommand.setBundleContext(bundleContext);
            passwordCommand.setPassword(password);
            passwordCommand.execute(session);
            logger.debug(Messages.getString("overlord.configure.execution.ended"));
        }

        return null;
    }

    public void commonActions() throws Exception {
        logger.debug(Messages.getString("overlord.configure.common.actions.started"));
        // Add realm to jetty.xml
        File xmlFile = new File(karafConfigPath + "jetty.xml"); //$NON-NLS-1$
        InputStream xsltFile = AbstractConfigureCommand.class.getClassLoader().getResourceAsStream("/addRealm-fuse-6.1.xslt"); //$NON-NLS-1$
        applyXslt(xmlFile, xsltFile);

        // enable encryption
        Properties jaasProperties = new Properties();
        File srcFile = new File(karafConfigPath + "org.apache.karaf.jaas.cfg"); //$NON-NLS-1$
        jaasProperties.load(new FileInputStream(srcFile));
        jaasProperties.setProperty("encryption.enabled", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        jaasProperties.setProperty("encryption.algorithm", "SHA-256"); //$NON-NLS-1$ //$NON-NLS-2$
        jaasProperties.store(new FileOutputStream(srcFile), ""); //$NON-NLS-1$

        // enable JBoss repose
        Properties mvnProperties = new Properties();
        srcFile = new File(karafConfigPath + "org.ops4j.pax.url.mvn.cfg"); //$NON-NLS-1$
        mvnProperties.load(new FileInputStream(srcFile));
        mvnProperties.setProperty("org.ops4j.pax.url.mvn.repositories", //$NON-NLS-1$
                mvnProperties.getProperty("org.ops4j.pax.url.mvn.repositories") //$NON-NLS-1$
                        + ", http://repository.jboss.org/nexus/content/groups/developer/@snapshots"); //$NON-NLS-1$
        mvnProperties.store(new FileOutputStream(srcFile), ""); //$NON-NLS-1$
        logger.debug(Messages.getString("overlord.configure.common.actions.started"));
    }

    /**
     * Applies XSLT to the given XML file. Note that the transformation is
     * *in-place*! It will simply overwrite the original file!
     *
     * @param xmlFile
     * @param xsltFile
     * @throws Exception
     */
    protected void applyXslt(File xmlFile, InputStream xsltFile) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String pid, String sid) throws SAXException {
                return new InputSource(AbstractConfigureCommand.class.getClassLoader().getResourceAsStream("xslt/configure.dtd")); //$NON-NLS-1$
            }
        });
        Document d = db.parse(xmlFile);
        DOMSource xml = new DOMSource(d);

        Source xslt = new StreamSource(xsltFile);
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(xslt);
        Result result = new StreamResult(xmlFile);
        transformer.transform(xml, result);
    }

    /**
     * Copy the given filename from this bundle to Fuse.  2 assumptions are made:
     *
     * 1.) The filename is available in this bundle's /src/main/resources.
     * 2.) The target is simply FUSE_HOME/etc.
     *
     * @param filename
     * @throws Exception
     */
    protected void copyFile(String filename) throws Exception {
        File destFile = new File(karafConfigPath + filename);
        if (!destFile.exists()) {
            InputStream is = this.getClass().getResourceAsStream("/" + filename); //$NON-NLS-1$
            FileUtils.copyInputStreamToFile(is, destFile);
        }
    }

    /**
     * Copy the given filename from this bundle to Fuse. 2 assumptions are made:
     *
     * 1.) The filename is available in this bundle's /src/main/resources. 2.)
     * The target is simply FUSE_HOME/etc.
     *
     * @param filename
     * @throws Exception
     */
    protected void copyFile(String inputFile, String destFileName) throws Exception {
        File destFile = new File(karafConfigPath + destFileName);
        if (!destFile.exists()) {
            InputStream is = this.getClass().getResourceAsStream("/" + inputFile); //$NON-NLS-1$
            FileUtils.copyInputStreamToFile(is, destFile);
        }
    }
}
