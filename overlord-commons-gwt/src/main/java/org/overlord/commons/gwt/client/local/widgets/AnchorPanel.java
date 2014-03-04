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

import java.util.ArrayList;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Just like a FlowPanel except parented by an anchor (a) rather than a div.
 * 
 * @author eric.wittmann@redhat.com
 */
public class AnchorPanel extends ComplexPanel implements InsertPanel.ForIsWidget {
    
    private ArrayList<Widget> children = new ArrayList<Widget>();
    
    /**
     * Creates an empty flow panel.
     */
    public AnchorPanel() {
        setElement(DOM.createAnchor());
    }

    /**
     * Adds a new child widget to the panel.
     * 
     * @param w
     *            the widget to be added
     */
    @Override
    public void add(Widget w) {
        add(w, getElement());
        children.add(w);
    }

    /**
     * @see com.google.gwt.user.client.ui.Panel#clear()
     */
    @Override
    public void clear() {
        for (Widget child : children) {
            remove(child);
        }
        children.clear();
    }

    /**
     * @see com.google.gwt.user.client.ui.InsertPanel.ForIsWidget#insert(com.google.gwt.user.client.ui.IsWidget, int)
     */
    @Override
    public void insert(IsWidget w, int beforeIndex) {
        insert(asWidgetOrNull(w), beforeIndex);
    }

    /**
     * Inserts a widget before the specified index.
     * 
     * @param w
     *            the widget to be inserted
     * @param beforeIndex
     *            the index before which it will be inserted
     * @throws IndexOutOfBoundsException
     *             if <code>beforeIndex</code> is out of range
     */
    public void insert(Widget w, int beforeIndex) {
        insert(w, getElement(), beforeIndex, true);
        children.add(beforeIndex, w.asWidget());
    }
}
