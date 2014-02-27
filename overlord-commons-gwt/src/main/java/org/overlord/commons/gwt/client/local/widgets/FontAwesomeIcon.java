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
package org.overlord.commons.gwt.client.local.widgets;

/**
 * Can be used with font awesome.
 *
 * @author eric.wittmann@redhat.com
 */
public class FontAwesomeIcon extends IconLabel {
    
    /**
     * Constructor.
     */
    public FontAwesomeIcon() {
        getElement().setClassName("fa");
    }
    
    /**
     * Constructor.
     * @param iconName
     */
    public FontAwesomeIcon(String iconName) {
        this();
        getElement().addClassName("fa-" + iconName);
    }
    
    /**
     * Constructor.
     * @param iconName
     * @param fixedWidth
     */
    public FontAwesomeIcon(String iconName, boolean fixedWidth) {
        this(iconName);
        if (fixedWidth)
            getElement().addClassName("fa-fw");
    }
    
    /**
     * @param iconName
     */
    public void setIconName(String iconName) {
        getElement().addClassName("fa-" + iconName);
    }

    /**
     * @param iconName
     */
    public void setFixedWidth(boolean fixedWidth) {
        if (fixedWidth)
            getElement().addClassName("fa-fw");
        else
            getElement().removeClassName("fa-fw");
    }
}
