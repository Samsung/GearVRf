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
import org.gearvrf.io.cursor3d.CursorAsset.Action;
import org.gearvrf.utility.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CursorAssetFactory {
    private static final String TAG = CursorAssetFactory.class.getSimpleName();
    private static final String ACTION = "action";
    private static final String SRC = "src";
    private static final String TYPE = "type";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String SOUND_ENABLED = "soundEnabled";
    private static final String SOUND_SRC = "soundSrc";
    private static final String ANIMATED = "animated";
    private static final String DURATION = "duration";
    private static final float DEFAULT_MESH_DIMENSION = 0.1f;
    private static final String XML_START_TAG = "<asset ";
    private static final String FBX_EXTENSION = ".fbx";
    private static final String OBJ_EXTENSION = ".obj";
    private static final String JPG_EXTENSION = ".jpg";
    private static final String JPEG_EXTENSION = ".jpeg";
    private static final String PNG_EXTENSION = ".png";
    private static final String PKM_EXTENSION = ".pkm";
    private static final String ZIP_EXTENSION = ".zip";

    static CursorAsset readAsset(XmlPullParser parser, GVRContext context, CursorType type) throws
            XmlPullParserException, IOException {
        CursorAsset cursorAsset;
        AssetType assetType;
        Action action;
        String src;
        boolean animated;
        String assetTypeXml = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, TYPE);
        if (assetTypeXml == null)

            throw new XmlPullParserException("No Asset type specified");
        assetType = AssetType.fromString(assetTypeXml);

        String animatedVal = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, ANIMATED);
        if (animatedVal == null) {
            animated = false;
        } else {
            animated = XMLUtils.parseBoolean(animatedVal);
        }

        String actionVal = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, ACTION);
        if (actionVal == null) {
            throw new XmlPullParserException("No Action specified for asset");
        }
        action = XMLUtils.getEnumFromString(Action.class, actionVal);

        src = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, SRC);
        if (src == null) {
            throw new XmlPullParserException("No src specified for asset");
        }

        if (assetType.getValue().equals(AssetType.TYPE_2D)) {
            if (animated) {
                cursorAsset = getNewAnimatedCursorAsset(context, src, type, action, parser);
            } else {
                cursorAsset = getNew2DMeshAsset(context, src, type, action, parser);
            }
        } else if (assetType.getValue().equals(AssetType.TYPE_3D)) {
            if (src.endsWith(FBX_EXTENSION)) {
                cursorAsset = new ObjectCursorAsset(context, type, action, src);
            } else {
                cursorAsset = getNew3DMeshAsset(context, src, type, action, animated);
            }
        } else {
            throw new IllegalArgumentException("Asset type has to be 2D or 3D");
        }

        cursorAsset.setAssetType(assetType);
        cursorAsset.setSrc(src);
        cursorAsset.setAnimated(animated);

        String soundEnabledVal = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE,
                SOUND_ENABLED);
        if (soundEnabledVal == null) {
            cursorAsset.setSoundEnabled(false);
        } else {
            cursorAsset.setSoundEnabled(XMLUtils.parseBoolean(soundEnabledVal));
        }

        String soundSrc = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, SOUND_SRC);
        if (soundSrc == null) {
            if (cursorAsset.isSoundEnabled()) {
                throw new XmlPullParserException("Sound source not specified with sound " +
                        "enabled");
            }
        } else {
            cursorAsset.setSoundSrc(soundSrc);
        }

        XMLUtils.parseTillElementEnd(parser);

        Log.d(TAG, CursorAssetFactory.getXmlString(cursorAsset));
        return cursorAsset;
    }

    private static CursorAsset getNew3DMeshAsset(GVRContext context, String src, CursorType type,
                                                 Action action, boolean animated) {
        List<String> meshAssets = new ArrayList<String>(1);
        List<String> textureAssets = new ArrayList<String>();
        getFilesFromAssets(src, context.getContext(), meshAssets, textureAssets);
        if (!animated) {
            if (meshAssets.size() != 1 || textureAssets.size() != 1) {
                throw new IllegalArgumentException("Invalid attribute value for asset's src, " +
                        "non animated asset folder should have 1 .obj file and 1 .png/jpeg/jpg " +
                        "file");
            }

            return new MeshCursorAsset(context, type, action, meshAssets.get(0), textureAssets
                    .get(0));
        } else {
            if (meshAssets.size() != 1 || textureAssets.size() != 1) {
                throw new IllegalArgumentException("Invalid attribute value for asset's src, " +
                        "animated asset folder should have 1 .obj file and zip file containing " +
                        "multiple .png/jpg/jpeg files");
            }
            return new AnimatedCursorAsset(context, type, action, textureAssets
                    .get(0), meshAssets.get(0));
        }
    }

    private static boolean isTextureFile(String fileName) {
        return (fileName.endsWith(PNG_EXTENSION) || fileName.endsWith(JPG_EXTENSION) ||
                fileName.endsWith(PKM_EXTENSION) || fileName.endsWith(JPEG_EXTENSION));
    }

    private static MeshCursorAsset getNew2DMeshAsset(GVRContext context, String src, CursorType
            type, CursorAsset.Action action, XmlPullParser parser)
            throws XmlPullParserException {
        MeshCursorAsset asset = new MeshCursorAsset(context, type, action, src);
        String xVal, yVal;
        float x, y;
        xVal = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, X);
        yVal = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, Y);

        try {
            if (xVal == null) {
                x = DEFAULT_MESH_DIMENSION;
            } else {
                x = Float.parseFloat(xVal);
            }

            if (yVal == null) {
                y = DEFAULT_MESH_DIMENSION;
            } else {
                y = Float.parseFloat(yVal);
            }
        } catch (NumberFormatException e) {
            throw new XmlPullParserException("Invalid x or y dimension for asset");
        }
        asset.setQuadMesh(x, y);
        return asset;
    }

    private static AnimatedCursorAsset getNewAnimatedCursorAsset(GVRContext context, String src,
                                                                 CursorType type, Action action,
                                                                 XmlPullParser parser)
            throws XmlPullParserException {

        AnimatedCursorAsset asset;
        if (src.endsWith(ZIP_EXTENSION)) {
            asset = new AnimatedCursorAsset(context, type, action, src);
        } else {
            throw new IllegalArgumentException("src value for animated asset should contain " +
                    "a zip file with multiple .png/jpeg/jpg files");
        }
        String duration = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, DURATION);
        if (duration == null) {
            throw new XmlPullParserException("Duration needs to be specified for animated " +
                    "asset");
        } else {
            try {
                asset.setAnimationDuration(Float.parseFloat(duration));
            } catch (NumberFormatException e) {
                throw new XmlPullParserException("Illegal Value for Asset duration");
            }
        }
        String xVal, yVal;
        float x, y;
        xVal = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, X);
        yVal = parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, Y);

        try {
            if (xVal == null) {
                x = DEFAULT_MESH_DIMENSION;
            } else {
                x = Float.parseFloat(xVal);
            }

            if (yVal == null) {
                y = DEFAULT_MESH_DIMENSION;
            } else {
                y = Float.parseFloat(yVal);
            }
        } catch (NumberFormatException e) {
            throw new XmlPullParserException("Invalid x or y dimension for asset");
        }
        asset.setQuadMesh(x, y);
        return asset;
    }

    //TODO use XmlSerializer
    static void writeCursorAsset(CursorAsset asset, BufferedWriter writer) throws IOException {
        writer.write(XML_START_TAG);
        XMLUtils.writeXmlAttribute(ACTION, asset.getAction(), writer);
        XMLUtils.writeXmlAttribute(SRC, asset.getSrc(), writer);
        XMLUtils.writeXmlAttribute(SOUND_ENABLED, XMLUtils.xmlFromBoolean(asset.isSoundEnabled())
                , writer);

        String soundSrc = asset.getSoundSrc();
        if (soundSrc != null) {
            XMLUtils.writeXmlAttribute(SOUND_SRC, soundSrc, writer);
        }
        XMLUtils.writeXmlAttribute(TYPE, asset.getAssetType().getValue(), writer);
        XMLUtils.writeXmlAttribute(ANIMATED, XMLUtils.xmlFromBoolean(asset.getAnimated()), writer);

        if (asset instanceof AnimatedCursorAsset) {
            AnimatedCursorAsset animatedCursorAsset = (AnimatedCursorAsset) asset;
            XMLUtils.writeXmlAttribute(DURATION, animatedCursorAsset.getAnimationDuration(),
                    writer);
        }
        if (asset instanceof MeshCursorAsset) {
            MeshCursorAsset meshAsset = (MeshCursorAsset) asset;
            XMLUtils.writeXmlAttribute(X, meshAsset.getX(), writer);
            XMLUtils.writeXmlAttribute(Y, meshAsset.getY(), writer);
        }
        writer.write(XMLUtils.ELEMENT_END);
    }

    static String getXmlString(CursorAsset asset) {
        StringWriter stringWriter = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
        try {
            writeCursorAsset(asset, bufferedWriter);
            bufferedWriter.flush();
        } catch (IOException e) {
            Log.d(TAG, "Could not convert asset to xml:", e);
        }
        return stringWriter.toString();
    }

    private static void getFilesFromAssets(String src, Context context, List<String> meshAssets,
                                           List<String> textures) {
        String[] files = null;
        try {
            files = context.getAssets().list(src);
        } catch (IOException e) {
            Log.d(TAG, "", e);
            throw new IllegalArgumentException("Invalid attribute value for src, should point to " +
                    "folder containing assets");
        }

        for (String fileName : files) {
            if (fileName.endsWith(OBJ_EXTENSION)) {
                meshAssets.add(src + File.separator + fileName);
            } else if (fileName.endsWith(ZIP_EXTENSION) || isTextureFile(fileName)) {
                textures.add(src + File.separator + fileName);
            }
        }
        Collections.sort(textures);
    }

}
