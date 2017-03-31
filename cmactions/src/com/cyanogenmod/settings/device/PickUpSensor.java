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

public class PickUpSensor implements SensorEventListener
{
    private static final int PICKUP_DELAY = 500000;
    private static final int PICKUP_LATENCY = 100000;
    public static final int PICK_UP_FALSE = 1;
    private static final float PICK_UP_SAFEZONE = 5.0f;
    private static final float PICK_UP_THRESHOLD = 6.0f;
    public static final int PICK_UP_TRUE = 2;
    public static final int PICK_UP_UNKNOWN;
    private boolean mEnabled;
    private PickUpSensor$PickUpListener mPickUpListener;
    private Sensor mPickUpSensor;
    private boolean mReady;
    private SensorManager mSensorManager;
    private int mState;
    
    public PickUpSensor(final Context context, final SensorManager mSensorManager, final PickUpSensor$PickUpListener mPickUpListener) {
        this.mEnabled = false;
        this.reset();
        this.mPickUpSensor = mSensorManager.getDefaultSensor(1, false);
        this.mPickUpListener = mPickUpListener;
        this.mSensorManager = mSensorManager;
    }
    
    public void disable() {
        if (this.mEnabled && this.mPickUpSensor != null) {
            this.mSensorManager.unregisterListener((SensorEventListener)this, this.mPickUpSensor);
            this.mEnabled = false;
        }
    }
    
    public void enable() {
        if (!this.mEnabled && this.mPickUpSensor != null) {
            this.reset();
            this.mSensorManager.registerListener((SensorEventListener)this, this.mPickUpSensor, 500000, 100000);
            this.mEnabled = true;
        }
    }
    
    public boolean isPickUpAbove(final float n, final float n2, final float n3) {
        boolean b = true;
        if (n >= -n3 && n <= n3 && n2 <= n3) {
            b = false;
        }
        return b;
    }
    
    public boolean isPickedUp() {
        boolean b = false;
        if (this.mReady && this.mState == 2) {
            b = true;
        }
        return b;
    }
    
    public void onAccuracyChanged(final Sensor sensor, final int n) {
    }
    
    public void onSensorChanged(final SensorEvent sensorEvent) {
        final int mState = 2;
        final int mReady = 1;
        if (sensorEvent.values.length == 0) {
            return;
        }
        final float n = sensorEvent.values[0];
        final float n2 = sensorEvent.values[mReady];
        final float n3 = sensorEvent.values[mState];
        if (this.isPickUpAbove(n, n2, 5.0f)) {
            if (this.isPickUpAbove(n, n2, 6.0f) && this.mState != mState) {
                this.mState = mState;
                if (this.mReady) {
                    this.mPickUpListener.onEvent();
                }
            }
        }
        else if (this.mState != mReady) {
            this.mState = mReady;
            this.mState = mReady;
            if (this.mReady) {
                this.mPickUpListener.onEvent();
            }
        }
        if (!this.mReady) {
            this.mReady = (mReady != 0);
            this.mPickUpListener.onInit();
        }
    }
    
    public void reset() {
        this.mReady = false;
        this.mState = 0;
    }
}
