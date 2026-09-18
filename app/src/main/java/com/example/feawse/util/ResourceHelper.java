package com.example.feawse.util;

import android.content.Context;
import java.io.InputStream;

/**
 * Android resource & asset helper.
 * Provides safe fallback to Android AssetManager when classloader getResourceAsStream is unavailable.
 */
public class ResourceHelper {
    private static Context applicationContext;

    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
    }

    public static InputStream openAsset(String assetPath) {
        if (applicationContext == null) return null;
        try {
            String clean = assetPath.startsWith("/") ? assetPath.substring(1) : assetPath;
            return applicationContext.getAssets().open(clean);
        } catch (Exception e) {
            return null;
        }
    }

    public static InputStream getStream(Class<?> clazz, String resourcePath, String assetRelativePath) {
        try {
            InputStream is = clazz.getResourceAsStream(resourcePath);
            if (is != null) return is;
        } catch (Exception ignored) {}

        try {
            if (clazz.getClassLoader() != null) {
                String clean = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
                InputStream is = clazz.getClassLoader().getResourceAsStream(clean);
                if (is != null) return is;
            }
        } catch (Exception ignored) {}

        return openAsset(assetRelativePath);
    }
}
