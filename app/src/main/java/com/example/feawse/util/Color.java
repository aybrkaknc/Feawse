package com.example.feawse.util;

/**
 * Lightweight platform-independent Color class replacing JavaFX Color.
 * Used by savefile blocks for avatar and unit hair color parsing/formatting.
 */
public class Color {
    private final int r;
    private final int g;
    private final int b;
    private final int a;

    public Color(int r, int g, int b, int a) {
        this.r = Math.max(0, Math.min(255, r));
        this.g = Math.max(0, Math.min(255, g));
        this.b = Math.max(0, Math.min(255, b));
        this.a = Math.max(0, Math.min(255, a));
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(double r, double g, double b, double a) {
        this((int) Math.round(r * 255), (int) Math.round(g * 255), (int) Math.round(b * 255), (int) Math.round(a * 255));
    }

    public static Color rgb(int r, int g, int b) {
        return new Color(r, g, b, 255);
    }

    public static Color rgb(int r, int g, int b, double opacity) {
        return new Color(r, g, b, (int) (opacity * 255));
    }

    public double getRed() {
        return r / 255.0;
    }

    public double getGreen() {
        return g / 255.0;
    }

    public double getBlue() {
        return b / 255.0;
    }

    public double getOpacity() {
        return a / 255.0;
    }

    public int getRedInt() {
        return r;
    }

    public int getGreenInt() {
        return g;
    }

    public int getBlueInt() {
        return b;
    }

    public int getAlphaInt() {
        return a;
    }

    public String toHexString() {
        return String.format("%02X%02X%02X", r, g, b);
    }

    @Override
    public String toString() {
        return toHexString();
    }
}
