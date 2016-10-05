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
import org.gearvrf.utility.VrAppSettings.EyeBufferParams.ColorFormat;
import org.gearvrf.utility.VrAppSettings.EyeBufferParams.DepthFormat;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.AssetManager;

/**
 * This class simply parses XML file for distortion stored in assets folder, and
 * allows users to read specific distortion value from the XML file.
 * {@link OvrViewManager} calls OvrXMLParser to initialize the distortion value
 * internally
 */
class OvrXMLParser {
    /**
     * Constructs a OvrXMLParser with current package assets manager and the
     * file name of the distortion xml file under assets folder
     * 
     * @param assets
     *            the current {@link AssetManager} for the package
     * @param fileName
     *            the distortion file name under assets folder
     */
    OvrXMLParser(AssetManager assets, String fileName, VrAppSettings baseSettings) {
        final OvrVrAppSettings settings = (OvrVrAppSettings)baseSettings;

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
                                settings.getMonoscopicModeParams()
                                        .setMonoFullScreenMode(Boolean
                                                .parseBoolean(xpp
                                                        .getAttributeValue(i)));
                            } else if (attributeName.equals("monoMode")) {
                                settings.getMonoscopicModeParams()
                                        .setMonoscopicMode(Boolean
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
                                    .equals("useAndroidWearTouchpad")) {
                                settings.setUseAndroidWearTouchpad(Boolean
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
                            } else if (attributeName
                                    .equals("useMultiview")){                              
                                settings.setUseMultiview(Boolean.
                                        parseBoolean(xpp.getAttributeValue(i)));
                            }
                        } else if (tagName.equals("mode-parms")
                                || "mode-params".equals(tagName)) {
                            if (attributeName.equals("allowPowerSave")) {
                                settings.getModeParams().setAllowPowerSave(
                                        Boolean.parseBoolean(xpp
                                                .getAttributeValue(i)));
                            } else if (attributeName
                                    .equals("resetWindowFullScreen")) {
                                settings.getModeParams()
                                        .setResetWindowFullScreen(
                                                Boolean.parseBoolean(xpp
                                                        .getAttributeValue(i)));
                            } 
                        } else if(tagName.equals("performance-parms")
                                || "performance-params".equals(tagName)){
                            if (attributeName.equals("cpuLevel")) {
                                settings.getPerformanceParams().setCpuLevel(
                                        Integer.parseInt(xpp
                                                .getAttributeValue(i)));
                            } else if (attributeName.equals("gpuLevel")) {
                                settings.getPerformanceParams().setGpuLevel(
                                        Integer.parseInt(xpp
                                                .getAttributeValue(i)));
                            }
                        }else if (tagName.equals("eye-buffer-parms")
                                || "eye-buffer-params".equals(tagName)) {
                            String attributeValue = xpp.getAttributeValue(i);
                            if (attributeName.equals("fov-y")) {
                                settings.getEyeBufferParams().setFovY(
                                        Float.parseFloat(xpp
                                                .getAttributeValue(i)));
                            } else if (attributeName.equals("depthFormat")) {
                                if (attributeValue.equals("DEPTH_0")) {
                                    settings.getEyeBufferParams()
                                            .setDepthFormat(DepthFormat.DEPTH_0);
                                } else if (attributeValue.equals("DEPTH_16")) {
                                    settings.getEyeBufferParams()
                                            .setDepthFormat(DepthFormat.DEPTH_16);
                                } else if (attributeValue.equals("DEPTH_24")) {
                                    settings.getEyeBufferParams()
                                            .setDepthFormat(DepthFormat.DEPTH_24);
                                } else if (attributeValue
                                        .equals("DEPTH_24_STENCIL_8")) {
                                    settings.getEyeBufferParams()
                                            .setDepthFormat(DepthFormat.DEPTH_24_STENCIL_8);
                                }
                            } else if (attributeName.equals("colorFormat")) {
                                if (attributeValue.equals("COLOR_565")) {
                                    settings.getEyeBufferParams()
                                            .setColorFormat(ColorFormat.COLOR_565);
                                } else if (attributeValue.equals("COLOR_5551")) {
                                    settings.getEyeBufferParams()
                                            .setColorFormat(ColorFormat.COLOR_5551);
                                } else if (attributeValue.equals("COLOR_4444")) {
                                    settings.getEyeBufferParams()
                                            .setColorFormat(ColorFormat.COLOR_4444);
                                } else if (attributeValue.equals("COLOR_8888")) {
                                    settings.getEyeBufferParams()
                                            .setColorFormat(ColorFormat.COLOR_8888);
                                } else if (attributeValue
                                        .equals("COLOR_8888_sRGB")) {
                                    settings.getEyeBufferParams()
                                            .setColorFormat(ColorFormat.COLOR_8888_sRGB);
                                } else if (attributeValue
                                        .equals("COLOR_RGBA16F")) {
                                    settings.getEyeBufferParams()
                                            .setColorFormat(ColorFormat.COLOR_RGBA16F);
                                }
                            } else if (attributeName.equals("multiSamples")) {
                                settings.getEyeBufferParams().setMultiSamples(Integer
                                        .parseInt(attributeValue));
                            } else if (attributeName.equals("resolveDepth")) {
                                settings.getEyeBufferParams().enableResolveDepth(Boolean
                                        .parseBoolean(attributeValue));
                            } else if (attributeName.equals("resolutionWidth")) {
                                int resolutionWidth = Integer
                                        .parseInt(attributeValue);
                                settings.getEyeBufferParams()
                                        .setResolutionWidth(resolutionWidth);
                            } else if (attributeName.equals("resolutionHeight")) {
                                int resolutionHeight = Integer
                                        .parseInt(attributeValue);
                                settings.getEyeBufferParams()
                                        .setResolutionHeight(resolutionHeight);
                            }
                        } else if (tagName.equals("head-model-parms")
                                || "head-model-params".equals(tagName)) {
                            if (attributeName.equals("interpupillaryDistance")) {
                                settings.getHeadModelParams()
                                        .setInterpupillaryDistance(Float
                                                .parseFloat(xpp
                                                        .getAttributeValue(i)));
                            } else if (attributeName.equals("eyeHeight")) {
                                settings.getHeadModelParams().setEyeHeight(Float
                                        .parseFloat(xpp.getAttributeValue(i)));
                            } else if (attributeName.equals("headModelDepth")) {
                                settings.getHeadModelParams().setHeadModelDepth(Float
                                        .parseFloat(xpp.getAttributeValue(i)));
                            } else if (attributeName.equals("headModelHeight")) {
                                settings.getHeadModelParams()
                                        .setHeadModelHeight(Float
                                                .parseFloat(xpp
                                                        .getAttributeValue(i)));
                            }
                        } else if (tagName.equals("scene-parms")
                                || "scene-params".equals(tagName)) {
                            if (attributeName.equals("viewportX")) {
                                settings.getSceneParams().viewportX =
                                        Integer.parseInt(xpp.getAttributeValue(i));
                            } else if (attributeName.equals("viewportY")) {
                                settings.getSceneParams().viewportY =
                                        Integer.parseInt(xpp.getAttributeValue(i));
                            } else if (attributeName.equals("viewportWidth")) {
                                settings.getSceneParams().viewportWidth =
                                        Integer.parseInt(xpp.getAttributeValue(i));
                            } else if (attributeName.equals("viewportHeight")) {
                                settings.getSceneParams().viewportHeight =
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
