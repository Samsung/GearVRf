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

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;

class XMLUtils {
    private static final String ATTR_VALUE_PREFIX = "=\"";
    private static final String ATTR_VALUE_POSTFIX = "\" ";
    public static final String ELEMENT_END = "/>\n";
    public static final String CLOSING_BRACE = ">\n";

    private static final String YES = "yes";
    private static final String NO = "no";

    public static final String RAW = "raw";
    public static final String DEFAULT_XML_NAMESPACE = null;

    // taken from http://stackoverflow.com/questions/604424/convert-a-string-to-an-enum-in-java
    //TODO see if convert to Enums in network library is better
    static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) throws
            XmlPullParserException {
        if (c != null && string != null) {
            try {
                T t = Enum.valueOf(c, string.trim().toUpperCase());
                return t;
            } catch (IllegalArgumentException e) {
                throw new XmlPullParserException("Invalid value for " + c.getSimpleName());
            }
        } else {
            throw new XmlPullParserException("Null class or string");
        }
    }

    static void parseTillElementEnd(XmlPullParser parser) throws XmlPullParserException,
            IOException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() == XmlPullParser.TEXT) {
                continue;
            } else {
                throw new XmlPullParserException("Malformed Xml file");
            }
        }
    }

    static boolean parseBoolean(String boolValue) throws XmlPullParserException {
        if (boolValue == null) {
            throw new XmlPullParserException("Invalid yes/no value in xml");
        }
        if (boolValue.equals(YES)) {
            return true;
        } else if (boolValue.equals(NO)) {
            return false;
        } else {
            throw new XmlPullParserException("Invalid yes/no value in xml");
        }
    }

    static String xmlFromBoolean(boolean value) {
        if (value) {
            return YES;
        } else {
            return NO;
        }
    }

    static void writeXmlAttribute(String name, Object value, BufferedWriter writer)
            throws IOException {
        writer.write(name);
        writer.write(XMLUtils.ATTR_VALUE_PREFIX);
        writer.write(value.toString());
        writer.write(XMLUtils.ATTR_VALUE_POSTFIX);
    }

    static XmlPullParser initiateParser(InputStream in) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);
        parser.nextTag();
        return parser;
    }
}
