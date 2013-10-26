/*
 * Copyright 2013 YTEQ Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fixb.quickfix;

import com.google.common.collect.ImmutableMap;
import org.fixb.meta.*;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.FixVersions;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.fixb.FixConstants.BEGIN_STRING_TAG;
import static org.fixb.FixConstants.MSG_TYPE_TAG;

/**
 * A QuickFIX/J DataDictionary based on the metadata provided by a FixMetaDictionary.
 *
 * @author vladyslav.yatsenko
 * @see org.fixb.meta.MutableFixMetaDictionary
 */
public class FixMetaDataDictionary extends DataDictionary {
    private static final String FIXT_PREFIX = "FIXT";
    private static final String FIX = "fix";
    private static final String HEADER = "header";
    private static final String TRAILER = "trailer";
    private static final String MESSAGES = "messages";
    private static final String COMPONENTS = "components";
    private static final String FIELDS = "fields";
    private static final String GROUP = "group";
    private static final String PREAMBULE_TYPE = "type";
    private static final String VERSION_MAJOR = "major";
    private static final String VERSION_MINOR = "minor";
    private static final String MESSAGE = "message";
    private static final String MSGTYPE = "msgtype";
    private static final String MSGCAT = "msgcat";
    private static final String MSGCAT_APP = "app";
    private static final String COMPONENT = "component";
    private static final String REQUIRED = "required";
    private static final String FIELD = "field";
    private static final String NUMBER = "number";
    private static final String TYPE = "type";
    private static final String NAME = "name";
    private static final String TYPE_STRING = "STRING";
    private static final String TYPE_GROUP_NUM = "NUMINGROUP";
    private static final String YES = "Y";
    private static final String NO = "N";
    private static final String GROUP_NAME_PREFIX = "No";
    private static final String COMPONENT_NAME_SUFFIX = "Gr";
    private static final Map<Integer, String> HEADER_FIELDS = ImmutableMap.of(
            8, "BeginString",
            9, "BodyLength",
            35, "MsgType"
    );

    private static final Map<Integer, String> TRAILER_FIELDS = ImmutableMap.of(10, "CheckSum");

    public FixMetaDataDictionary(final String fixProtocolVersion, FixMetaDictionary fixMetaRepository) throws ConfigError {
        super(generateDictionaryXml(fixProtocolVersion, fixMetaRepository));
        setCheckFieldsOutOfOrder(false);
        setCheckUnorderedGroupFields(false);
    }

