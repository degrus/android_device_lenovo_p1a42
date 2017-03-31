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

import com.android.settingslib.drawer.*;
import android.os.*;
import android.app.*;

public class TouchscreenGesturePreferenceActivity extends SettingsDrawerActivity
{
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        this.getFragmentManager().beginTransaction().replace(2131492982, (Fragment)new TouchscreenGesturePreferenceFragment()).commit();
    }
}
