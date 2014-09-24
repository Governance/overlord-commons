/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.commons.maven.plugin;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Class that encapsulates the merging information
 *
 * @author David Virgil Naranjo
 */
public class Merge {
    @Parameter(property = "destination", required = true)
    private File destination;

    @Parameter(property = "files", required = true)
    private File[] files;



    public File getDestination() {
        return destination;
    }


    public void setDestination(File destination) {
        this.destination = destination;
    }

    /**
     * Gets the files.
     *
     * @return the files
     */
    public File[] getFiles() {
        return files.clone();
    }


}