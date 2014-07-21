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

package org.overlord.commons.eap.extensions.deploy;

import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import java.util.Collections;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoAttributes;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoContent;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

/**
 */
public class SubsystemParser_1_0 implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    public static final SubsystemParser_1_0 INSTANCE = new SubsystemParser_1_0();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {
        requireNoAttributes(reader);
        
        final ModelNode address = new ModelNode();
        address.add(ModelDescriptionConstants.SUBSYSTEM, SubsystemExtension.SUBSYSTEM_NAME);
        address.protect();

        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).set(address);
        list.add(subsystem);
  
        // elements
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case OVERLORD_DEPLOYMENT_1_0: {
                    final Element element = Element.forName(reader.getLocalName());
                    switch (element) {
                        case DEPLOYMENTS: {
                            parseDeployments(reader, address, list);
                            break;
                        }
                        default:
                            throw unexpectedElement(reader);
                    }
                    break;
                }
                default:
                    throw unexpectedElement(reader);
            }
        }
    }

    public void parseDeployments(final XMLExtendedStreamReader reader, final ModelNode parentAddress, final List<ModelNode> list) throws XMLStreamException {
        requireNoAttributes(reader);
        
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case OVERLORD_DEPLOYMENT_1_0: {
                    final Element element = Element.forName(reader.getLocalName());
                    switch (element) {
                        case DEPLOYMENT: {
                            parseDeployment(reader, parentAddress, list);
                            break;
                        }
                        default:
                            throw unexpectedElement(reader);
                    }
                    break;
                }
                default:
                    throw unexpectedElement(reader);
            }
        }
    }
    
    public void parseDeployment(final XMLExtendedStreamReader reader, final ModelNode parentAddress, final List<ModelNode> list) throws XMLStreamException {
        String name = null;
        String module = null;

        final ModelNode operation = new ModelNode();
        operation.get(OP).set(ADD);
        final int attrCount = reader.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case NAME: {
                    name = value;
                    DeploymentDefinition.NAME.parseAndSetParameter(value, operation, reader);
                    break;
                }
                case MODULE: {
                    module = value;
                    DeploymentDefinition.MODULE.parseAndSetParameter(value, operation, reader);
                    break;
                }
                case VERSION: {
                    DeploymentDefinition.VERSION.parseAndSetParameter(value, operation, reader);
                    break;
                }
                default:
                    throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }
        if (name == null) {
            ParseUtils.missingRequired(reader, Collections.singleton(Constants.ATTRIBUTE_NAME));
        }
        if (module == null) {
            ParseUtils.missingRequired(reader, Collections.singleton(Constants.ATTRIBUTE_MODULE));
        }
        final ModelNode address = parentAddress.clone();
        address.add(Constants.MODEL_DEPLOYMENT, name);
        address.protect();
        
        operation.get(OP_ADDR).set(address);
        list.add(operation);
        requireNoContent(reader);
    }
    
    /**
     * {@inheritDoc}
     * */
    @Override
    public void writeContent(final XMLExtendedStreamWriter streamWriter, final SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(Namespace.CURRENT.getUriString(), false);
        
        final ModelNode node = context.getModelNode();
        streamWriter.writeStartElement(Constants.ELEMENT_DEPLOYMENTS);
        
        if (node.hasDefined(Constants.MODEL_DEPLOYMENT)) {
            final List<ModelNode> deployments = node.get(Constants.MODEL_DEPLOYMENT).asList();
            for (ModelNode deployment: deployments) {
                streamWriter.writeStartElement(Constants.ELEMENT_DEPLOYMENT);
                
                writeAttributeIfDefined(streamWriter, deployment, Constants.ATTRIBUTE_NAME);
                writeAttributeIfDefined(streamWriter, deployment, Constants.ATTRIBUTE_MODULE);
                writeAttributeIfDefined(streamWriter, deployment, Constants.ATTRIBUTE_VERSION);
                
                streamWriter.writeEndElement();
            }
        }
        
        streamWriter.writeEndElement();
        streamWriter.writeEndElement();
    }

    private void writeAttributeIfDefined(final XMLExtendedStreamWriter streamWriter,
        final ModelNode node, final String name)
        throws XMLStreamException {
        if (node.has(name)) {
            final ModelNode attr = node.get(name);
            if (attr.isDefined()) {
                streamWriter.writeAttribute(name, attr.asString());
            }
        }
    }
}
