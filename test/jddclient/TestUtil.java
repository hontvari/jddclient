package jddclient;

import java.lang.reflect.Field;

public abstract class TestUtil {
    public static void setField(Object o, String name, Object v) {
        try {
            Field field = field(o, name);
            field.setAccessible(true);
            field.set(o, v);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getField(Object o, String name) {
        try {
            Field field = field(o, name);
            field.setAccessible(true);
            return field.get(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Field field(Object o, String name) {
        Field field = null;
        Class<?> clazz = o.getClass();
        do {
            try {
                field = clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
                if (clazz == null)
                    throw new IllegalArgumentException("No such field: " + name);
            }
        } while (field == null);
        return field;
    }
}
