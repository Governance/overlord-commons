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

package org.overlord.commons.eap.extensions.config;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.dmr.ModelNode;

/**
 * The FSW configuration subsystem add update handler.
 *
 * @author Kevin Conner
 */
public class SubsystemAdd extends AbstractAddStepHandler {

    static final SubsystemAdd INSTANCE = new SubsystemAdd();

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) {
        model.get(Constants.MODEL_CONFIGURATION).setEmptyObject();
    }

    protected boolean requiresRuntimeVerification() {
        return false;
    }
}
