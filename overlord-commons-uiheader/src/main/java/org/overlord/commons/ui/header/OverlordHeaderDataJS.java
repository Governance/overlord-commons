/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.commons.ui.header;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

/**
 * This is a simple servlet that generates the javascript data used by the Overlord Header javascript.
 *
 * @author eric.wittmann@redhat.com
 */
public class OverlordHeaderDataJS extends HttpServlet {

    private static final long serialVersionUID = -4982770016769892713L;

    private String appId;
    private String logoutUrl;

    /**
     * Constructor.
     */
    public OverlordHeaderDataJS() {
    }

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        ServletConfig config = getServletConfig();
        appId = config.getInitParameter("app-id"); //$NON-NLS-1$
        if (appId == null || appId.trim().length() == 0) {
            throw new ServletException("Application identifier (app-id) parameter missing from Overlord Header Data JS servlet."); //$NON-NLS-1$
        }
        logoutUrl = config.getInitParameter("logout-url"); //$NON-NLS-1$
        if (logoutUrl == null || logoutUrl.trim().length() == 0) {
            logoutUrl = "?GLO=true"; //$NON-NLS-1$
        }
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        // Tell the browser to never cache this JavaScript (it's generated and includes
        // information like the currently logged-in user).
        noCache(response);

        // Now generate the JavaScript data (JSON)
        response.setContentType("text/javascript"); //$NON-NLS-1$

