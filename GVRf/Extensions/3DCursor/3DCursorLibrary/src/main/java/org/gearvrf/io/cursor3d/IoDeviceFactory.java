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
import java.io.StringWriter;

class IoDeviceFactory {
    private static final String TAG = IoDeviceFactory.class.getSimpleName();
    private static final String VENDOR_ID = "vendorId";
    private static final String PRODUCT_ID = "productId";
    private static final String DEVICE_ID = "deviceId";
    private static final String PRIORITY = "priority";
    private static final String VENDOR_NAME = "vendorName";
    private static final String NAME = "name";
    private static final String XML_START_TAG = "<io ";
    static final int INVALID_PRIORITY = -1;

    static PriorityIoDeviceTuple readIoDeviceFromSettingsXml(XmlPullParser parser) throws
            XmlPullParserException, IOException {
        IoDevice ioDevice = readIoDevice(parser);
        int priority;
        try {
            priority = Integer.parseInt(parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE,
                    PRIORITY));
        } catch (NumberFormatException e) {
            throw new XmlPullParserException("Invalid VendorId, ProductId or Priority for Io " +
                    "device");
        }
        XMLUtils.parseTillElementEnd(parser);
        return new PriorityIoDeviceTuple(priority, ioDevice);
    }

    private static IoDevice readIoDevice(XmlPullParser parser) throws
            XmlPullParserException, IOException {
        IoDevice ioDevice = new IoDevice();
        try {
            ioDevice.setVendorId(Integer.parseInt(parser.getAttributeValue(XMLUtils
                    .DEFAULT_XML_NAMESPACE, VENDOR_ID)));
            ioDevice.setProductId(Integer.parseInt(parser.getAttributeValue(XMLUtils
                    .DEFAULT_XML_NAMESPACE, PRODUCT_ID)));
        } catch (NumberFormatException e) {
            throw new XmlPullParserException("Invalid VendorId, ProductId or Priority for Io " +
                    "device");
        }

        ioDevice.setDeviceId(parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, DEVICE_ID));
        if (ioDevice.getDeviceId() == null) {
            throw new XmlPullParserException("deviceId for cursors IO device not specified");
        }
        ioDevice.setVendorName(parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE,
                VENDOR_NAME));
        ioDevice.setName(parser.getAttributeValue(XMLUtils.DEFAULT_XML_NAMESPACE, NAME));
        return ioDevice;
    }

    static IoDevice readIoDeviceFromIoXml(XmlPullParser parser) throws
            XmlPullParserException, IOException {
        IoDevice ioDevice = readIoDevice(parser);
        XMLUtils.parseTillElementEnd(parser);
        return ioDevice;
    }

    //TODO use XmlSerializer
    static void writeIoDevice(IoDevice ioDevice, BufferedWriter writer, int priority) throws
            IOException {
        writer.write(XML_START_TAG);
        XMLUtils.writeXmlAttribute(VENDOR_ID, ioDevice.getVendorId(), writer);
        XMLUtils.writeXmlAttribute(PRODUCT_ID, ioDevice.getProductId(), writer);
        XMLUtils.writeXmlAttribute(DEVICE_ID, ioDevice.getDeviceId(), writer);

        String name = ioDevice.getName();
        if (name != null) {
            XMLUtils.writeXmlAttribute(NAME, name, writer);
        }

        String vendorName = ioDevice.getVendorName();
        if (vendorName != null) {
            XMLUtils.writeXmlAttribute(VENDOR_NAME, vendorName, writer);
        }

        if (priority != INVALID_PRIORITY) {
            XMLUtils.writeXmlAttribute(PRIORITY, priority, writer);
        }
        writer.write(XMLUtils.ELEMENT_END);
    }

    static String getXmlString(IoDevice ioDevice) {
        StringWriter stringWriter = new StringWriter();
        BufferedWriter writer = new BufferedWriter(stringWriter);
        try {
            writeIoDevice(ioDevice, writer, INVALID_PRIORITY);
            writer.flush();
        } catch (IOException e) {
            Log.d(TAG, "Cannot convert IoDevice to string:", e);
        }
        return stringWriter.toString();
    }
}
