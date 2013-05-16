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
package org.overlord.commons.dev.server;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.resource.Resource;

/**
 * A collection of servlets.  Allows the server to map multiple Jetty
 * DefaultServlets to the same path.
 *
 * @author eric.wittmann@redhat.com
 */
public class MultiDefaultServlet extends DefaultServlet {

    private static final long serialVersionUID = 164511284620801981L;

    private ContextHandler ctxHandler;
    private final Set<Resource> resourceBases = new LinkedHashSet<Resource>();

    /**
     * Constructor.
     */
    public MultiDefaultServlet() {
    }

    /**
     * @see org.eclipse.jetty.servlet.DefaultServlet#init()
     */
    @Override
    public void init() throws UnavailableException {
        super.init();
        String rbsParam = getInitParameter("resourceBases");
        String[] rbs = rbsParam.split("\\|");
        for (String rb : rbs) {
            try {
                Resource r = ctxHandler.newResource(rb);
                resourceBases.add(r);
            } catch (Exception e) {
                throw new UnavailableException(e.toString());
            }
        }

    }

    /**
     * @see org.eclipse.jetty.servlet.DefaultServlet#getResource(java.lang.String)
     */
    @Override
    public synchronized Resource getResource(String pathInContext) {
        for (Resource resourceBase : resourceBases) {
            setResourceBase(resourceBase);
            Resource resource = super.getResource(pathInContext);
            if (resource != null && resource.exists()) {
                return resource;
            }
        }
        return null;
    }

    /**
     * @see org.eclipse.jetty.servlet.DefaultServlet#initContextHandler(javax.servlet.ServletContext)
     */
    @Override
    protected ContextHandler initContextHandler(ServletContext servletContext) {
        ctxHandler = super.initContextHandler(servletContext);
        return ctxHandler;
    }

    /**
     * Sets the filter's resource base.
     * @param resourceBase
     */
    private void setResourceBase(Resource resourceBase) {
        try {
            Field declaredField = DefaultServlet.class.getDeclaredField("_resourceBase");
            declaredField.setAccessible(true);
            declaredField.set(this, resourceBase);
            declaredField.setAccessible(false);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
