package com.ruben.github_users_rest_api.utilities;

import lombok.val;

import java.util.zip.CRC32;

public class Checksum {
    public static long getCRC32Checksum(byte[] bytes) {
        val crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }
}
