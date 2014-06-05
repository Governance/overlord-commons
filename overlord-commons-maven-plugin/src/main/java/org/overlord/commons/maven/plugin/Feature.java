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

package org.overlord.commons.maven.plugin;

import java.util.List;

/**
 * Simple java bean modelling a single feature listed in the configuration of the 
 * {@link GenerateFeaturesXmlMojo} plugin.
 *
 * @author eric.wittmann@redhat.com
 */
public class Feature {

    private String name;
    private String version;
    private String comment;
    private String startLevel;
    private List<Feature> dependsOnFeatures;
    private List<String> includes;
    private List<String> excludes;
    private List<String> bundles;
    
    /**
     * Constructor.
     */
    public Feature() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the dependsOnFeatures
     */
    public List<Feature> getDependsOnFeatures() {
        return dependsOnFeatures;
    }

    /**
     * @param dependsOnFeatures the dependsOnFeatures to set
     */
    public void setDependsOnFeatures(List<Feature> dependsOnFeatures) {
        this.dependsOnFeatures = dependsOnFeatures;
    }

    /**
     * @return the includes
     */
    public List<String> getIncludes() {
        return includes;
    }

    /**
     * @param includes the includes to set
     */
    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    /**
     * @return the excludes
     */
    public List<String> getExcludes() {
        return excludes;
    }

    /**
     * @param excludes the excludes to set
     */
    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the bundles
     */
    public List<String> getBundles() {
        return bundles;
    }

    /**
     * @param bundles the bundles to set
     */
    public void setBundles(List<String> bundles) {
        this.bundles = bundles;
    }

    /**
     * @return the startLevel
     */
    public String getStartLevel() {
        return startLevel;
    }

    /**
     * @param startLevel the startLevel to set
     */
    public void setStartLevel(String startLevel) {
        this.startLevel = startLevel;
    }
    
}
