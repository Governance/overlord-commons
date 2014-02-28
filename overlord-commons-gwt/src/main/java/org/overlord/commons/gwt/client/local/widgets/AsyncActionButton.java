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

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.ui.Button;

/**
 * A GWT button that supports the concept of an async action.  When
 * the async action is started, the button changes its state to 
 * show a progress icon (animated) and also alters its text.  When
 * the action completes, the icon is removed and the text reverts
 * back.
 * 
 * This widget also supports Errai templating.  The icon can be 
 * specified in the template using the "data-icon" attribute.  The
 * action text can be specified using the "placeholder" attribute.
 *
 * @author eric.wittmann@redhat.com
 */
public class AsyncActionButton extends Button {
    
    private String html;
    private String actionText;
    private String icon;
    
    /**
     * Constructor.
     */
    public AsyncActionButton() {
        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    initFromTemplate();
                }
            }
        });
    }
    
    /**
     * Called to reset the button to its default state.
     */
    public void reset() {
        setEnabled(true);
        this.setHTML(this.html);
    }
    
    /**
     * Called when the button's async action has been started.  This usually
     * is a result of the user clicking on the button, but it could be triggered
     * in some other way (e.g. the "enter" key being pressed in a search box).
     */
    public void onActionStarted() {
        setEnabled(false);
        this.html = getHTML();
        StringBuilder builder = new StringBuilder();
        builder.append("<i class=\"");
        builder.append("fa fa-spin ");
        builder.append(getIcon());
        builder.append("\"></i> ");
        builder.append(getActionText());
        this.setHTML(builder.toString());
    }
    
    /**
     * Called when the button's async action has completed.
     */
    public void onActionComplete() {
        reset();
    }

    /**
     * Initialize the widget from an Errai template.  This does nothing if the
     * widget has already been initialized manually.
     */
    protected void initFromTemplate() {
        if (actionText == null) {
            this.html = getHTML();
            this.actionText = getElement().getAttribute("placeholder");
            this.icon = getElement().getAttribute("data-icon");
        }
    }
    
    /**
     * @return the actionText
     */
    public String getActionText() {
        return actionText;
    }

    /**
     * @param actionText the actionText to set
     */
    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    /**
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

}
