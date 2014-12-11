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

import java.util.Set;

/**
 * Provides simple access to services.
 *
 * @author eric.wittmann@redhat.com
 */
public class ServiceRegistryUtil {

    private static CompositeServiceRegistry registry = new CompositeServiceRegistry();
    static {
        try {
            registry.addRegistry(new ServiceLoaderServiceRegistry());
        } catch (Throwable t) {
            // do nothing
        }
    }

    /**
     * @see org.overlord.commons.services.ServiceRegistry#getSingleService(Class)
     */
    public static <T> T getSingleService(Class<T> serviceInterface) throws IllegalStateException {
        return registry.getSingleService(serviceInterface);
    }

    /**
     * @see org.overlord.commons.services.ServiceRegistry#getServices(Class)
     */
    public static <T> Set<T> getServices(Class<T> serviceInterface) {
        return registry.getServices(serviceInterface);
    }

    /**
     * @see org.overlord.commons.services.ServiceRegistry#addServiceListener(Class,ServiceListener)
     */
    public static <T> void addServiceListener(Class<T> serviceInterface, ServiceListener<T> listener) {
        registry.addServiceListener(serviceInterface, listener);
    }

    /**
     * @see org.overlord.commons.services.ServiceRegistry#removeServiceListener(ServiceListener)
     */
    public static <T> void removeServiceListener(ServiceListener<T> listener) {
        registry.removeServiceListener(listener);
    }

}
