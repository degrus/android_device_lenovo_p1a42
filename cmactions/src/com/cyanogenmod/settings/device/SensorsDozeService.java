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

import android.app.*;
import android.hardware.*;
import android.provider.*;
import android.media.*;
import cyanogenmod.providers.*;
import android.os.*;
import android.preference.*;
import android.content.*;

public class SensorsDozeService extends Service
{
    public static final boolean DEBUG = false;
    private static final String DOZE_INTENT = "com.android.systemui.doze.pulse";
    private static final int HANDWAVE_DELTA_NS = 1000000000;
    private static final String KEY_GESTURE_HAND_WAVE = "gesture_hand_wave";
    private static final String KEY_GESTURE_PICK_UP = "gesture_pick_up";
    private static final String KEY_GESTURE_POCKET = "gesture_pocket";
    private static final String KEY_PROXIMITY_WAKE = "proximity_wake_enable";
    private static final int PULSE_MIN_INTERVAL_MS = 5000;
    private static final int SENSORS_WAKELOCK_DURATION = 1000;
    public static final String TAG = "SensorsDozeService";
    private static final int VIBRATOR_ACKNOWLEDGE = 40;
    private Context mContext;
    private boolean mDozeEnabled;
    private boolean mHandwaveDoze;
    private boolean mHandwaveGestureEnabled;
    private long mLastPulseTimestamp;
    private long mLastStowedTimestamp;
    private OrientationSensor$OrientationListener mOrientationListener;
    private OrientationSensor mOrientationSensor;
    private boolean mPickUpDoze;
    private boolean mPickUpGestureEnabled;
    private PickUpSensor$PickUpListener mPickUpListener;
    private PickUpSensor mPickUpSensor;
    private boolean mPickUpState;
    private boolean mPocketDoze;
    private boolean mPocketGestureEnabled;
    private PowerManager mPowerManager;
    private SharedPreferences$OnSharedPreferenceChangeListener mPrefListener;
    private ProximitySensor$ProximityListener mProximityListener;
    private boolean mProximityNear;
    private ProximitySensor mProximitySensor;
    private boolean mProximityWake;
    private boolean mProximityWakeEnabled;
    private BroadcastReceiver mScreenStateReceiver;
    private SensorManager mSensorManager;
    private PowerManager$WakeLock mSensorsWakeLock;
    
    public SensorsDozeService() {
        final long n = 0L;
        this.mDozeEnabled = false;
        this.mHandwaveDoze = false;
        this.mHandwaveGestureEnabled = false;
        this.mPickUpDoze = false;
        this.mPickUpGestureEnabled = false;
        this.mPickUpState = false;
        this.mPocketDoze = false;
        this.mPocketGestureEnabled = false;
        this.mProximityNear = false;
        this.mProximityWake = false;
        this.mProximityWakeEnabled = false;
        this.mLastPulseTimestamp = n;
        this.mLastStowedTimestamp = n;
        this.mOrientationListener = new SensorsDozeService$1(this);
        this.mPickUpListener = new SensorsDozeService$2(this);
        this.mProximityListener = new SensorsDozeService$3(this);
        this.mScreenStateReceiver = new SensorsDozeService$4(this);
        this.mPrefListener = (SharedPreferences$OnSharedPreferenceChangeListener)new SensorsDozeService$5(this);
    }
    
    private void analyseDoze() {
        this.getDozeEnabled();
        if (!this.mHandwaveDoze || this.mOrientationSensor.isFaceDown()) {
            if (this.mPickUpDoze && ((this.mPickUpState && !this.mProximityNear) || (!this.mPickUpState && this.mOrientationSensor.isFaceDown()))) {
                this.launchDozePulse();
            }
            else if (this.mPocketDoze && this.mOrientationSensor.isVertical()) {
                this.launchDozePulse();
            }
            else if (this.mProximityWake && !this.mOrientationSensor.isFaceDown()) {
                this.launchDeviceWake();
            }
        }
        else {
            this.launchDozePulse();
        }
        if (!this.mProximityNear && this.isPickUpEnabled()) {
            this.setPickUpSensor(true, false);
        }
        this.resetValues();
    }
    
