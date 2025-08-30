package com.selalerer.edit;

import java.util.Arrays;

public class Utils {

    public static String toString(Object o) {
        if (o == null) {
            return "null";
        }
        if (o.getClass().isArray()) {
            if (char.class.equals(o.getClass().getComponentType())) {
                return Arrays.toString((char[]) o);
            }
            if (byte.class.equals(o.getClass().getComponentType())) {
                 return Arrays.toString((byte[]) o);
            }
            if (short.class.equals(o.getClass().getComponentType())) {
                return Arrays.toString((short[]) o);
            }
            if (int.class.equals(o.getClass().getComponentType())) {
                return Arrays.toString((int[]) o);
            }
            if (long.class.equals(o.getClass().getComponentType())) {
                return Arrays.toString((long[]) o);
            }
            if (boolean.class.equals(o.getClass().getComponentType())) {
                return Arrays.toString((boolean[]) o);
            }
            if (float.class.equals(o.getClass().getComponentType())) {
                return Arrays.toString((float[]) o);
            }
            if (double.class.equals(o.getClass().getComponentType())) {
                return Arrays.toString((double[]) o);
            }
            return Arrays.toString((Object[]) o);
        }
        return String.valueOf(o);
    }
}
