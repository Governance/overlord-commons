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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages a list of service registries.  The first one always wins.
 *
 * @author eric.wittmann@redhat.com
 */
public class CompositeServiceRegistry implements ServiceRegistry {
    
    private List<ServiceRegistry> registries = new ArrayList<ServiceRegistry>();
    
    /**
     * Constructor.
     */
    public CompositeServiceRegistry() {
    }

    /**
     * @see org.overlord.commons.services.ServiceRegistry#getSingleService(java.lang.Class)
     */
    @Override
    public <T> T getSingleService(Class<T> serviceInterface) throws IllegalStateException {
        for (ServiceRegistry registry : registries) {
            T service = registry.getSingleService(serviceInterface);
            if (service != null) {
                return service;
            }
        }
        return null;
    }

    /**
     * @see org.overlord.commons.services.ServiceRegistry#getServices(java.lang.Class)
     */
    @Override
    public <T> Set<T> getServices(Class<T> serviceInterface) {
        Set<T> rval = new LinkedHashSet<T>();
        for (ServiceRegistry registry : registries) {
            Set<T> svcs = registry.getServices(serviceInterface);
            if (svcs != null) {
                rval.addAll(svcs);
            }
        }
        return rval;
    }

    /**
     * @see org.overlord.commons.services.ServiceRegistry#addServiceListener(java.lang.Class,org.overlord.commons.services.ServiceListener)
     */
    @Override
    public <T> void addServiceListener(Class<T> serviceInterface, ServiceListener<T> listener) {
        for (ServiceRegistry registry : registries) {
            registry.addServiceListener(serviceInterface, listener);
        }
    }

    /**
     * @see org.overlord.commons.services.ServiceRegistry#removeServiceListener(org.overlord.commons.services.ServiceListener)
     */
    @Override
    public <T> void removeServiceListener(ServiceListener<T> listener) {
        for (ServiceRegistry registry : registries) {
            registry.removeServiceListener(listener);
        }
    }

    /**
     * @param registry
     */
    public void addRegistry(ServiceRegistry registry) {
        registries.add(registry);
    }

}
