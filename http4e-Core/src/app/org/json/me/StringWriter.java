/*
 * Copyright 2006 Sun Microsystems, Inc.
 */

package org.json.me;

import java.io.IOException;
import java.io.Writer;

/**
 * A simple StringBuffer-based implementation of StringWriter
 */
public class StringWriter extends Writer {

    final private StringBuffer buf;

    public StringWriter() {
        buf = new StringBuffer();
    }

    public StringWriter(final int initialSize) {
        buf = new StringBuffer(initialSize);
    }

    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        buf.append(cbuf, off, len);
    }

    @Override
    public void write(final String str) throws IOException {
        buf.append(str);
    }

    @Override
    public void write(final String str, final int off, final int len) throws IOException {
        buf.append(str.substring(off, len));
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }
}
