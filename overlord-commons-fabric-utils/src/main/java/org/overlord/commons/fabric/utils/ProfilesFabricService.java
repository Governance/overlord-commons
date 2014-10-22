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
package org.overlord.commons.fabric.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.management.MalformedObjectNameException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jolokia.client.J4pClient;
import org.jolokia.client.J4pClientBuilder;
import org.jolokia.client.request.J4pExecRequest;
import org.jolokia.client.request.J4pResponse;
import org.overlord.commons.fabric.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that allows to connect to the fabric instance using jolokia
 * client. The way the instructions are executed in fabric are using the fabric
 * mbean that is is used in karaf. This class is a bridge between the overlord
 * applications and a fabric server.
 *
 * @author David Virgil Naranjo
 */
public class ProfilesFabricService {

    private static Logger logger = LoggerFactory.getLogger(ProfilesFabricService.class);

    private J4pClient client;

    private final static String FABRIC_MBEAN_NAME = "io.fabric8:type=Fabric"; //$NON-NLS-1$

    private final static String FABRIC_MAIN_PROFILE_FILE = "io.fabric8.agent.properties"; //$NON-NLS-1$

    /**
     * Instantiates a new profiles fabric service.
     *
     * @param jolokiaUrl
     *            the jolokia url
     * @param user
     *            the user
     * @param password
     *            the password
     */
    public ProfilesFabricService(String jolokiaUrl, String user, String password) {
        try {
            client = createJolokiaClient(jolokiaUrl, user, password);
        } catch (Exception e) {
            throw new RuntimeException(Messages.format("fabric.service.jolokia.client.generation.error", jolokiaUrl, user, password), e); //$NON-NLS-1$
        }

    }

