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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.gearvrf.utility.VrAppSettings;
import org.gearvrf.utility.VrAppSettings.EyeBufferParms.ColorFormat;
import org.gearvrf.utility.VrAppSettings.EyeBufferParms.DepthFormat;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.AssetManager;
import android.util.Log;

/**
 * This class simply parses XML file for distortion stored in assets folder, and
 * allows users to read specific distortion value from the XML file.
 * {@link GVRViewManager} calls GVRXMLParser to initialize the distortion value
 * internally
 */
class GVRXMLParser {
    /**
     * Constructs a GVRXMLParser with current package assets manager and the
     * file name of the distortion xml file under assets folder
     * 
     * @param assets
     *            the current {@link AssetManager} for the package
     * @param fileName
     *            the distortion file name under assets folder
     */
    GVRXMLParser(AssetManager assets, String fileName, VrAppSettings settings) {
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
                        if(xpp.getAttributeValue(i).equals("DEFAULT")){
                            continue;
                        }
                        String attributeName = xpp.getAttributeName(i);
                        if (tagName.equals("mono-mode-parms")
                                || "mono-mode-parms".equals(tagName)) {
                            if (attributeName.equals("monoFullScreen")) {
                                settings.monoScopicModeParms
                                        .setMonoFullScreenMode(Boolean
                                                .parseBoolean(xpp
                                                        .getAttributeValue(i)));
                            } else if (attributeName.equals("monoMode")) {
                                settings.monoScopicModeParms
                                        .setMonoScopicMode(Boolean
                                                .parseBoolean(xpp
                                                        .getAttributeValue(i)));
                            }
                        } else if (tagName.equals("vr-app-settings")) {
                            if (attributeName.equals("showLoadingIcon")) {
                                settings.setShowLoadingIcon(Boolean
                                        .parseBoolean(xpp.getAttributeValue(i)));
                            } else if (attributeName
                                    .equals("useGazeCursorController")) {
                                settings.setUseGazeCursorController(Boolean
                                        .parseBoolean(xpp.getAttributeValue(i)));
                            } else if (attributeName
                                    .equals("useSrgbFramebuffer")) {
                                settings.setUseSrgbFramebuffer(Boolean
                                        .parseBoolean(xpp.getAttributeValue(i)));
                            } else if (attributeName
                                    .equals("useProtectedFramebuffer")) {
                                settings.setUseProtectedFramebuffer(Boolean
                                        .parseBoolean(xpp.getAttributeValue(i)));
                            } else if (attributeName
                                    .equals("framebufferPixelsWide")) {
                                settings.setFramebufferPixelsWide(Integer
                                        .parseInt(xpp.getAttributeValue(i)));
                            } else if (attributeName
                                    .equals("framebufferPixelsHigh")) {
                                settings.setFramebufferPixelsHigh(Integer
                                        .parseInt(xpp.getAttributeValue(i)));
                            }
                        } else if (tagName.equals("mode-parms")
                                || "mode-params".equals(tagName)) {
                            if (attributeName.equals("allowPowerSave")) {
                                settings.getModeParms().setAllowPowerSave(
                                        Boolean.parseBoolean(xpp
                                                .getAttributeValue(i)));
                            } else if (attributeName
                                    .equals("resetWindowFullScreen")) {
                                settings.getModeParms()
                                        .setResetWindowFullScreen(
                                                Boolean.parseBoolean(xpp
                                                        .getAttributeValue(i)));
                            } 
                        } else if(tagName.equals("performance-parms")
                                || "performance-params".equals(tagName)){
                            if (attributeName.equals("cpuLevel")) {
                                settings.getPerformanceParms().setCpuLevel(
                                        Integer.parseInt(xpp
                                                .getAttributeValue(i)));
                            } else if (attributeName.equals("gpuLevel")) {
                                settings.getPerformanceParms().setGpuLevel(
                                        Integer.parseInt(xpp
                                                .getAttributeValue(i)));
                            }
                        }else if (tagName.equals("eye-buffer-parms")
                                || "eye-buffer-params".equals(tagName)) {
                            String attributeValue = xpp.getAttributeValue(i);
                            if (attributeName.equals("fov-y")) {
                                settings.getEyeBufferParms().setFovY(
                                        Float.parseFloat(xpp
                                                .getAttributeValue(i)));
                            } else if (attributeName.equals("depthFormat")) {
                                if (attributeValue.equals("DEPTH_0")) {
                                    settings.eyeBufferParms
                                            .setDepthFormat(DepthFormat.DEPTH_0);
                                } else if (attributeValue.equals("DEPTH_16")) {
                                    settings.eyeBufferParms
                                            .setDepthFormat(DepthFormat.DEPTH_16);
                                } else if (attributeValue.equals("DEPTH_24")) {
                                    settings.eyeBufferParms
                                            .setDepthFormat(DepthFormat.DEPTH_24);
                                } else if (attributeValue
                                        .equals("DEPTH_24_STENCIL_8")) {
                                    settings.eyeBufferParms
                                            .setDepthFormat(DepthFormat.DEPTH_24_STENCIL_8);
                                }
                            } else if (attributeName.equals("colorFormat")) {
                                if (attributeValue.equals("COLOR_565")) {
                                    settings.eyeBufferParms
                                            .setColorFormat(ColorFormat.COLOR_565);
                                } else if (attributeValue.equals("COLOR_5551")) {
                                    settings.eyeBufferParms
                                            .setColorFormat(ColorFormat.COLOR_5551);
                                } else if (attributeValue.equals("COLOR_4444")) {
                                    settings.eyeBufferParms
                                            .setColorFormat(ColorFormat.COLOR_4444);
                                } else if (attributeValue.equals("COLOR_8888")) {
                                    settings.eyeBufferParms
                                            .setColorFormat(ColorFormat.COLOR_8888);
                                } else if (attributeValue
                                        .equals("COLOR_8888_sRGB")) {
                                    settings.eyeBufferParms
                                            .setColorFormat(ColorFormat.COLOR_8888_sRGB);
                                } else if (attributeValue
                                        .equals("COLOR_RGBA16F")) {
                                    settings.eyeBufferParms
                                            .setColorFormat(ColorFormat.COLOR_RGBA16F);
                                }
                            } else if (attributeName.equals("multiSamples")) {
                                settings.eyeBufferParms.setMultiSamples(Integer
                                        .parseInt(attributeValue));
                            } else if (attributeName.equals("resolveDepth")) {
                                settings.eyeBufferParms.enableResolveDepth(Boolean
                                        .parseBoolean(attributeValue));
                            } else if (attributeName.equals("resolutionWidth")) {
                                int resolutionWidth = Integer
                                        .parseInt(attributeValue);
                                settings.eyeBufferParms
                                        .setResolutionWidth(resolutionWidth);
                            } else if (attributeName.equals("resolutionHeight")) {
                                int resolutionHeight = Integer
                                        .parseInt(attributeValue);
                                settings.eyeBufferParms
                                        .setResolutionHeight(resolutionHeight);
                            }
                        } else if (tagName.equals("head-model-parms")
                                || "head-model-params".equals(tagName)) {
                            if (attributeName.equals("interpupillaryDistance")) {
                                settings.headModelParms
                                        .setInterpupillaryDistance(Float
                                                .parseFloat(xpp
                                                        .getAttributeValue(i)));
                            } else if (attributeName.equals("eyeHeight")) {
                                settings.headModelParms.setEyeHeight(Float
                                        .parseFloat(xpp.getAttributeValue(i)));
                            } else if (attributeName.equals("headModelDepth")) {
                                settings.headModelParms.setHeadModelDepth(Float
                                        .parseFloat(xpp.getAttributeValue(i)));
                            } else if (attributeName.equals("headModelHeight")) {
                                settings.headModelParms
                                        .setHeadModelHeight(Float
                                                .parseFloat(xpp
                                                        .getAttributeValue(i)));
                            }
                        } else if (tagName.equals("scene-parms")
                                || "scene-params".equals(tagName)) {
                            if (attributeName.equals("viewportX")) {
                                settings.sceneParms.viewportX =
                                        Integer.parseInt(xpp.getAttributeValue(i));
                            } else if (attributeName.equals("viewportY")) {
                                settings.sceneParms.viewportY =
                                        Integer.parseInt(xpp.getAttributeValue(i));
                            } else if (attributeName.equals("viewportWidth")) {
                                settings.sceneParms.viewportWidth =
                                        Integer.parseInt(xpp.getAttributeValue(i));
                            } else if (attributeName.equals("viewportHeight")) {
                                settings.sceneParms.viewportHeight =
                                        Integer.parseInt(xpp.getAttributeValue(i));
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
}