        try {
            List<TabInfo> tabs = getTabs(request);

            response.getOutputStream().write("var OVERLORD_HEADER_DATA = ".getBytes("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
            JsonFactory f = new JsonFactory();
            JsonGenerator g = f.createJsonGenerator(response.getOutputStream(), JsonEncoding.UTF8);
            g.useDefaultPrettyPrinter();
            g.writeStartObject();
            g.writeStringField("username", getRemoteUser(request)); //$NON-NLS-1$
            g.writeStringField("logoutLink", getLogoutLink(request)); //$NON-NLS-1$
            g.writeStringField("primaryBrand", getPrimaryBrand(tabs)); //$NON-NLS-1$
            g.writeStringField("secondaryBrand", getSecondaryBrand(tabs)); //$NON-NLS-1$
            g.writeArrayFieldStart("tabs"); //$NON-NLS-1$
            for (TabInfo tabInfo : tabs) {
                g.writeStartObject();
                g.writeStringField("app-id", tabInfo.appId); //$NON-NLS-1$
                g.writeStringField("href", tabInfo.href); //$NON-NLS-1$
                g.writeStringField("label", tabInfo.label); //$NON-NLS-1$
                g.writeBooleanField("active", tabInfo.active); //$NON-NLS-1$
                g.writeEndObject();
            }
            g.writeEndArray();
            g.writeEndObject();
            g.flush();
            response.getOutputStream().write(";".getBytes("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
            g.close();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Gets the remote user, handles null.
     * @param request
     */
    private String getRemoteUser(HttpServletRequest request) {
        return request.getRemoteUser() == null ? "<anonymous>" : request.getRemoteUser(); //$NON-NLS-1$
    }

    /**
     * Gets the configured logout link.
     * @param request
     */
    private String getLogoutLink(HttpServletRequest request) {
        return logoutUrl;
    }

    /**
     * Gets the primary brand from the currently active tab.
     * @param tabs
     */
    private String getPrimaryBrand(List<TabInfo> tabs) {
        for (TabInfo tabInfo : tabs) {
            if (tabInfo.active) {
                return tabInfo.primaryBrand;
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Gets the secondary brand from the currently active tab.
     * @param tabs
     */
    private String getSecondaryBrand(List<TabInfo> tabs) {
        for (TabInfo tabInfo : tabs) {
            if (tabInfo.active) {
                return tabInfo.secondaryBrand;
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Gets the tabs configured to appear in the UI.
     * @param request
     */
    private List<TabInfo> getTabs(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        List<TabInfo> tabs = (List<TabInfo>) session.getAttribute("overlord-tabs"); //$NON-NLS-1$
        if (tabs == null) {
            tabs = new ArrayList<TabInfo>();
            getConfiguredTabs(tabs);
            session.setAttribute("overlord-tabs", tabs); //$NON-NLS-1$
        }
        return tabs;
    }

    /**
     * Reads any overlord header config files to determine which overlord applications
     * are currently deployed/configured.
     * @param tabs
     */
    private void getConfiguredTabs(List<TabInfo> tabs) throws Exception {
        File configDir = getConfigDir();
        if (configDir == null)
            return;

        TreeSet<TabInfo> sortedTabs = new TreeSet<TabInfo>(new Comparator<TabInfo>() {
            @Override
            public int compare(TabInfo o1, TabInfo o2) {
                return o1.appId.compareTo(o2.appId);
            }
        });
        Collection<File> configFiles = FileUtils.listFiles(configDir, new String[] { "properties" } , false); //$NON-NLS-1$
        for (File configFile : configFiles) {
            if (!configFile.getCanonicalPath().endsWith("-overlordapp.properties")) //$NON-NLS-1$
                continue;
            FileReader reader = new FileReader(configFile);
            try {
                Properties configProps = new Properties();
                configProps.load(new FileReader(configFile));
                String appId = configProps.getProperty("overlordapp.app-id"); //$NON-NLS-1$
                String href = configProps.getProperty("overlordapp.href"); //$NON-NLS-1$
                // TODO need i18n support here - need different versions of the config files for each lang?
                String primaryBrand = configProps.getProperty("overlordapp.primary-brand"); //$NON-NLS-1$
                String secondaryBrand = configProps.getProperty("overlordapp.secondary-brand"); //$NON-NLS-1$
                String label = configProps.getProperty("overlordapp.label"); //$NON-NLS-1$
                sortedTabs.add(new TabInfo(appId, primaryBrand, secondaryBrand, href, label, appId.equals(this.appId)));
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }

        tabs.addAll(sortedTabs);
    }

    /**
     * Returns the directory to check for overlord-app config files.
     */
    private File getConfigDir() {
        File configDir = null;

        // First, check for a configured system property
        String configDirProp = System.getProperty("org.overlord.apps.config-dir"); //$NON-NLS-1$
        if (configDirProp != null) {
            configDir = new File(configDirProp);
            if (configDir.isDirectory()) {
                return configDir;
            }
        }

        // Next, check for JBoss
        String jbossConfigDir = System.getProperty("jboss.server.config.dir"); //$NON-NLS-1$
        if (jbossConfigDir != null) {
            File dirFile = new File(jbossConfigDir);
            if (dirFile.isDirectory()) {
                configDir = new File(dirFile, "overlord-apps"); //$NON-NLS-1$
                if (configDir.isDirectory()) {
                    return configDir;
                }
            }
        }
        String jbossConfigUrl = System.getProperty("jboss.server.config.url"); //$NON-NLS-1$
        if (jbossConfigUrl != null) {
            File dirFile = new File(jbossConfigUrl);
            if (dirFile.isDirectory()) {
                configDir = new File(dirFile, "overlord-apps"); //$NON-NLS-1$
                if (configDir.isDirectory()) {
                    return configDir;
                }
            }
        }
        String jbossDataDir = System.getProperty("jboss.server.data.dir"); //$NON-NLS-1$
        if (jbossDataDir != null) {
            File dirFile = new File(jbossDataDir);
            if (dirFile.isDirectory()) {
                configDir = new File(dirFile, "overlord-apps"); //$NON-NLS-1$
                if (configDir.isDirectory()) {
                    return configDir;
                }
            }
        }
        
        // Now try tomcat
        String catalinaHomeDir = System.getProperty("catalina.home"); //$NON-NLS-1$
        if (catalinaHomeDir != null) {
            File dirFile = new File(catalinaHomeDir, "conf"); //$NON-NLS-1$
            if (dirFile.isDirectory()) {
                configDir = new File(dirFile, "overlord-apps"); //$NON-NLS-1$
                if (configDir.isDirectory()) {
                    return configDir;
                }
            }
        }
        
        // Now try karaf/fuse
        String karafDir = System.getProperty("karaf.home"); //$NON-NLS-1$
        if (karafDir != null) {
            File dirFile = new File(karafDir, "etc"); //$NON-NLS-1$
            if (dirFile.isDirectory()) {
                configDir = new File(dirFile, "overlord-apps"); //$NON-NLS-1$
                if (configDir.isDirectory()) {
                    return configDir;
                }
            }
        }

        // Now try jetty
        String jettyDir = System.getProperty("jetty.home"); //$NON-NLS-1$
        if (jettyDir != null) {
            File dirFile = new File(jettyDir, "etc"); //$NON-NLS-1$
            if (dirFile.isDirectory()) {
                configDir = new File(dirFile, "overlord-apps"); //$NON-NLS-1$
                if (configDir.isDirectory()) {
                    return configDir;
                }
            }
        }

        return null;
    }

    /**
     * Make sure to tell the browser not to cache it.
     *
     * @param response
     */
    private void noCache(HttpServletResponse response) {
        Date now = new Date();
        response.setDateHeader("Date", now.getTime()); //$NON-NLS-1$
        // one day old
        response.setDateHeader("Expires", now.getTime() - 86400000L); //$NON-NLS-1$
        response.setHeader("Pragma", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
        response.setHeader("Cache-control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * A single tab in the UI.
     * @author eric.wittmann@redhat.com
     */
    private static final class TabInfo implements Serializable {
        private static final long serialVersionUID = 7628110060136125387L;
        
        public final String appId;
        public final String primaryBrand;
        public final String secondaryBrand;
        public final String href;
        public final String label;
        public final boolean active;
        public TabInfo(String appId, String primaryBrand, String secondaryBrand, String href, String label, boolean active) {
            this.appId = appId;
            this.primaryBrand = primaryBrand;
            this.secondaryBrand = secondaryBrand;
            this.href = href;
            this.label = label;
            this.active = active;
        }
    }

}
