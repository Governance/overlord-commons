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

package org.overlord.commons.osgi.weld;

import javax.servlet.ServletContext;

import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.environment.servlet.deployment.URLScanner;

/**
 * Extends the Weld Listener to override the {@link URLScanner} used.  The
 * scanner provided is one that can operate in an OSGi environment.
 * 
 * @author eric.wittmann@redhat.com
 */
public class BundleListener extends Listener {

    /**
     * Constructor.
     */
    public BundleListener() {
    }

    /**
     * @see org.jboss.weld.environment.servlet.Listener#createUrlScanner(java.lang.ClassLoader,
     *      javax.servlet.ServletContext)
     */
    @Override
    protected URLScanner createUrlScanner(ClassLoader classLoader, ServletContext context) {
        return new BundleURLScanner(classLoader, context);
    }

}
