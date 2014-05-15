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

package org.overlord.commons.services;

import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Implements a service registry by delegating to the osgi service
 * registry.
 *
 * @author eric.wittmann@redhat.com
 */
public class OSGiServiceRegistry implements ServiceRegistry {

    public static final String OSGI_ENABLED_PROP = "overlord-commons-config.osgi-enabled"; //$NON-NLS-1$

    /**
     * @see org.overlord.commons.services.ServiceRegistry#getSingleService(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getSingleService(Class<T> serviceInterface) throws IllegalStateException {
        // TODO use the osgi service tracker here
        T service = null;
        try {
            BundleContext context = FrameworkUtil.getBundle(serviceInterface).getBundleContext();
            ServiceReference[] serviceReferences = context.getServiceReferences(serviceInterface.getName(), null);
            if (serviceReferences != null) {
                if (serviceReferences.length == 1)
                    service = (T) context.getService(serviceReferences[0]);
                else
                    throw new IllegalStateException(Messages.getString("OSGiServiceRegistry.MultipleImplsRegistered") + serviceInterface); //$NON-NLS-1$
            }
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
        return service;
    }

    /**
     * @see org.overlord.commons.services.ServiceRegistry#getServices(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Set<T> getServices(Class<T> serviceInterface) {
        Set<T> services = new HashSet<T>();
        try {
            BundleContext context = FrameworkUtil.getBundle(serviceInterface).getBundleContext();
            if (context != null) {
                ServiceReference[] serviceReferences = context.getServiceReferences(
                        serviceInterface.getName(), null);
                if (serviceReferences != null) {
                    for (ServiceReference serviceReference : serviceReferences) {
                        T service = (T) context.getService(serviceReference);
                        services.add(service);
                    }
                }
            }

        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
        return services;
    }

}
