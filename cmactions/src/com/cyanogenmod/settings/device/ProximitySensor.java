/*
 * Copyright (c) 2015 The CyanogenMod Project
 *
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

package com.cyanogenmod.settings.device;

import android.content.*;
import android.hardware.*;

public class ProximitySensor implements SensorEventListener
{
    private static final int PROXIMITY_DELAY = 1000000;
    private static final int PROXIMITY_LATENCY = 100000;
    private boolean mEnabled;
    private float mMaxRange;
    private ProximitySensor$ProximityListener mProximityListener;
    private Sensor mProximitySensor;
    private boolean mReady;
    private SensorManager mSensorManager;
    private boolean mState;
    
    public ProximitySensor(final Context context, final SensorManager mSensorManager, final ProximitySensor$ProximityListener mProximityListener) {
        this.mEnabled = false;
        this.reset();
        this.mProximitySensor = mSensorManager.getDefaultSensor(8, true);
        this.mProximityListener = mProximityListener;
        this.mSensorManager = mSensorManager;
        if (this.mProximitySensor != null) {
            this.mMaxRange = this.mProximitySensor.getMaximumRange();
        }
    }
    
    public void disable() {
        if (this.mEnabled && this.mProximitySensor != null) {
            this.mSensorManager.unregisterListener((SensorEventListener)this, this.mProximitySensor);
            this.mEnabled = false;
        }
    }
    
    public void enable() {
        if (!this.mEnabled && this.mProximitySensor != null) {
            this.mSensorManager.registerListener((SensorEventListener)this, this.mProximitySensor, 1000000, 100000);
            this.mEnabled = true;
        }
    }
    
    public void onAccuracyChanged(final Sensor sensor, final int n) {
    }
    
    public void onSensorChanged(final SensorEvent sensorEvent) {
        if (sensorEvent.values.length == 0) {
            return;
        }
        final boolean mState = sensorEvent.values[0] < this.mMaxRange;
        if (this.mState != mState) {
            this.mState = mState;
            if (this.mReady) {
                this.mProximityListener.onEvent(this.mState, sensorEvent.timestamp);
            }
        }
        if (!this.mReady) {
            this.mProximityListener.onInit(this.mState, sensorEvent.timestamp);
            this.mReady = true;
        }
    }
    
    public void reset() {
        this.mReady = false;
        this.mState = false;
    }
}
