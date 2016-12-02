
/* Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf.x3d;


import org.gearvrf.utility.Log;

import java.util.Vector;

/**
 * @author m1.williams
 *         InteractiveObject has links to all the relevant objects (Sensors - TouchSensor, Anchor, ProximitySensor -
 *         TimeSensor, Interpolators, Scripting [in the future] and the DEFined Item) so one autonimous interactive
 *         object or animation will be in a single InteractiveObject.
 *         If sensor is null, then this likely an animation only
 */

public class InteractiveObject {

    private static final String TAG = InteractiveObject.class.getSimpleName();
    private Sensor sensor = null;
    private String sensorFromField; // usually isActive or isOver
    private TimeSensor timeSensor = null;
    private String timeSensorFromField; // often setEnabled
    private String timeSensorToField; // usually fraction_changed
    private Interpolator interpolator = null;
    private String interpolatorFromField; // often value_changed
    private String interpolatorToField; // usually set_fraction
    private EventUtility eventUtility = null;
    private String eventUtilityFromField; // often 'toggle_changed'
    private String eventUtilityToField; // usually 'set_boolean'
    private ScriptObject scriptObject = null;

    private DefinedItem definedItem = null;
    private String definedItemToField; // can be set_translation, set_rotation, set_position, set_orientation, set_scale
    private String definedItemFromField; // used for Scripts, sending data from SceneObj to Script


    public InteractiveObject() {
    }

    public void setSensor(Sensor sensor, String sensorFromField) {
        this.sensor = sensor;
        this.sensorFromField = sensorFromField;
    }

    public Sensor getSensor() {
        return this.sensor;
    }

    public String getSensorFromField() {
        return this.sensorFromField;
    }

    public void setTimeSensor(TimeSensor timeSensor) {
        this.timeSensor = timeSensor;
    }

    public TimeSensor getTimeSensor() {
        return this.timeSensor;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    public Interpolator getInterpolator() {
        return this.interpolator;
    }

    public void setEventUtility(EventUtility eventUtility) {
        this.eventUtility = eventUtility;
    }

    public EventUtility getEventUtility() {
        return this.eventUtility;
    }

    public ScriptObject getScriptObject() {
        return this.scriptObject;
    }

    public void setScriptObject(ScriptObject scriptObject) {
        this.scriptObject = scriptObject;
    }


    public void setDefinedItemToField(DefinedItem definedItem, String definedItemToField) {
        this.definedItem = definedItem;
        this.definedItemToField = definedItemToField;
    }

    public void setDefinedItemFromField(DefinedItem definedItem, String definedItemFromField) {
        this.definedItem = definedItem;
        this.definedItemFromField = definedItemFromField;
    }

    public DefinedItem getDefinedItem() {
        return this.definedItem;
    }

    public String getDefinedItemToField() {
        return this.definedItemToField;
    }

    public String getDefinedItemFromField() {
        return this.definedItemFromField;
    }
}



