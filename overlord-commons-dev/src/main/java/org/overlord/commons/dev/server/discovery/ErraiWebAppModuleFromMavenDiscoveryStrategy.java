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
package org.overlord.commons.dev.server.discovery;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.overlord.commons.dev.server.DevServerModule;

/**
 *
 * @author eric.wittmann@redhat.com
 */
public class ErraiWebAppModuleFromMavenDiscoveryStrategy extends WebAppModuleFromMavenDiscoveryStrategy {

    /**
     * Constructor.
     */
    public ErraiWebAppModuleFromMavenDiscoveryStrategy(Class<?> someClass) {
        super(someClass);
    }

    /**
     * @see org.overlord.commons.dev.server.discovery.WebAppModuleFromMavenDiscoveryStrategy#discover(org.overlord.commons.dev.server.discovery.ModuleDiscoveryContext)
     */
    @Override
    public DevServerModule discover(ModuleDiscoveryContext context) {
        try {
            DevServerModule module = super.discover(context);
            if (module != null) {
                FileUtils.deleteDirectory(new File(module.getWorkDir(), "WEB-INF/lib"));
                FileUtils.deleteDirectory(new File(module.getWorkDir(), "WEB-INF/classes/org/overlord/sramp/ui/client/local"));
            }
            return module;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
