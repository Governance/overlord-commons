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

package org.jboss.solder.resourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.jboss.solder.logging.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * A solder resource loader that uses the osgi bundle to resolve resources.
 *
 * @author eric.wittmann@redhat.com
 */
public class BundleResourceLoader implements ResourceLoader {

    private static final Logger log = Logger.getLogger("org.jboss.solder.resources"); //$NON-NLS-1$

    /**
     * Constructor.
     */
    public BundleResourceLoader() {
    }

    /**
     * @see org.jboss.solder.resourceLoader.ResourceLoader#getResourceAsStream(java.lang.String)
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        // Always use the strippedName, classloader always assumes no starting /
        String strippedName = getStrippedName(name);
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        URL resource = bundle.getResource(strippedName);
        if (resource != null) {
            log.trace("Loaded resource from bundle: " + strippedName); //$NON-NLS-1$
            try {
                return resource.openStream();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * @see org.jboss.solder.resourceLoader.ResourceLoader#getResource(java.lang.String)
     */
    @Override
    public URL getResource(String name) {
        // Always use the strippedName, classloader always assumes no starting /
        String strippedName = getStrippedName(name);
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        URL resource = bundle.getResource(strippedName);
        return resource;
    }


    /**
     * @see org.jboss.solder.resourceLoader.ResourceLoader#getResources(java.lang.String)
     */
    @Override
    public Set<URL> getResources(String name) {
        Set<URL> urls = new HashSet<URL>();
        // Always use the strippedName, classloader always assumes no starting /
        String strippedName = getStrippedName(name);
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        try {
            @SuppressWarnings("unchecked")
            Enumeration<URL> urlEnum = bundle.getResources(strippedName);
            if (urlEnum != null) {
                while (urlEnum.hasMoreElements()) {
                    urls.add(urlEnum.nextElement());
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return urls;
    }

    /**
     * @see org.jboss.solder.resourceLoader.ResourceLoader#getResourcesAsStream(java.lang.String)
     */
    @Override
    public Collection<InputStream> getResourcesAsStream(String name) {
        Set<InputStream> resources = new HashSet<InputStream>();
        // Always use the strippedName, classloader always assumes no starting /
        String strippedName = getStrippedName(name);
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        try {
            @SuppressWarnings("unchecked")
            Enumeration<URL> urlEnum = bundle.getResources(strippedName);
            if (urlEnum != null) {
                while (urlEnum.hasMoreElements()) {
                    resources.add(urlEnum.nextElement().openStream());
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return resources;
    }

    /**
     * @see org.jboss.solder.util.Sortable#getPrecedence()
     */
    @Override
    public int getPrecedence() {
        return 10;
    }

    private static String getStrippedName(String name) {
        return name.startsWith("/") ? name.substring(1) : name; //$NON-NLS-1$
    }

}
