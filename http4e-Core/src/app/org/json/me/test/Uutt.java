package org.json.me.test;

import java.io.BufferedReader;
import java.io.FileReader;

public class Uutt {

    public static String readFileAsString(final String filePath) throws java.io.IOException {
        final StringBuilder fileData = new StringBuilder(1000);
        final BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            final String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
}
