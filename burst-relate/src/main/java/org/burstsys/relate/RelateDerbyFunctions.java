/* Copyright Yahoo, Licensed under the terms of the Apache 2.0 license. See LICENSE file in project root for terms. */
package org.burstsys.relate;

import org.apache.derby.iapi.types.HarmonySerialBlob;

import java.nio.charset.StandardCharsets;
import java.sql.Blob;

public class RelateDerbyFunctions {
    public static Blob stringToBlob(String data) throws Exception {
        return new HarmonySerialBlob(data.getBytes(StandardCharsets.UTF_8));
    }
}
