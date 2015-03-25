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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

/**
 * This class simply parses XML file for distortion stored in assets folder, and
 * allows users to read specific distortion value from the XML file.
 * {@link GVRViewManager} calls GVRXMLParser to initialize the distortion value
 * internally
 */
class GVRXMLParser {
    private float mLensSeparationDistance = 0.0f;
    private float mFovY = 0.0f;
    private int mFBOWidth = 512;
    private int mFBOHeight = 512;
    private int mMSAA = 1;

    /**
     * Constructs a GVRXMLParser with current package assets manager and the
     * file name of the distortion xml file under assets folder
     * 
     * @param assets
     *            the current {@link AssetManager} for the package
     * @param fileName
     *            the distortion file name under assets folder
     */
    public GVRXMLParser(AssetManager assets, String fileName) {
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
                        String attributeName = xpp.getAttributeName(i);
                        if (tagName.equals("metric")) {
                            if (attributeName
                                    .equals("lens-separation-distance")) {
                                mLensSeparationDistance = Float.parseFloat(xpp
                                        .getAttributeValue(i));
                            }
                        } else if (tagName.equals("scene")) {
                            if (attributeName.equals("fov-y")) {
                                mFovY = Float.parseFloat(xpp
                                        .getAttributeValue(i));
                            } else if (attributeName.equals("fbo-width")) {
                                mFBOWidth = Integer.parseInt(xpp
                                        .getAttributeValue(i));
                            } else if (attributeName.equals("fbo-height")) {
                                mFBOHeight = Integer.parseInt(xpp
                                        .getAttributeValue(i));
                            } else if (attributeName.equals("msaa")) {
                                mMSAA = Integer.parseInt(xpp
                                        .getAttributeValue(i));
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
        }
    }

    /**
     * Returns metric lens separation distance value
     * 
     * @return lens separation distance in float
     */
    public float getLensSeparationDistance() {
        return mLensSeparationDistance;
    }

    /**
     * Returns scene fov-y value
     * 
     * @return fov-y in float
     */
    public float getFovY() {
        return mFovY;
    }

    /**
     * Returns scene fbo-width value
     * 
     * @return fbo-width in int
     */
    public int getFBOWidth() {
        return mFBOWidth;
    }

    /**
     * Returns scene fbo-height value
     * 
     * @return fbo-height in int
     */
    public int getFBOHeight() {
        return mFBOHeight;
    }

    /**
     * Returns scene msaa value
     * 
     * @return msaa in int
     */
    public int getMSAA() {
        return mMSAA;
    }
}
