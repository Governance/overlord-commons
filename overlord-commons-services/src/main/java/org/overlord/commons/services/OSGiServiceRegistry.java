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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

/**
 * Implements a service registry by delegating to the osgi service
 * registry.
 *
 * @author eric.wittmann@redhat.com
 */
public class OSGiServiceRegistry extends AbstractServiceRegistry {
    
    private static final Logger LOG=Logger.getLogger(OSGiServiceRegistry.class.getName());

    private java.util.Map<ServiceListener<?>, ServiceListenerAdapter<?>> _listeners=
                            new java.util.HashMap<ServiceListener<?>, ServiceListenerAdapter<?>>();

    /**
     * Constructor.
     */
    public OSGiServiceRegistry() {
    }

    /**
     * @see org.overlord.commons.services.ServiceRegistry#getSingleService(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getSingleService(Class<T> serviceInterface) throws IllegalStateException {
        // TODO use the osgi service tracker here
        T service = null;
        try {
            Bundle bundle = FrameworkUtil.getBundle(serviceInterface);
            if (bundle != null) {
                BundleContext context = bundle.getBundleContext();
                
                if (context != null) {
                    ServiceReference[] serviceReferences = context.getServiceReferences(serviceInterface.getName(), null);
                    if (serviceReferences != null) {
                        if (serviceReferences.length == 1)
                            service = (T) context.getService(serviceReferences[0]);
                        else
                            throw new IllegalStateException(Messages.getString("OSGiServiceRegistry.MultipleImplsRegistered") + serviceInterface); //$NON-NLS-1$
                    }
                } else {
                    LOG.warning("Unable to get bundle context for interface: "+serviceInterface);
                }
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
            Bundle bundle = FrameworkUtil.getBundle(serviceInterface);
            if (bundle != null) {
                if (bundle.getState() == Bundle.RESOLVED) {
                    bundle.start();
                }
                BundleContext context = bundle.getBundleContext();
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
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return services;
    }

    /**
     * {@inheritDoc}
     */
    public <T> void addServiceListener(Class<T> serviceInterface, ServiceListener<T> listener) {
        synchronized (_listeners) {
            _listeners.put(listener, new ServiceListenerAdapter<T>(serviceInterface, listener));
        }
    }

    /**
     * {@inheritDoc}
     */
    public <T> void removeServiceListener(ServiceListener<T> listener) {
        synchronized (_listeners) {
            @SuppressWarnings("unchecked")
            ServiceListenerAdapter<T> adapter=(ServiceListenerAdapter<T>)_listeners.get(listener);
            
            if (adapter != null) {
                adapter.close();
                _listeners.remove(listener);
            }
        }
    }

    /**
     * This class bridges between the OSGi service listener and the commons service listener.
     * 
     */
    public static class ServiceListenerAdapter<T> {
        
        private Class<T> _serviceInterface;
        private ServiceListener<T> _serviceListener;
        
        private org.osgi.framework.ServiceListener _osgiListener=null;
        
        public ServiceListenerAdapter(Class<T> serviceInterface, ServiceListener<T> listener) {
            _serviceInterface = serviceInterface;
            _serviceListener = listener;
            
            init();
        }
        
        @SuppressWarnings("unchecked")
        protected void init() {
            Bundle bundle = FrameworkUtil.getBundle(_serviceInterface);
            if (bundle != null) {
                final BundleContext context = bundle.getBundleContext();
                
                _osgiListener = new org.osgi.framework.ServiceListener() {
                    public void serviceChanged(ServiceEvent ev) {
                        ServiceReference sr = ev.getServiceReference();
                        switch(ev.getType()) {
                        case ServiceEvent.REGISTERED:
                            _serviceListener.registered((T)context.getService(sr));
                            break;
                        case ServiceEvent.UNREGISTERING:
                            _serviceListener.unregistered((T)context.getService(sr));
                            break;
                        default:
                            break;
                        }
                    }           
                };
                
                String filter = "(objectclass=" + _serviceInterface.getName() + ")";
                try {
                    context.addServiceListener(_osgiListener, filter);
                } catch (InvalidSyntaxException e) { 
                    LOG.log(Level.SEVERE, "Failed to add service listener for type '"+_serviceInterface.getName()+"'", e);
                }

                ServiceReference[] srefs;
                try {
                    srefs = context.getServiceReferences(_serviceInterface.getName(), null);
                    
                    if (srefs != null) {
                        for (int i=0; i < srefs.length; i++) {
                            _serviceListener.registered((T)context.getService(srefs[i]));
                        }
                    }
                } catch (InvalidSyntaxException e) {
                    LOG.log(Level.SEVERE, "Failed to get service references for type '"+_serviceInterface.getName()+"'", e);
                }
            }
        }
        
        public void close() {
            
        }
    }
}

