/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
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

package org.gearvrf.io.cursor3d;

import org.gearvrf.GVRContext;
import org.gearvrf.io.cursor3d.CursorAsset.Action;
import org.gearvrf.utility.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;

class CursorThemeFactory {

    private static final String TAG = CursorTheme.class.getSimpleName();
    // XML Attributes
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String TYPE = "cursorType";
    // XML Elements in a theme
    private static final String ASSET = "asset";

    private static final String XML_START_TAG = "<theme ";
    private static final String XML_END_TAG = "</theme";

    static CursorTheme readTheme(XmlPullParser parser, GVRContext context) throws
            XmlPullParserException,
            IOException {
        CursorTheme cursorTheme = new CursorTheme();

        cursorTheme.setId(parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, ID));
        cursorTheme.setName(parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, NAME));
        cursorTheme.setDescription(parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE,
                DESCRIPTION));
        cursorTheme.setType(XMLUtils.getEnumFromString(CursorType.class, parser.getAttributeValue
                (null, TYPE)));

        if (cursorTheme.getId() == null) {
            throw new XmlPullParserException("Theme id needs to be defined");
        }
        if (cursorTheme.getName() == null) {
            throw new XmlPullParserException("Theme name needs to be defined");
        }

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() == XmlPullParser.TEXT) {
                continue;
            } else if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("Cannot find start tag");
            }
            String name = parser.getName();
            Log.d(TAG, "Reading tag:" + name);
            // Starts by looking for the entry tag
            CursorType cursorType = cursorTheme.getCursorType();
            if (name.equals(ASSET)) {
                cursorTheme.addAsset(CursorAssetFactory.readAsset(parser, context, cursorType));
            } else {
                throw new XmlPullParserException("Illegal start tag inside theme");
            }
        }

        if (cursorTheme.getAsset(Action.DEFAULT) == null) {
            throw new IllegalArgumentException("Asset for Default action not specified in theme:"
                    + cursorTheme.getId());
        }
        return cursorTheme;
    }

    //TODO use XmlSerializer
    static void writeCursorTheme(CursorTheme theme, BufferedWriter writer) throws IOException {
        writer.write(XML_START_TAG);
        XMLUtils.writeXmlAttribute(ID, theme.getId(), writer);
        XMLUtils.writeXmlAttribute(NAME, theme.getName(), writer);
        XMLUtils.writeXmlAttribute(TYPE, theme.getType(), writer);

        String description = theme.getDescription();
        if (description != null) {
            XMLUtils.writeXmlAttribute(DESCRIPTION, description, writer);
        }
        writer.write(XMLUtils.CLOSING_BRACE);
        for (CursorAsset asset : theme.getAssets()) {
            CursorAssetFactory.writeCursorAsset(asset, writer);
        }
        writer.write(XML_END_TAG);
        writer.write(XMLUtils.CLOSING_BRACE);
    }

    static String getXMLString(CursorTheme theme) {
        StringWriter stringWriter = new StringWriter();
        BufferedWriter writer = new BufferedWriter(stringWriter);
        try {
            writeCursorTheme(theme, writer);
            writer.flush();
        } catch (IOException e) {
            Log.d(TAG, "Could not convert theme to string:", e);
        }
        return stringWriter.toString();
    }
}
