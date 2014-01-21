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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Implements a service registry by using the standard java {@link ServiceLoader}
 * mechanism.
 *
 * @author eric.wittmann@redhat.com
 */
public class ServiceLoaderServiceRegistry implements ServiceRegistry {
    
    private Map<Class<?>, Set<?>> servicesCache = new HashMap<Class<?>, Set<?>>();
    private Map<Class<?>, Object> serviceCache = new HashMap<Class<?>, Object>();

    /**
     * @see org.overlord.commons.services.ServiceRegistry#getSingleService(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getSingleService(Class<T> serviceInterface) throws IllegalStateException {
        if (serviceCache.containsKey(serviceInterface))
            return (T) serviceCache.get(serviceInterface);

        T rval = null;
        for (T service : ServiceLoader.load(serviceInterface)) {
            if (rval == null) {
                rval = service;
                serviceCache.put(serviceInterface, rval);
                break;
            } else {
                throw new IllegalStateException("Multiple implementations found for service: " + serviceInterface);
            }
        }
        return rval;
    }

    /**
     * @see org.overlord.commons.services.ServiceRegistry#getServices(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Set<T> getServices(Class<T> serviceInterface) {
        if (servicesCache.containsKey(serviceInterface))
            return (Set<T>) servicesCache.get(serviceInterface);

        Set<T> services = new HashSet<T>();
        for (T service : ServiceLoader.load(serviceInterface)) {
            services.add(service);
        }
        servicesCache.put(serviceInterface, services);
        return services;
    }

}