    /**
     * Creates the profile.
     *
     * @param profileFolder
     *            the profile folder
     * @param profileName
     *            the profile name
     * @param version
     *            the version
     * @param zipFile
     *            the zip file
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ConfigurationException
     *             the configuration exception
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     */
    public String createProfile(String profileFolder, String profileName, String version, InputStream zipFile) throws IOException,
            ConfigurationException, MalformedObjectNameException {
        String profileId = getProfileName(profileFolder, profileName);
        String versionId = getOrCreateVersion(version);
        createProfile(versionId, profileId);

        ZipInputStream zis = new ZipInputStream(zipFile);
        ZipEntry ze = zis.getNextEntry();

        while (ze != null) {

            String name = ze.getName();
            if (name.contains(FABRIC_MAIN_PROFILE_FILE)) {

                // READ THE MAIN PROFILE FILE AND PARSE ITS CONTENT
                int count;
                byte data[] = new byte[2048];
                File f = File.createTempFile("temp.fabric", "properties"); //$NON-NLS-1$ //$NON-NLS-2$
                FileOutputStream fos = new FileOutputStream(f);

                while ((count = zis.read(data, 0, 2048)) != -1) {
                    fos.write(data, 0, count);
                }
                fos.flush();
                fos.close();

                PropertiesConfiguration propertiesFile = new PropertiesConfiguration(f);
                parseProperties(profileId, versionId, propertiesFile);
                f.delete();
            } else {
                if (!ze.isDirectory()) {
                    int count;
                    byte data[] = new byte[2048];
                    File f = File.createTempFile("temp.fabric", "properties"); //$NON-NLS-1$ //$NON-NLS-2$
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(f);

                        while ((count = zis.read(data, 0, 2048)) != -1) {
                            fos.write(data, 0, count);
                        }
                        fos.flush();
                    } finally {
                        if (fos != null) {
                            fos.close();
                        }
                    }
                    InputStream is = null;
                    try {
                        is = new FileInputStream(f);

                        uploadProfilePropertiesFile(versionId, profileId, ze.getName(), is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }

                    f.delete();
                }

            }
            ze = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
        refreshProfile(versionId, profileId);
        return profileId;
    }

    /**
     * Delete profile.
     *
     * @param version
     *            the version
     * @param profileId
     *            the profile id
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     */
    public void deleteProfile(String version, String profileId) throws MalformedObjectNameException {
        String versionId = getVersion(version);
        executeJ4P("deleteProfile(java.lang.String,java.lang.String)", true, true, versionId, profileId); //$NON-NLS-1$
    }

    /**
     * Refresh profile.
     *
     * @param versionId
     *            the version id
     * @param profileId
     *            the profile id
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     */
    private void refreshProfile(String versionId, String profileId) throws MalformedObjectNameException {
        executeJ4P("refreshProfile", true, false, versionId, profileId); //$NON-NLS-1$
    }

    /**
     * Gets the profile name.
     *
     * @param profileFolder
     *            the profile folder
     * @param profileId
     *            the profile id
     * @return the profile name
     */
    private String getProfileName(String profileFolder, String profileId) {
        String profileName = profileFolder.replace("/", "-");
        profileName = profileName.replace("\\", "-");
        profileName = profileName.concat("-").concat(profileId);
        return profileName;
    }

    /**
     * Gets the or create version.
     *
     * @param version
     *            the version
     * @return the or create version
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     */
    private String getOrCreateVersion(String version) throws MalformedObjectNameException {
        if (StringUtils.isBlank(version)) {
            Map<String, Object> value = (Map<String, Object>) executeJ4P("defaultVersion", true, false); //$NON-NLS-1$
            if (value != null) {
                return (String) value.get("id");
            } else {
                throw new RuntimeException(Messages.format("fabric.service.jolokia.get.default.version.empty")); //$NON-NLS-1$
            }

        } else {
            // First try to get the version(if it already exists)
            boolean existVersion = false;
            Map<String, Object> value = (Map<String, Object>) executeJ4P("getVersion(java.lang.String)", false, false, version); //$NON-NLS-1$
            if (value != null) {
                existVersion = true;
            }
            // If does not exist the version in fabric, let's create it
            if (!existVersion) {
                value = (Map<String, Object>) executeJ4P("createVersion(java.lang.String)", false, false, version); //$NON-NLS-1$
                if (value != null) {
                    return (String) value.get("id");
                } else {
                    throw new RuntimeException(Messages.format("fabric.service.jolokia.create.version.empty")); //$NON-NLS-1$
                }
            }
            return version;

        }
    }

    /**
     * Gets the version.
     *
     * @param version
     *            the version
     * @return the version
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     */
    private String getVersion(String version) throws MalformedObjectNameException {
        if (StringUtils.isBlank(version)) {
            Map<String, Object> value = (Map<String, Object>) executeJ4P("defaultVersion", true, false); //$NON-NLS-1$
            if (value != null) {
                return (String) value.get("id"); //$NON-NLS-1$
            } else {
                throw new RuntimeException(Messages.format("fabric.service.jolokia.get.default.version.empty")); //$NON-NLS-1$
            }

        } else {
            return version;
        }
    }

    /**
     * Parses the properties.
     *
     * @param profileId
     *            the profile id
     * @param versionId
     *            the version id
     * @param props
     *            the props
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     */
    private void parseProperties(String profileId, String versionId, PropertiesConfiguration props) throws MalformedObjectNameException {
        List<String> features = new ArrayList<String>();
        List<String> bundles = new ArrayList<String>();
        List<String> parents = new ArrayList<String>();
        List<String> repositories = new ArrayList<String>();
        List<String> optionals = new ArrayList<String>();
        List<String> fabs = new ArrayList<String>();
        Map<String, String> properties = new HashMap<String, String>();
        Iterator it = props.getKeys();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.startsWith("feature")) { //$NON-NLS-1$
                features.add(props.getString(key));
            } else if (key.startsWith("bundle")) { //$NON-NLS-1$
                bundles.add(props.getString(key));
            } else if (key.startsWith("repository")) { //$NON-NLS-1$
                repositories.add(props.getString(key));
            } else if (key.startsWith("optional")) { //$NON-NLS-1$
                optionals.add(props.getString(key));
            } else if (key.startsWith("fab")) { //$NON-NLS-1$
                fabs.add(props.getString(key));
            } else if (key.equals("attribute.parents")) { //$NON-NLS-1$
                String parents_str = (props.getString(key));
                String[] split = parents_str.split(" ");
                if (split != null && split.length > 0) {
                    for (String parent : split) {
                        parents.add(parent);
                    }
                }
            } else {
                properties.put(key, props.getString(key));
            }
        }

