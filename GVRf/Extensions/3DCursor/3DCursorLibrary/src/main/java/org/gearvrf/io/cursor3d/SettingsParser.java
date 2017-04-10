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

import android.content.Context;

import org.gearvrf.GVRContext;
import org.gearvrf.utility.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

class SettingsParser {
    private static final String TAG = SettingsParser.class.getSimpleName();
    private static final String FILE_ENCODING = "UTF-8";
    // XML Elements
    private static final String SETTINGS = "settings";
    private static final String THEME = "theme";
    private static final String CURSOR = "cursor";
    private static final String GLOBAL = "global";
    private static final String XML_START_TAG = "<settings>";
    private static final String XML_END_TAG = "</settings>";

    static void parseSettings(GVRContext context, CursorManager cursorManager)
            throws XmlPullParserException, IOException {
        InputStream in = getSettingsStream(context.getContext());
        try {
            XmlPullParser parser = XMLUtils.initiateParser(in);
            readSettings(parser, context, cursorManager);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private static InputStream getSettingsStream(Context context) throws IOException {

        File settingsFile = new File(context.getFilesDir() + "/" + CursorManager.SETTINGS_SOURCE);
        if (settingsFile.exists()) {
            Log.d(TAG, "The file exists");
            return context.openFileInput(CursorManager.SETTINGS_SOURCE);
        } else {
            Log.d(TAG, "The file does not exist, opening " + CursorManager.SETTINGS_SOURCE);
            return context.getAssets().open(CursorManager.SETTINGS_SOURCE);
        }
    }

    //TODO: use a utility to read XML and convert to Object and use Objects to write XML
    private static void readSettings(XmlPullParser parser, GVRContext context, CursorManager
            cursorManager) throws XmlPullParserException, IOException {

        Map<String, CursorTheme> themes = cursorManager.getThemeMap();
        List<Cursor> cursors = cursorManager.getUnusedCursors();

        parser.require(XmlPullParser.START_TAG, null, SETTINGS);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() == XmlPullParser.TEXT) {
                continue;
            } else if (parser.getEventType() != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("Cannot find start tag");
            }
            String name = parser.getName();
            Log.d(TAG, "Reading tag:" + name);
            // Starts by looking for the entry tag
            if (name.equals(GLOBAL)) {
                GlobalSettingsFactory.readGlobalSettings(parser);
            } else if (name.equals(THEME)) {
                Log.d(TAG, "Reading the theme tag");
                CursorTheme theme = CursorThemeFactory.readTheme(parser, context);
                themes.put(theme.getId(), theme);
            } else if (name.equals(CURSOR)) {
                Log.d(TAG, "Reading the cursor tag");
                cursors.add(CursorFactory.readCursor(parser, context, cursorManager));
            }
        }
        if (cursors.size() == 0) {
            throw new XmlPullParserException("No cursors specified in settings.xml");
        }
    }

    static void saveSettings(Context context, CursorManager cursorManager) throws IOException {
        Map<String, CursorTheme> themes = cursorManager.getThemeMap();
        List<Cursor> unusedCursors = cursorManager.getUnusedCursors();
        List<Cursor> cursors = cursorManager.getActiveCursors();
        BufferedWriter writer = null;
        try {
            FileOutputStream outputStream = context.openFileOutput(CursorManager.SETTINGS_SOURCE,
                    Context.MODE_PRIVATE);
            OutputStreamWriter ow = new OutputStreamWriter(outputStream, FILE_ENCODING);
            writer = new BufferedWriter(ow);
            writer.write(XML_START_TAG);
            GlobalSettingsFactory.writeGlobalSettings(writer);
            for (CursorTheme theme : themes.values()) {
                CursorThemeFactory.writeCursorTheme(theme, writer);
            }
            for (Cursor cursor : cursors) {
                CursorFactory.writeCursor(cursor, writer);
            }
            for (Cursor cursor : unusedCursors) {
                CursorFactory.writeCursor(cursor, writer);
            }
            writer.write(XML_END_TAG);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}