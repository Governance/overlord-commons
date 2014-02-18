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
package org.overlord.commons.gwt.client.local.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * Label with a 'p' tag as the root element instead of a div or a span.
 *
 * @author eric.wittmann@redhat.com
 */
public class ParagraphLabel extends Widget implements HasText {

    /**
     * Creates an empty label.
     */
    public ParagraphLabel() {
      setElement(Document.get().createPElement());
    }

    /**
     * Creates a label with the specified text.
     *
     * @param text the new label's text
     */
    public ParagraphLabel(String text) {
      this();
      setText(text);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#getText()
     */
    @Override
    public String getText() {
        return getElement().getInnerText();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
     */
    @Override
    public void setText(String text) {
        getElement().setInnerText(text);
    }

}