    private void getDozeEnabled() {
        boolean mDozeEnabled = true;
        if (Settings$Secure.getInt(this.mContext.getContentResolver(), "doze_enabled", 1) == 0) {
            mDozeEnabled = false;
        }
        this.mDozeEnabled = mDozeEnabled;
    }
    
    private void handleOrientation() {
        if (!this.mProximityNear) {
            this.analyseDoze();
        }
    }
    
    private void handlePickUp() {
        this.getDozeEnabled();
        if (this.mPickUpState && this.isPickUpEnabled()) {
            this.mPickUpDoze = true;
            this.launchWakeLock();
            this.analyseDoze();
        }
        else {
            this.mPickUpDoze = false;
        }
    }
    
    private void handleProximity(final long mLastStowedTimestamp) {
        final boolean b = true;
        final boolean mHandwaveDoze = mLastStowedTimestamp - this.mLastStowedTimestamp < 1000000000L;
        this.getDozeEnabled();
        if (!this.mProximityNear) {
            this.mHandwaveDoze = false;
            this.mPickUpDoze = false;
            this.mPocketDoze = false;
            this.mProximityWake = false;
            if (this.isHandwaveEnabled() && this.isPickUpEnabled() && this.isPocketEnabled()) {
                this.mHandwaveDoze = mHandwaveDoze;
                this.mPickUpDoze = (!mHandwaveDoze && b);
                this.mPocketDoze = (!mHandwaveDoze && b);
                this.setOrientationSensor(b, false);
            }
            else if (this.isProximityWakeEnabled() && mHandwaveDoze) {
                this.setOrientationSensor(this.mProximityWake = b, false);
            }
            else if (this.isHandwaveEnabled() && mHandwaveDoze) {
                this.setOrientationSensor(this.mHandwaveDoze = b, false);
            }
            else if ((!this.isPickUpEnabled() && !this.isPocketEnabled()) || mHandwaveDoze) {
                if (this.isPickUpEnabled()) {
                    this.setPickUpSensor(b, false);
                }
            }
            else {
                this.mPickUpDoze = this.isPickUpEnabled();
                this.mPocketDoze = this.isPocketEnabled();
                this.setOrientationSensor(b, false);
            }
        }
        else {
            this.mLastStowedTimestamp = mLastStowedTimestamp;
            this.setOrientationSensor(false, false);
            this.setPickUpSensor(false, false);
        }
    }
    
    private boolean isDozeEnabled() {
        return this.mDozeEnabled;
    }
    
    private boolean isEventPending() {
        return this.mHandwaveDoze || this.mPickUpDoze || this.mPocketDoze || this.mProximityWake;
    }
    
    private boolean isHandwaveEnabled() {
        return this.mHandwaveGestureEnabled && this.isDozeEnabled();
    }
    
    private boolean isPickUpEnabled() {
        return this.mPickUpGestureEnabled && this.isDozeEnabled();
    }
    
    private boolean isPocketEnabled() {
        return this.mPocketGestureEnabled && this.isDozeEnabled();
    }
    
    private boolean isProximityWakeEnabled() {
        return this.mProximityWakeEnabled;
    }
    
    private void launchAcknowledge() {
        final AudioManager audioManager = (AudioManager)this.mContext.getSystemService("audio");
        final Vibrator vibrator = (Vibrator)this.mContext.getSystemService("vibrator");
        int n;
        if (CMSettings$System.getInt(this.mContext.getContentResolver(), "touchscreen_gesture_haptic_feedback", 1) != 0) {
            n = 1;
        }
        else {
            n = 0;
        }
        switch (audioManager.getRingerMode()) {
            default: {
                if (n != 0) {
                    vibrator.vibrate((long)40);
                }
            }
            case 0: {}
        }
    }
    
    private void launchDeviceWake() {
        this.mSensorsWakeLock.acquire(1000L);
        this.launchAcknowledge();
        this.mPowerManager.wakeUp(SystemClock.uptimeMillis());
    }
    
    private void launchDozePulse() {
        long n;
        if (this.mLastPulseTimestamp != 0L) {
            n = SystemClock.elapsedRealtime() - this.mLastPulseTimestamp;
        }
        else {
            n = 5000L;
        }
        if (n >= 5000L) {
            this.launchWakeLock();
            this.launchAcknowledge();
            this.mLastPulseTimestamp = SystemClock.elapsedRealtime();
            this.mContext.sendBroadcastAsUser(new Intent("com.android.systemui.doze.pulse"), UserHandle.ALL);
        }
    }
    