        if (!features.isEmpty()) {
            executeJ4P("setProfileFeatures", true, true, versionId, profileId, asJson(features)); //$NON-NLS-1$

        }
        if (!repositories.isEmpty()) {
            executeJ4P("setProfileRepositories", true, true, versionId, profileId, //$NON-NLS-1$
                    asJson(repositories));

        }
        if (!bundles.isEmpty()) {
            executeJ4P("setProfileBundles", true, true, versionId, profileId, asJson(bundles)); //$NON-NLS-1$

        }
        if (!optionals.isEmpty()) {
            executeJ4P("setProfileOptionals", true, true, versionId, profileId, asJson(optionals)); //$NON-NLS-1$

        }
        if (!fabs.isEmpty()) {
            executeJ4P("setProfileFabs", true, true, versionId, profileId, asJson(fabs)); //$NON-NLS-1$

        }
        if (!properties.isEmpty()) {
            for (String key : properties.keySet()) {
                executeJ4P("setProfileAttribute", true, true, versionId, profileId, key, properties.get(key)); //$NON-NLS-1$
            }
        }

    }

    /**
     * Execute j4 p.
     *
     * @param operation
     *            the operation
     * @param throwException
     *            the throw exception
     * @param isVoid
     *            the is void
     * @param params
     *            the params
     * @return the object
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     */
    private Object executeJ4P(String operation, boolean throwException, boolean isVoid, Object... params) throws MalformedObjectNameException {
        J4pExecRequest request;
        if (params != null && params.length > 0) {
            request = new J4pExecRequest(FABRIC_MBEAN_NAME, operation, params);
        } else {
            request = new J4pExecRequest(FABRIC_MBEAN_NAME, operation);
        }
        try {
            J4pResponse<J4pExecRequest> response = client.execute(request, "POST");
            if (!isVoid) {
                return response.getValue();
            }

        } catch (Exception e) {
            if (throwException) {
                throw new RuntimeException(Messages.format("fabric.service.jolokia.error"), e);//$NON-NLS-1$
            }
            logger.info(Messages.format("fabric.service.jolokia.error"), e);//$NON-NLS-1$
        }
        return null;

    }

    /**
     * As json.
     *
     * @param list
     *            the list
     * @return the string
     */
    private String asJson(List<String> list) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        int i = 0;
        int size = list.size();
        for (String value : list) {
            jsonBuilder.append("\"").append(value).append("\"");
            if (i != size - 1) {
                jsonBuilder.append(",");
            }
            i++;
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }


    /**
     * Creates the profile.
     *
     * @param versionId
     *            the version id
     * @param profileId
     *            the profile id
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     */
    private void createProfile(String versionId, String profileId) throws MalformedObjectNameException {
        Map<String, Object> value = (Map<String, Object>) executeJ4P("getProfile(java.lang.String,java.lang.String)", false, false, versionId,
                profileId); //$NON-NLS-1$
        if (value != null) {
            // If the profile already exist, delete its content
            executeJ4P("deleteProfile(java.lang.String,java.lang.String)", true, true, versionId, profileId); //$NON-NLS-1$
        }

        // Create the profile
        value = (Map<String, Object>) executeJ4P("createProfile(java.lang.String,java.lang.String)", true, false, versionId, profileId); //$NON-NLS-1$
        if (value == null) {
            throw new RuntimeException(Messages.format("fabric.service.jolokia.create.profile.error", profileId, versionId)); //$NON-NLS-1$
        }
    }

    /**
     * Upload profile properties file.
     *
     * @param versionId
     *            the version id
     * @param profileId
     *            the profile id
     * @param path
     *            the path
     * @param is
     *            the is
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws MalformedObjectNameException
     *             the malformed object name exception
     */
    protected void uploadProfilePropertiesFile(String versionId, String profileId, String path, InputStream is) throws IOException,
            MalformedObjectNameException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer);
        executeJ4P("setConfigurationFile", true, false, versionId, profileId, path, writer.toString()); //$NON-NLS-1$
    }

    /**
     * Creates the jolokia client.
     *
     * @param jolokiaUrl
     *            the jolokia url
     * @param user
     *            the user
     * @param password
     *            the password
     * @return the j4p client
     */
    private J4pClient createJolokiaClient(String jolokiaUrl, String user, String password) {
        J4pClientBuilder builder = J4pClient.url(jolokiaUrl);
        if (StringUtils.isNotBlank(user)) {
            builder = builder.user(user);
        }
        if (StringUtils.isNotBlank(password)) {
            builder = builder.password(password);
        }
        return builder.build();
    }
}
