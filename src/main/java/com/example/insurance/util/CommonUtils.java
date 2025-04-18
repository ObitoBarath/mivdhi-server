package com.example.insurance.util;

import java.util.Collection;

public class CommonUtils {


    public static boolean nullOrEmpty(Collection<?> collection)
    {
        return collection == null || collection.isEmpty();
    }

    public static boolean nullOrEmpty(Integer value)
    {
        return value == null ;
    }

    public static boolean nullOrEmpty(String value)
    {
        return value == null || value.isBlank();
    }
}