    private static InputStream generateDictionaryXml(final String fixProtocolVersion, FixMetaDictionary fixMetaRepository) {
        final Map<FixFieldMeta, String> fields = new LinkedHashMap<FixFieldMeta, String>();
        final Map<Integer, String> fieldNames = new LinkedHashMap<Integer, String>();
        final XMLOutputFactory factory = XMLOutputFactory.newInstance();

        final StringWriter stream = new StringWriter();
        try {
            addStandardHeaderAndTrailerFields(fields, fieldNames);

            XMLStreamWriter writer = factory.createXMLStreamWriter(stream);

            writer.writeStartDocument();

            writer.writeStartElement(FIX);
            writeFixPreambule(writer, fixProtocolVersion);
            writeHeader(writer, fixProtocolVersion, null);

            writer.writeStartElement(MESSAGES);
            for (FixMessageMeta<?> message : fixMetaRepository.getAllMessageMetas()) {
                writeMessageXml(writer, message, fields, fieldNames);
            }
            writer.writeEndElement();

            writer.writeStartElement(COMPONENTS);
            for (FixMessageMeta<?> message : fixMetaRepository.getAllMessageMetas()) {
                writeComponentXml(writer, message, fields, fieldNames);
            }
            writer.writeEndElement();

            writer.writeStartElement(FIELDS);
            for (Map.Entry<FixFieldMeta, String> entry : fields.entrySet()) {
                final FixFieldMeta field = entry.getKey();
                if (fieldNames.containsKey(field.getTag())) {
                    writeFieldXml(writer, field, entry.getValue(), fieldNames);
                }
            }
            writer.writeEndElement();

            writer.writeEndElement();
            writer.writeEndDocument();

            writer.flush();
            writer.close();

        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(stream.toString().getBytes());
    }

    private static void addStandardHeaderAndTrailerFields(final Map<FixFieldMeta, String> fields,
                                                          final Map<Integer, String> fieldNames) throws XMLStreamException {
        for (Map.Entry<Integer, String> field : HEADER_FIELDS.entrySet()) {
            fields.put(new FixConstantFieldMeta(field.getKey(), true, ""), field.getValue());
            fieldNames.put(field.getKey(), field.getValue());
        }
        for (Map.Entry<Integer, String> field : TRAILER_FIELDS.entrySet()) {
            fields.put(new FixConstantFieldMeta(field.getKey(), true, ""), field.getValue());
            fieldNames.put(field.getKey(), field.getValue());
        }
    }

    private static void writeHeader(final XMLStreamWriter writer, final String fixProtocolVersion, FixMetaDictionary fixMetaRepository) throws XMLStreamException {
        if (isLessThan50Version(fixProtocolVersion)) {
            writer.writeStartElement(HEADER);
            for (Map.Entry<Integer, String> field : HEADER_FIELDS.entrySet()) {
                writer.writeStartElement(FIELD);
                writer.writeAttribute(NAME, field.getValue());
                writer.writeAttribute(REQUIRED, YES);
                writer.writeEndElement();
            }
            for (FixMessageMeta<?> message : fixMetaRepository.getAllMessageMetas()) {
                for (FixFieldMeta field : message.getFields()) {
                    if (field.isHeader() && field.getTag() != MSG_TYPE_TAG) {
                        writer.writeStartElement(FIELD);
                        writer.writeAttribute(NAME, getFieldName(field));
                        writer.writeAttribute(REQUIRED, field.isOptional() ? NO : YES);
                        writer.writeEndElement();
                    }
                }
            }
            writer.writeEndElement();
            writer.writeStartElement(TRAILER);
            for (Map.Entry<Integer, String> field : TRAILER_FIELDS.entrySet()) {
                writer.writeStartElement(FIELD);
                writer.writeAttribute(NAME, field.getValue());
                writer.writeAttribute(REQUIRED, YES);
                writer.writeEndElement();
            }
            writer.writeEndElement();
        } else {
            writer.writeEmptyElement(HEADER_ID);
        }
    }

    private static boolean isLessThan50Version(final String fixProtocolVersion) {
        return fixProtocolVersion.startsWith(FIXT_PREFIX) || fixProtocolVersion.compareTo(FixVersions.FIX50) < 0;
    }

    private static void writeComponentXml(final XMLStreamWriter writer,
                                          final FixMessageMeta<?> message,
                                          final Map<FixFieldMeta, String> fields,
                                          final Map<Integer, String> fieldNames) throws XMLStreamException {
        for (FixFieldMeta field : message.getFields()) {
            if (field.isGroup()) {
                FixGroupMeta group = (FixGroupMeta) field;
                writer.writeStartElement(COMPONENT);
                final String fieldName = getFieldName(field);
                final String componentName = fieldName + COMPONENT_NAME_SUFFIX;
                writer.writeAttribute(NAME, componentName);
                writer.writeStartElement(GROUP);
                writer.writeAttribute(NAME, GROUP_NAME_PREFIX + fieldName);
                writer.writeAttribute(REQUIRED, group.isOptional() ? NO : YES);
                if (group.isSimple()) {
                    writer.writeEmptyElement(FIELD);
                    writer.writeAttribute(NAME, getAnonymousFieldName(group.getComponentTag()));
                    writer.writeAttribute(REQUIRED, group.isOptional() ? NO : YES);
                } else {
                    writeComponentFields(writer, group.getComponentMeta(), fields, fieldNames);
                }
                writer.writeEndElement();
                writer.writeEndElement();
            }
        }
    }

    private static void writeFixPreambule(final XMLStreamWriter writer,
                                          final String fixProtocolVersion) throws XMLStreamException {
        final String[] tokens = fixProtocolVersion.split("\\.");
        if (tokens.length < 3) {
            throw new RuntimeException("Invalid FIX version: " + fixProtocolVersion);
        }
        writer.writeAttribute(PREAMBULE_TYPE, tokens[0]);
        writer.writeAttribute(VERSION_MAJOR, tokens[1]);
        writer.writeAttribute(VERSION_MINOR, tokens[2]);
    }

    private static void writeMessageXml(final XMLStreamWriter writer,
                                        final FixMessageMeta<?> message,
                                        final Map<FixFieldMeta, String> fields,
                                        final Map<Integer, String> fieldNames) throws XMLStreamException {
        writer.writeStartElement(MESSAGE);
        writer.writeAttribute(NAME, message.getType().getSimpleName());
        writer.writeAttribute(MSGTYPE, message.getMessageType());
        writer.writeAttribute(MSGCAT, MSGCAT_APP);
        writeComponentFields(writer, message, fields, fieldNames);
        writer.writeEndElement();
    }

    private static void writeComponentFields(final XMLStreamWriter writer,
                                             final FixBlockMeta<?> message,
                                             final Map<FixFieldMeta, String> fields,
                                             final Map<Integer, String> fieldNames) throws XMLStreamException {
        for (FixFieldMeta field : message.getFields()) {
            if (MSG_TYPE_TAG != field.getTag() && BEGIN_STRING_TAG != field.getTag()) {
                final String fieldName = getFieldName(field);
                writer.writeEmptyElement(field.isGroup() ? COMPONENT : FIELD);
                writer.writeAttribute(NAME, fieldName + (field.isGroup() ? COMPONENT_NAME_SUFFIX : ""));
                writer.writeAttribute(REQUIRED, field.isOptional() ? NO : YES);
                fields.put(field, fieldName);
                fieldNames.put(field.getTag(), fieldName);
                if (field.isGroup() && ((FixGroupMeta) field).isSimple()) {
                    final int componentTag = ((FixGroupMeta) field).getComponentTag();
                    fieldNames.put(componentTag, getAnonymousFieldName(componentTag));
                }
            }
        }
    }

    private static void writeFieldXml(final XMLStreamWriter writer,
                                      final FixFieldMeta field,
                                      final String fieldName,
                                      final Map<Integer, String> fieldNames) throws XMLStreamException {
        writer.writeEmptyElement(FIELD);
        writer.writeAttribute(NUMBER, String.valueOf(field.getTag()));
        writer.writeAttribute(TYPE, getFieldType(field));
        if (field.isGroup()) {
            writer.writeAttribute(NAME, GROUP_NAME_PREFIX + fieldName);
            final FixGroupMeta groupField = (FixGroupMeta) field;
            if (groupField.isSimple() && fieldNames.containsKey(groupField.getComponentTag())) {
                writer.writeEmptyElement(FIELD);
                writer.writeAttribute(NUMBER, String.valueOf(groupField.getComponentTag()));
                writer.writeAttribute(TYPE, TYPE_STRING);
                writer.writeAttribute(NAME, getAnonymousFieldName(groupField.getComponentTag()));
                fieldNames.remove(groupField.getComponentTag());
            }
        } else {
            writer.writeAttribute(NAME, fieldName);
        }
        fieldNames.remove(field.getTag());
    }

    private static String getFieldName(final FixFieldMeta field) {
        return "field" + field.getTag();
    }

    private static String getAnonymousFieldName(final int tag) {
        return "field" + tag;
    }

    private static String getFieldType(final FixFieldMeta field) {
        final String type;
        if (field.isGroup()) {
            type = TYPE_GROUP_NUM;
        } else {
            type = TYPE_STRING;
        }
        return type;
    }
}
