/*
 * Copyright (C) 2025-2026 AxionOS
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

package com.android.launcher3.icons;

import android.content.Context;
import android.content.res.Configuration;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ThemedIconSettings {

    public static final String KEY_ICON_SCALE = "themed_icon_scale";
    public static final String KEY_BACKGROUND_COLOR = "themed_icon_background_color";
    public static final String KEY_FOREGROUND_COLOR = "themed_icon_foreground_color";
    public static final String KEY_COLOR_PRESET = "themed_icon_color_preset";
    public static final String KEY_THEMED_ICONS = "themed_icons";

    public static final String COLOR_PRESET_AXICONS = "axicons";
    public static final String COLOR_PRESET_AOSP = "aosp";
    public static final String COLOR_PRESET_CUSTOM = "custom";

    public static final int DEFAULT_ICON_SCALE = 100;
    public static final int MIN_ICON_SCALE = 48;
    public static final int MAX_ICON_SCALE = 100;

    private ThemedIconSettings() { }

    @NonNull
    public static String getState(@Nullable Context context) {
        if (context == null) {
            return "";
        }
        return "," + getIconScale(context)
                + "," + getBackgroundColor(context)
                + "," + getForegroundColor(context)
                + "," + getColorPreset(context);
    }

    public static boolean isThemedIconsEnabled(@Nullable Context context) {
        return context != null && Settings.Secure.getInt(
                context.getContentResolver(), KEY_THEMED_ICONS, 0) == 1;
    }

    public static int getIconScale(@NonNull Context context) {
        return clamp(Settings.Secure.getInt(
                context.getContentResolver(), KEY_ICON_SCALE, DEFAULT_ICON_SCALE),
                MIN_ICON_SCALE, MAX_ICON_SCALE);
    }

    public static int getIconInsetInPixels(@NonNull Context context, int size) {
        if (size <= 0) {
            return 0;
        }
        return Math.round(size * (1f - (getIconScale(context) / 100f)) / 2f);
    }

    public static float getIconInsetFraction(@NonNull Context context) {
        return (1f - (getIconScale(context) / 100f)) / 2f;
    }

    public static int getBackgroundColor(@NonNull Context context) {
        String preset = getColorPreset(context);
        if (COLOR_PRESET_AOSP.equals(preset)) {
            return getAospBackgroundColor(context);
        }
        if (COLOR_PRESET_CUSTOM.equals(preset)) {
            return getColor(context, KEY_BACKGROUND_COLOR, getAxBackgroundColor(context));
        }
        return getAxBackgroundColor(context);
    }

    public static int getForegroundColor(@NonNull Context context) {
        String preset = getColorPreset(context);
        if (COLOR_PRESET_AOSP.equals(preset)) {
            return getAospForegroundColor(context);
        }
        if (COLOR_PRESET_CUSTOM.equals(preset)) {
            return getColor(context, KEY_FOREGROUND_COLOR, getAxForegroundColor(context));
        }
        return getAxForegroundColor(context);
    }

    public static int getAdaptiveBackgroundColor(@NonNull Context context) {
        String preset = getColorPreset(context);
        if (COLOR_PRESET_AXICONS.equals(preset)) {
            return context.getResources().getColor(R.color.ax_themed_icon_adaptive_background_color);
        }
        if (COLOR_PRESET_AOSP.equals(preset)) {
            return context.getResources().getColor(R.color.themed_icon_adaptive_background_color);
        }
        int background = getBackgroundColor(context);
        int foreground = getForegroundColor(context);
        if (background == getAxBackgroundColor(context)
                && foreground == getAxForegroundColor(context)) {
            return context.getResources().getColor(R.color.ax_themed_icon_adaptive_background_color);
        }
        if (background == getAospBackgroundColor(context)
                && foreground == getAospForegroundColor(context)) {
            return context.getResources().getColor(R.color.themed_icon_adaptive_background_color);
        }
        return background;
    }

    public static int getAxBackgroundColor(@NonNull Context context) {
        return context.getResources().getColor(R.color.ax_themed_icon_background_color);
    }

    public static int getAxBackgroundColor(@NonNull Context context, boolean night) {
        return getResourceColor(context, R.color.ax_themed_icon_background_color, night);
    }

    public static int getAxForegroundColor(@NonNull Context context) {
        return context.getResources().getColor(R.color.ax_themed_icon_color);
    }

    public static int getAxForegroundColor(@NonNull Context context, boolean night) {
        return getResourceColor(context, R.color.ax_themed_icon_color, night);
    }

    public static int getAospBackgroundColor(@NonNull Context context) {
        return context.getResources().getColor(R.color.themed_icon_background_color);
    }

    public static int getAospBackgroundColor(@NonNull Context context, boolean night) {
        return getResourceColor(context, R.color.themed_icon_background_color, night);
    }

    public static int getAospForegroundColor(@NonNull Context context) {
        return context.getResources().getColor(R.color.themed_icon_color);
    }

    public static int getAospForegroundColor(@NonNull Context context, boolean night) {
        return getResourceColor(context, R.color.themed_icon_color, night);
    }

    @NonNull
    public static String getColorPreset(@NonNull Context context) {
        String preset = Settings.Secure.getString(context.getContentResolver(), KEY_COLOR_PRESET);
        if (isColorPreset(preset)) {
            return preset;
        }

        Integer background = parseColor(Settings.Secure.getString(
                context.getContentResolver(), KEY_BACKGROUND_COLOR));
        Integer foreground = parseColor(Settings.Secure.getString(
                context.getContentResolver(), KEY_FOREGROUND_COLOR));
        if (background == null && foreground == null) {
            return COLOR_PRESET_AXICONS;
        }
        if (background != null && foreground != null) {
            if (matchesDefaultColors(context, background, foreground, COLOR_PRESET_AXICONS)) {
                return COLOR_PRESET_AXICONS;
            }
            if (matchesDefaultColors(context, background, foreground, COLOR_PRESET_AOSP)) {
                return COLOR_PRESET_AOSP;
            }
        }
        return COLOR_PRESET_CUSTOM;
    }

    private static boolean isColorPreset(@Nullable String preset) {
        return COLOR_PRESET_AXICONS.equals(preset)
                || COLOR_PRESET_AOSP.equals(preset)
                || COLOR_PRESET_CUSTOM.equals(preset);
    }

    private static boolean matchesDefaultColors(@NonNull Context context, int background,
            int foreground, @NonNull String preset) {
        if (COLOR_PRESET_AOSP.equals(preset)) {
            return matchesDefaultColors(background, foreground,
                    getAospBackgroundColor(context, false), getAospForegroundColor(context, false))
                    || matchesDefaultColors(background, foreground,
                            getAospBackgroundColor(context, true),
                            getAospForegroundColor(context, true));
        }
        return matchesDefaultColors(background, foreground,
                getAxBackgroundColor(context, false), getAxForegroundColor(context, false))
                || matchesDefaultColors(background, foreground,
                        getAxBackgroundColor(context, true), getAxForegroundColor(context, true));
    }

    private static boolean matchesDefaultColors(int background, int foreground,
            int defaultBackground, int defaultForeground) {
        return background == defaultBackground && foreground == defaultForeground;
    }

    private static int getColor(@NonNull Context context, @NonNull String key, int defaultColor) {
        Integer value = parseColor(Settings.Secure.getString(context.getContentResolver(), key));
        return value != null ? value : defaultColor;
    }

    @Nullable
    private static Integer parseColor(@Nullable String value) {
        try {
            return value != null ? (int) Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int getResourceColor(@NonNull Context context, int resId, boolean night) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.uiMode = (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                | (night ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO);
        return context.createConfigurationContext(configuration).getResources().getColor(resId);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
