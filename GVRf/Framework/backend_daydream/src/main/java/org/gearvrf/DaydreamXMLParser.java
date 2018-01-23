/* Copyright 2015 Samsung Electronics Co., LTD
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

package org.gearvrf;

import android.content.res.AssetManager;

import org.gearvrf.io.GVRControllerType;
import org.gearvrf.utility.VrAppSettings;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DaydreamXMLParser {
    DaydreamXMLParser(AssetManager assets, String fileName, VrAppSettings settings) {
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = assets.open(fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    buf.append('\n');
                }
                buf.append(str);
            }

            String file = buf.toString();

            in.close();
            is.close();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(file));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                } else if (eventType == XmlPullParser.START_TAG) {
                    String tagName = xpp.getName();
                    for (int i = 0; i < xpp.getAttributeCount(); ++i) {
                        if (xpp.getAttributeValue(i).equals("DEFAULT")) {
                            continue;
                        }
                        String attributeName = xpp.getAttributeName(i);
                        if (tagName.equals("vr-app-settings"))
                        {
                            if (attributeName.equals("useGazeCursorController"))
                            {
                                settings.setUseGazeCursorController(
                                        Boolean.parseBoolean(xpp.getAttributeValue(i)));
                            }
                            else if (attributeName.equals("useControllerTypes"))
                            {
                                parseControllerTypes(settings, xpp.getAttributeValue(i));
                            }
                            else if (attributeName.equals("useAndroidWearTouchpad"))
                            {
                                settings.setUseAndroidWearTouchpad(
                                        Boolean.parseBoolean(xpp.getAttributeValue(i)));
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                } else if (eventType == XmlPullParser.TEXT) {
                }
                eventType = xpp.next();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void parseControllerTypes(VrAppSettings settings, String typeList) {
        Pattern pattern = Pattern.compile("([a-z]+)[, ]*");
        Matcher matcher = pattern.matcher(typeList);
        int index = 0;
        while (matcher.find(index))
        {
            String match = matcher.group(1);
            index = matcher.end();

            if (match.equals("gaze"))
            {
                settings.addControllerType(GVRControllerType.GAZE);
            }
            else if (match.equals("controller"))
            {
                settings.addControllerType(GVRControllerType.CONTROLLER);
            }
            else if (match.equals("gamepad"))
            {
                    settings.addControllerType(GVRControllerType.GAMEPAD);
            }
            else if (match.equals("weartouchpad"))
            {
                    settings.addControllerType(GVRControllerType.WEARTOUCHPAD);
            }
            else if (match.equals("mouse"))
            {
                    settings.addControllerType(GVRControllerType.MOUSE);
            }
            else if (match.equals("external"))
            {
                    settings.addControllerType(GVRControllerType.EXTERNAL);
            }
        }
    }
}
