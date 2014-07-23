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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoAttributes;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoContent;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * Parses the XML in the standalone.xml in the overlord configuration subsystem.
 */
public class SubsystemParser_1_0 implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    public static final SubsystemParser_1_0 INSTANCE = new SubsystemParser_1_0();
    
    /**
     * Constructor.
     */
    public SubsystemParser_1_0() {
    }
    
    /**
     * @see org.jboss.staxmapper.XMLElementReader#readElement(org.jboss.staxmapper.XMLExtendedStreamReader, java.lang.Object)
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
                case OVERLORD_CONFIGURATION_1_0: {
                    final Element element = Element.forName(reader.getLocalName());
                    switch (element) {
                        case CONFIGURATIONS: {
                            parseConfigurations(reader, address, list);
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

    /**
     * Parses all the configurations.
     * @param reader
     * @param parentAddress
     * @param list
     * @throws XMLStreamException
     */
    public void parseConfigurations(final XMLExtendedStreamReader reader, final ModelNode parentAddress, final List<ModelNode> list) throws XMLStreamException {
        requireNoAttributes(reader);
        
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case OVERLORD_CONFIGURATION_1_0: {
                    final Element element = Element.forName(reader.getLocalName());
                    switch (element) {
                        case CONFIGURATION: {
                            parseConfiguration(reader, parentAddress, list);
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
    
    /**
     * Parses the configuration element.
     * @param reader
     * @param parentAddress
     * @param list
     * @throws XMLStreamException
     */
    public void parseConfiguration(final XMLExtendedStreamReader reader, final ModelNode parentAddress,
            final List<ModelNode> list) throws XMLStreamException {
        String name = null;

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
                    ConfigurationDefinition.NAME.parseAndSetParameter(value, operation, reader);
                    break;
                }
                default:
                    throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }
        if (name == null) {
            ParseUtils.missingRequired(reader, Collections.singleton(Constants.ATTRIBUTE_NAME));
        }
        final ModelNode address = parentAddress.clone();
        address.add(Constants.MODEL_CONFIGURATION, name);
        address.protect();
        
        operation.get(OP_ADDR).set(address);
        list.add(operation);

        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case OVERLORD_CONFIGURATION_1_0: {
                    final Element element = Element.forName(reader.getLocalName());
                    switch (element) {
                        case PROPERTIES: {
                            parseProperties(reader, address, list);
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

    /**
     * Parses the properties element.
     * @param reader
     * @param parentAddress
     * @param list
     * @throws XMLStreamException
     */
    public void parseProperties(final XMLExtendedStreamReader reader, final ModelNode parentAddress, final List<ModelNode> list) throws XMLStreamException {
        requireNoAttributes(reader);
        
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case OVERLORD_CONFIGURATION_1_0: {
                    final Element element = Element.forName(reader.getLocalName());
                    switch (element) {
                        case PROPERTY: {
                            parseProperty(reader, parentAddress, list);
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
    
    /**
     * Parses as ingle property.
     * @param reader
     * @param parentAddress
     * @param list
     * @throws XMLStreamException
     */
    public void parseProperty(final XMLExtendedStreamReader reader, final ModelNode parentAddress, final List<ModelNode> list) throws XMLStreamException {
        String propertyName = null;
        String propertyValue = null;

        final ModelNode operation = new ModelNode();
        operation.get(OP).set(ADD);
        final int attrCount = reader.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case NAME: {
                    propertyName = value;
                    ConfigurationPropertyDefinition.NAME.parseAndSetParameter(value, operation, reader);
                    break;
                }
                case VALUE: {
                    propertyValue = value;
                    ConfigurationPropertyDefinition.VALUE.parseAndSetParameter(value, operation, reader);
                    break;
                }
                default:
                    throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }
        if (propertyName == null) {
            ParseUtils.missingRequired(reader, Collections.singleton(Constants.ATTRIBUTE_NAME));
        }
        if (propertyValue == null) {
            ParseUtils.missingRequired(reader, Collections.singleton(Constants.ATTRIBUTE_VALUE));
        }
        final ModelNode address = parentAddress.clone();
        address.add(Constants.MODEL_PROPERTY, propertyName);
        address.protect();
        
        operation.get(OP_ADDR).set(address);
        list.add(operation);

        requireNoContent(reader);
    }

    /**
     * @see org.jboss.staxmapper.XMLElementWriter#writeContent(org.jboss.staxmapper.XMLExtendedStreamWriter, java.lang.Object)
     */
    @Override
    public void writeContent(final XMLExtendedStreamWriter streamWriter, final SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(Namespace.CURRENT.getUriString(), false);
        
        final ModelNode node = context.getModelNode();
        streamWriter.writeStartElement(Constants.ELEMENT_CONFIGURATIONS);
        
        if (node.hasDefined(Constants.MODEL_CONFIGURATION)) {
            final List<ModelNode> configurations = node.get(Constants.MODEL_CONFIGURATION).asList();
            for (ModelNode configuration: configurations) {
                ModelNode configChild = configuration.get(0);
                
                streamWriter.writeStartElement(Constants.ELEMENT_CONFIGURATION);
                
                writeAttributeIfDefined(streamWriter, configChild, Constants.ATTRIBUTE_NAME);
                
                if (configChild.hasDefined(Constants.MODEL_PROPERTY)) {
                    final List<ModelNode> properties = configChild.get(Constants.MODEL_PROPERTY).asList();
                    for (ModelNode property: properties) {
                        streamWriter.writeStartElement(Constants.ELEMENT_PROPERTY);
                        
                        ModelNode propertyChild = property.get(0);
                        writeAttributeIfDefined(streamWriter, propertyChild, Constants.ATTRIBUTE_NAME);
                        writeAttributeIfDefined(streamWriter, propertyChild, Constants.ATTRIBUTE_VALUE);
                        
                        streamWriter.writeEndElement();
                    }
                    
                }
                streamWriter.writeEndElement();
            }
        }
        
        streamWriter.writeEndElement();
        streamWriter.writeEndElement();
    }

    /**
     * Writes an attribute to the output, but only if it's defined.
     * @param streamWriter
     * @param node
     * @param name
     * @throws XMLStreamException
     */
    private void writeAttributeIfDefined(final XMLExtendedStreamWriter streamWriter, final ModelNode node,
            final String name) throws XMLStreamException {
        if (node.has(name)) {
            final ModelNode attr = node.get(name);
            if (attr.isDefined()) {
                streamWriter.writeAttribute(name, attr.asString());
            }
        }
    }
}