    private void launchWakeLock() {
        this.mSensorsWakeLock.acquire(1000L);
    }
    
    private void loadPreferences(final SharedPreferences sharedPreferences) {
        this.mHandwaveGestureEnabled = sharedPreferences.getBoolean("gesture_hand_wave", false);
        this.mPickUpGestureEnabled = sharedPreferences.getBoolean("gesture_pick_up", false);
        this.mPocketGestureEnabled = sharedPreferences.getBoolean("gesture_pocket", false);
        this.mProximityWakeEnabled = sharedPreferences.getBoolean("proximity_wake_enable", false);
    }
    
    private void onDisplayOff() {
        final boolean b = true;
        this.getDozeEnabled();
        this.mLastPulseTimestamp = 0L;
        if (this.isHandwaveEnabled() || this.isPickUpEnabled() || this.isPocketEnabled() || this.isProximityWakeEnabled()) {
            this.resetValues();
            this.setOrientationSensor(false, b);
            this.setPickUpSensor(false, b);
            this.setProximitySensor(b, b);
        }
    }
    
    private void onDisplayOn() {
        final boolean b = true;
        this.setOrientationSensor(false, b);
        this.setPickUpSensor(false, b);
        this.setProximitySensor(false, b);
    }
    
    private void resetValues() {
        this.mHandwaveDoze = false;
        this.mPickUpDoze = false;
        this.mPocketDoze = false;
        this.mProximityWake = false;
    }
    
    private void setOrientationSensor(final boolean b, final boolean b2) {
        if (this.mOrientationSensor == null) {
            return;
        }
        if (b2) {
            this.mOrientationSensor.reset();
        }
        if (b) {
            this.setPickUpSensor(false, false);
            this.launchWakeLock();
            this.mOrientationSensor.enable();
        }
        else {
            this.mOrientationSensor.disable();
        }
    }
    
    private void setPickUpSensor(final boolean b, final boolean b2) {
        if (this.mPickUpSensor == null) {
            return;
        }
        if (b2) {
            this.mPickUpSensor.reset();
        }
        if (b) {
            this.setOrientationSensor(false, false);
            this.mPickUpSensor.enable();
        }
        else {
            this.mPickUpSensor.disable();
        }
    }
    
    private void setProximitySensor(final boolean b, final boolean b2) {
        if (this.mProximitySensor == null) {
            return;
        }
        if (b2) {
            this.mProximitySensor.reset();
        }
        if (b) {
            this.mProximitySensor.enable();
        }
        else {
            this.mProximitySensor.disable();
        }
    }
    
    public IBinder onBind(final Intent intent) {
        return null;
    }
    
    public void onCreate() {
        super.onCreate();
        this.mContext = (Context)this;
        this.mPowerManager = (PowerManager)this.mContext.getSystemService("power");
        this.mSensorManager = (SensorManager)this.mContext.getSystemService("sensor");
        this.mSensorsWakeLock = this.mPowerManager.newWakeLock(1, "SensorsDozeServiceWakeLock");
        this.mOrientationSensor = new OrientationSensor(this.mContext, this.mSensorManager, this.mOrientationListener);
        this.mPickUpSensor = new PickUpSensor(this.mContext, this.mSensorManager, this.mPickUpListener);
        this.mProximitySensor = new ProximitySensor(this.mContext, this.mSensorManager, this.mProximityListener);
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        this.loadPreferences(defaultSharedPreferences);
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this.mPrefListener);
    }
    
    public void onDestroy() {
        final boolean b = true;
        super.onDestroy();
        this.setOrientationSensor(false, b);
        this.setPickUpSensor(false, b);
        this.setProximitySensor(false, b);
    }
    
    public int onStartCommand(final Intent intent, final int n, final int n2) {
        final IntentFilter intentFilter = new IntentFilter("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mScreenStateReceiver, intentFilter);
        if (!this.mPowerManager.isInteractive()) {
            this.onDisplayOff();
        }
        return 1;
    }
}
