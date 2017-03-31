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

public class OrientationSensor implements SensorEventListener
{
    private static final float MATH_PI_1_4 = 0.785398f;
    private static final float MATH_PI_3_4 = 2.35619f;
    private static final int ORIENTATION_DELAY = 60000;
    public static final int ORIENTATION_FACE_DOWN = 1;
    public static final int ORIENTATION_FACE_UP = 2;
    private static final int ORIENTATION_LATENCY = 0;
    public static final int ORIENTATION_UNKNOWN = 0;
    public static final int ORIENTATION_VERTICAL = 3;
    private Sensor mAccelerometerSensor;
    private boolean mEnabled;
    private float[] mGravity;
    private float[] mMagnetic;
    private Sensor mMagneticFieldSensor;
    private OrientationSensor$OrientationListener mOrientationListener;
    private boolean mReady;
    private SensorManager mSensorManager;
    private int mState;
    
    public OrientationSensor(final Context context, final SensorManager mSensorManager, final OrientationSensor$OrientationListener mOrientationListener) {
        this.mEnabled = false;
        this.reset();
        this.mAccelerometerSensor = mSensorManager.getDefaultSensor(1, false);
        this.mMagneticFieldSensor = mSensorManager.getDefaultSensor(2, false);
        this.mOrientationListener = mOrientationListener;
        this.mSensorManager = mSensorManager;
    }
    
    public void disable() {
        if (this.mEnabled && this.mAccelerometerSensor != null && this.mMagneticFieldSensor != null) {
            this.mSensorManager.unregisterListener((SensorEventListener)this, this.mAccelerometerSensor);
            this.mSensorManager.unregisterListener((SensorEventListener)this, this.mMagneticFieldSensor);
            this.mEnabled = false;
        }
    }
    
    public void enable() {
        final int n = 60000;
        if (!this.mEnabled && this.mAccelerometerSensor != null && this.mMagneticFieldSensor != null) {
            this.reset();
            this.mState = 0;
            this.mSensorManager.registerListener((SensorEventListener)this, this.mAccelerometerSensor, n, 0);
            this.mSensorManager.registerListener((SensorEventListener)this, this.mMagneticFieldSensor, n, 0);
            this.mEnabled = true;
        }
    }
    
    public boolean isFaceDown() {
        boolean b = true;
        if (!this.mReady || this.mState != (b ? 1 : 0)) {
            b = false;
        }
        return b;
    }
    
    public boolean isFaceUp() {
        boolean b = false;
        if (this.mReady && this.mState == 2) {
            b = true;
        }
        return b;
    }
    
    public boolean isVertical() {
        boolean b = false;
        if (this.mReady && this.mState == 3) {
            b = true;
        }
        return b;
    }
    
    public void onAccuracyChanged(final Sensor sensor, final int n) {
    }
    
    public void onSensorChanged(final SensorEvent sensorEvent) {
        final float n = -2.35619f;
        final float n2 = 0.785398f;
        final float n3 = -0.785398f;
        final int n4 = 1;
        final int mState = 2;
        if (sensorEvent.values.length == 0) {
            return;
        }
        switch (sensorEvent.sensor.getType()) {
            case 1: {
                this.mGravity = sensorEvent.values;
                break;
            }
            case 2: {
                this.mMagnetic = sensorEvent.values;
                break;
            }
        }
        if (this.mGravity != null && this.mMagnetic != null) {
            final float[] array = new float[9];
            if (SensorManager.getRotationMatrix(array, new float[9], this.mGravity, this.mMagnetic)) {
                final float[] array2 = new float[3];
                this.mState = 0;
                SensorManager.getOrientation(array, array2);
                if (array2[n4] > n3 && array2[n4] < n2) {
                    if (array2[mState] > n3 && array2[mState] < n2) {
                        this.mState = mState;
                    }
                    else if (array2[mState] < n || array2[mState] > 2.35619f) {
                        this.mState = n4;
                    }
                }
                if (array2[n4] < n3 || array2[n4] > n2 || (array2[mState] > n2 && array2[mState] < 2.35619f) || (array2[mState] > n && array2[mState] < n3)) {
                    this.mState = 3;
                }
                this.mReady = (n4 != 0);
                this.mOrientationListener.onEvent();
            }
        }
    }
    
    public void reset() {
        this.mGravity = null;
        this.mMagnetic = null;
        this.mReady = false;
    }
}
