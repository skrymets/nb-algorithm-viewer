package org.netbeans.module.sandbox.utils;

import java.util.UUID;

public class IDUtility {

    public static String generateID() {
        return UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
    }

}
