package me.glindholm.plugin.http4e2.crypt;

import java.io.IOException;
import java.io.InputStream;

public class MiscUtils {

    public static byte[] streamToBytes(final InputStream in) throws IOException {
        final byte[] bytes = new byte[in.available()];
        in.read(bytes);
        in.close();
        return bytes;
    }

}
