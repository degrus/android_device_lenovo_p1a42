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

import android.content.pm.*;
import android.content.*;

public class BootCompletedReceiver extends BroadcastReceiver
{
    private static final boolean DEBUG = false;
    private static final String TAG = "CMActions";
    
    private void enableComponent(final Context context, final String s) {
        final int n = 1;
        final ComponentName componentName = new ComponentName(context, s);
        final PackageManager packageManager = context.getPackageManager();
        if (packageManager.getComponentEnabledSetting(componentName) == 2) {
            packageManager.setComponentEnabledSetting(componentName, n, n);
        }
    }
    
    public void onReceive(final Context context, final Intent intent) {
        context.startService(new Intent(context, (Class)SensorsDozeService.class));
    }
}
