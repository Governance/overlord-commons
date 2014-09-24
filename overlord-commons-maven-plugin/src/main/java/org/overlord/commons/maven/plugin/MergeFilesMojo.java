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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * A mojo that can merge files.
 *
 * @author David Virgil Naranjo
 */
@Mojo(name = "merge-files", requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class MergeFilesMojo extends AbstractMojo {

    @Parameter(property = "merges", required = true)
    private Merge[] merges;

    /*
     * (non-Javadoc)
     *
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        for (Merge mergeData : merges) {
            final Properties properties = new Properties();
            InputStream is = null;
            OutputStream os = null;
            for (File propertiesFile : mergeData.getFiles()) {
                if (!propertiesFile.exists())
                    throw new MojoExecutionException("File does not exist: " + propertiesFile.getAbsolutePath());
                try {
                    is = new FileInputStream(propertiesFile);
                    properties.load(is);
                } catch (Exception e) {
                    throw new MojoExecutionException("Exception while loading the file " + propertiesFile.getAbsolutePath(), e);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            File destination = mergeData.getDestination();
            try {
                os = new FileOutputStream(destination);
                properties.store(os, destination.getName());
            } catch (Exception e) {
                throw new MojoExecutionException("Exception writing in the file " + destination.getName(), e);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}