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
import java.util.Arrays;
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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.gogo.commands.Argument;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.overlord.commons.karaf.commands.GenerateSamlKeystoreCommand;
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

    @Argument(index = 0, name = "adminPassword", required = true, multiValued = false)
    protected String adminPassword = null;
    
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
        // Note: We're using the existence of overlord.properties to identify that Overlord has been installed, period.
        // If it exists, also skip configuring jetty.xml, etc.
        if (!destFile.exists()) {
            // Note that overlord.auth.saml-keystore-password and overlord.auth.saml-key-alias-password are
            // eventually written by GenerateSamlKeystoreCommand.
            InputStream is = AbstractConfigureCommand.class.getClassLoader().getResourceAsStream(
                    "/overlord.properties"); //$NON-NLS-1$
            FileUtils.copyInputStreamToFile(is, destFile);
            
            // Add realm to jetty.xml
            File xmlFile = new File(karafConfigPath + "jetty.xml"); //$NON-NLS-1$
            InputStream xsltFile = AbstractConfigureCommand.class.getClassLoader().getResourceAsStream(
                    "/addRealm-fuse-6.1.xslt"); //$NON-NLS-1$
            applyXslt(xmlFile, xsltFile);
            
            // Setup users.properties
            Properties usersProperties = new Properties();
            File srcFile = new File(karafConfigPath + "users.properties"); //$NON-NLS-1$
            usersProperties.load(new FileInputStream(srcFile));
            // If admin is already setup, gracefully handle it.
            String admin = (String) usersProperties.get("admin"); //$NON-NLS-1$
            admin = admin == null ? "" : admin; //$NON-NLS-1$
            // username=password,role1,role2,role3...
            String[] split = admin.split("/s*,/s*"); //$NON-NLS-1$
            String password;
            String[] roles;
            if (split.length > 1) {
                // password and role(s) already setup
                password = split[0];
                roles = Arrays.copyOfRange(split, 1, split.length);
            } else {
                password = "{CRYPT}" + DigestUtils.sha256Hex(adminPassword) + "{CRYPT}"; //$NON-NLS-1$ //$NON-NLS-2$
                roles = new String[] {"overlorduser", "overlordadmin", "admin.sramp"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            usersProperties.setProperty("admin", password + "," + StringUtils.join(roles, ",")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            usersProperties.store(new FileOutputStream(srcFile), ""); //$NON-NLS-1$
            
            // enable encryption
            Properties jaasProperties = new Properties();
            srcFile = new File(karafConfigPath + "org.apache.karaf.jaas.cfg"); //$NON-NLS-1$
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
        }
        
        GenerateSamlKeystoreCommand keystoreCommand = new GenerateSamlKeystoreCommand();
        keystoreCommand.setBundleContext(bundleContext);
        keystoreCommand.setKeystorePassword(adminPassword);
        keystoreCommand.execute(session);
        
        return null;
    }
    
    /**
     * Applies XSLT to the given XML file.  Note that the transformation is *in-place*!  It will simply
     * overwrite the original file!
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
            public InputSource resolveEntity(String pid, String sid) throws SAXException {
                return new InputSource(AbstractConfigureCommand.class.getClassLoader().getResourceAsStream(
                        "/configure.dtd")); //$NON-NLS-1$
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
}
