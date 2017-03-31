/*
 * Copyright (C) 2015 The CyanogenMod Project
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

import android.support.v7.preference.*;
import android.support.v14.preference.*;
import android.provider.*;
import android.content.*;
import android.os.*;
import cyanogenmod.providers.*;

public class TouchscreenGesturePreferenceFragment extends PreferenceFragment
{
    private static final String KEY_AMBIENT_DISPLAY_ENABLE = "ambient_display_enable";
    private static final String KEY_GESTURE_HAND_WAVE = "gesture_hand_wave";
    private static final String KEY_GESTURE_PICK_UP = "gesture_pick_up";
    private static final String KEY_GESTURE_POCKET = "gesture_pocket";
    private static final String KEY_HAPTIC_FEEDBACK = "touchscreen_gesture_haptic_feedback";
    private static final String KEY_PROXIMITY_WAKE = "proximity_wake_enable";
    private Preference$OnPreferenceChangeListener mAmbientDisplayPrefListener;
    private SwitchPreference mAmbientDisplayPreference;
    private Preference$OnPreferenceChangeListener mGesturePrefListener;
    private SwitchPreference mHandwavePreference;
    private SwitchPreference mHapticFeedback;
    private Preference$OnPreferenceChangeListener mHapticPrefListener;
    private SwitchPreference mPickupPreference;
    private SwitchPreference mPocketPreference;
    private SwitchPreference mProximityWakePreference;
    
    public TouchscreenGesturePreferenceFragment() {
        this.mAmbientDisplayPrefListener = new TouchscreenGesturePreferenceFragment$1(this);
        this.mGesturePrefListener = new TouchscreenGesturePreferenceFragment$2(this);
        this.mHapticPrefListener = new TouchscreenGesturePreferenceFragment$3(this);
    }
    
    private boolean enableDoze(final boolean b) {
        final ContentResolver contentResolver = this.getActivity().getContentResolver();
        final String s = "doze_enabled";
        int n;
        if (b) {
            n = 1;
        }
        else {
            n = 0;
        }
        return Settings$Secure.putInt(contentResolver, s, n);
    }
    
    private boolean isDozeEnabled() {
        int n = 1;
        if (Settings$Secure.getInt(this.getActivity().getContentResolver(), "doze_enabled", n) == 0) {
            n = 0;
        }
        return n != 0;
    }
    
    public void onCreatePreferences(final Bundle bundle, final String s) {
        this.addPreferencesFromResource(2131034112);
        final boolean dozeEnabled = this.isDozeEnabled();
        (this.mAmbientDisplayPreference = (SwitchPreference)this.findPreference("ambient_display_enable")).setChecked(dozeEnabled);
        this.mAmbientDisplayPreference.setOnPreferenceChangeListener(this.mAmbientDisplayPrefListener);
        (this.mHandwavePreference = (SwitchPreference)this.findPreference("gesture_hand_wave")).setEnabled(dozeEnabled);
        this.mHandwavePreference.setOnPreferenceChangeListener(this.mGesturePrefListener);
        (this.mPickupPreference = (SwitchPreference)this.findPreference("gesture_pick_up")).setEnabled(dozeEnabled);
        (this.mPocketPreference = (SwitchPreference)this.findPreference("gesture_pocket")).setEnabled(dozeEnabled);
        (this.mProximityWakePreference = (SwitchPreference)this.findPreference("proximity_wake_enable")).setOnPreferenceChangeListener(this.mGesturePrefListener);
        (this.mHapticFeedback = (SwitchPreference)this.findPreference("touchscreen_gesture_haptic_feedback")).setOnPreferenceChangeListener(this.mHapticPrefListener);
    }
    
    public void onResume() {
        int checked = 1;
        super.onResume();
        final SwitchPreference mHapticFeedback = this.mHapticFeedback;
        if (CMSettings$System.getInt(this.getActivity().getContentResolver(), "touchscreen_gesture_haptic_feedback", checked) == 0) {
            checked = 0;
        }
        mHapticFeedback.setChecked(checked != 0);
        this.getListView().setPadding(0, 0, 0, 0);
    }
}
