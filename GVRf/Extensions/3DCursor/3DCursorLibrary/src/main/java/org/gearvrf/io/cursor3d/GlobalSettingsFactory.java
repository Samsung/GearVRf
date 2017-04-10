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

import org.gearvrf.utility.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedWriter;
import java.io.IOException;

class GlobalSettingsFactory {
    private static final String TAG = GlobalSettingsFactory.class.getSimpleName();
    private static final String XML_START_TAG = "<global ";
    private static final String SOUND_ENABLED = "soundEnabled";
    private static final String ONSCREEN = "onScreen";
    private static final String PREVIEW = "preview";

    static void readGlobalSettings(XmlPullParser parser) throws
            XmlPullParserException,
            IOException {
        String soundEnabled = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE,
                SOUND_ENABLED);
        if (soundEnabled == null) {
            throw new XmlPullParserException("soundEnabled not specified");
        }
        Log.d(TAG, "Reading the global tag soundEnabled:" + soundEnabled);
        GlobalSettings globalSettings = GlobalSettings.getInstance();
        globalSettings.setSoundEnabled(XMLUtils.parseBoolean(soundEnabled));

        String onScreen = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, ONSCREEN);
        if (onScreen == null) {
            globalSettings.setOnScreen(false);
        } else {
            globalSettings.setOnScreen(XMLUtils.parseBoolean(onScreen));
        }

        String preview = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, PREVIEW);
        if (preview == null) {
            globalSettings.setPreview(false);
        } else {
            globalSettings.setPreview(XMLUtils.parseBoolean(preview));
        }

        XMLUtils.parseTillElementEnd(parser);
    }

    //TODO use XmlSerializer
    static void writeGlobalSettings(BufferedWriter writer) throws IOException {
        GlobalSettings globalSettings = GlobalSettings.getInstance();
        writer.write(XML_START_TAG);
        XMLUtils.writeXmlAttribute(SOUND_ENABLED, XMLUtils.xmlFromBoolean
                (globalSettings.isSoundEnabled()), writer);
        XMLUtils.writeXmlAttribute(ONSCREEN, XMLUtils.xmlFromBoolean(globalSettings.isOnScreen())
                , writer);
        XMLUtils.writeXmlAttribute(PREVIEW, XMLUtils.xmlFromBoolean(globalSettings.getPreview()),
                writer);
        writer.write(XMLUtils.ELEMENT_END);
    }
}