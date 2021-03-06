/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.dialer.oem;

import android.content.Context;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import com.android.dialer.common.ConfigProviderBindings;
import com.android.dialer.common.PackageUtils;

/** Util class for Motorola OEM devices. */
public class MotorolaUtils {

  private static final String CONFIG_HD_CODEC_BLINKING_ICON_WHEN_CONNECTING_CALL_ENABLED =
      "hd_codec_blinking_icon_when_connecting_enabled";
  private static final String CONFIG_HD_CODEC_SHOW_ICON_IN_NOTIFICATION_ENABLED =
      "hd_codec_show_icon_in_notification_enabled";
  private static final String CONFIG_HD_CODEC_SHOW_ICON_IN_CALL_LOG_ENABLED =
      "hd_codec_show_icon_in_call_log_enabled";
  private static final String CONFIG_WIFI_CALL_SHOW_ICON_IN_CALL_LOG_ENABLED =
      "wifi_call_show_icon_in_call_log_enabled";

  // This is used to check if a Motorola device supports HD voice call feature, which comes from
  // system feature setting.
  private static final String HD_CALL_FEATRURE = "com.motorola.software.sprint.hd_call";
  // This is used to check if a Motorola device supports WiFi call feature, by checking if a certain
  // package is enabled.
  private static final String WIFI_CALL_PACKAGE_NAME = "com.motorola.sprintwfc";

  // Feature flag indicates it's a HD call, currently this is only used by Motorola system build.
  // TODO(b/35359461): Use reference to android.provider.CallLog once it's in new SDK.
  private static final int FEATURES_HD_CALL = 0x4;
  // Feature flag indicates it's a WiFi call, currently this is only used by Motorola system build.
  private static final int FEATURES_WIFI = 0x8;

  private static boolean hasCheckedSprintWifiCall;
  private static boolean supportSprintWifiCall;

  /**
   * Returns true if SPN is specified and matched the current sim operator name. This is necessary
   * since mcc310-mnc000 is not sufficient to identify Sprint network.
   */
  static boolean isSpnMatched(Context context) {
    try {
      String spnResource = context.getResources().getString(R.string.motorola_enabled_spn);
      return spnResource.equalsIgnoreCase(
          context.getSystemService(TelephonyManager.class).getSimOperatorName());
    } catch (Resources.NotFoundException exception) {
      // If SPN is not specified we consider as not necessary to enable/disable the feature.
      return true;
    }
  }

  public static boolean shouldBlinkHdIconWhenConnectingCall(Context context) {
    return ConfigProviderBindings.get(context)
            .getBoolean(CONFIG_HD_CODEC_BLINKING_ICON_WHEN_CONNECTING_CALL_ENABLED, true)
        && isSupportingSprintHdCodec(context);
  }

  public static boolean shouldShowHdIconInNotification(Context context) {
    return ConfigProviderBindings.get(context)
            .getBoolean(CONFIG_HD_CODEC_SHOW_ICON_IN_NOTIFICATION_ENABLED, true)
        && isSupportingSprintHdCodec(context);
  }

  public static boolean shouldShowHdIconInCallLog(Context context, int features) {
    return ConfigProviderBindings.get(context)
            .getBoolean(CONFIG_HD_CODEC_SHOW_ICON_IN_CALL_LOG_ENABLED, true)
        && (features & FEATURES_HD_CALL) == FEATURES_HD_CALL
        && isSupportingSprintHdCodec(context);
  }

  public static boolean shouldShowWifiIconInCallLog(Context context, int features) {
    return ConfigProviderBindings.get(context)
            .getBoolean(CONFIG_WIFI_CALL_SHOW_ICON_IN_CALL_LOG_ENABLED, true)
        && (features & FEATURES_WIFI) == FEATURES_WIFI
        && isSupportingSprintWifiCall(context);
  }

  /**
   * Handle special char sequence entered in dialpad. This may launch special intent based on input.
   *
   * @param context context
   * @param input input string
   * @return true if the input is consumed and the intent is launched
   */
  public static boolean handleSpecialCharSequence(Context context, String input) {
    // TODO(b/35395377): Add check for Motorola devices.
    return MotorolaHiddenMenuKeySequence.handleCharSequence(context, input);
  }

  private static boolean isSupportingSprintHdCodec(Context context) {
    return isSpnMatched(context)
        && context.getResources().getBoolean(R.bool.motorola_sprint_hd_codec)
        && context.getPackageManager().hasSystemFeature(HD_CALL_FEATRURE);
  }

  private static boolean isSupportingSprintWifiCall(Context context) {
    if (!hasCheckedSprintWifiCall) {
      supportSprintWifiCall = PackageUtils.isPackageEnabled(WIFI_CALL_PACKAGE_NAME, context);
      hasCheckedSprintWifiCall = true;
    }
    return supportSprintWifiCall;
  }
}
