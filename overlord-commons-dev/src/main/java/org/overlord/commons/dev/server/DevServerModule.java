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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * A single module running in a dev server.
 * @author eric.wittmann@redhat.com
 */
public class DevServerModule {

    private File moduleDir;
    private File workDir;
    private boolean inIDE;

    /**
     * Constructor.
     */
    public DevServerModule() {
    }

    /**
     * Cleans the module's work dir (if it has one).
     */
    public void clean() {
        if (workDir != null) {
            try { FileUtils.deleteDirectory(workDir); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    /**
     * @return the moduleDir
     */
    public File getModuleDir() {
        return moduleDir;
    }

    /**
     * @param moduleDir the moduleDir to set
     */
    public void setModuleDir(File moduleDir) {
        this.moduleDir = moduleDir;
    }

    /**
     * @return the workDir
     */
    public File getWorkDir() {
        return workDir;
    }

    /**
     * @param workDir the workDir to set
     */
    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    /**
     * @return the inIDE
     */
    public boolean isInIDE() {
        return inIDE;
    }

    /**
     * @param inIDE the inIDE to set
     */
    public void setInIDE(boolean inIDE) {
        this.inIDE = inIDE;
    }

}
