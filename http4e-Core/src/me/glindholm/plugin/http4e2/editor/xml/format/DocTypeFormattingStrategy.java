/*
 * Created on Oct 11, 2004
 */
package me.glindholm.plugin.http4e2.editor.xml.format;

/**
 * 
 * @author Phil Zoio
 */
public class DocTypeFormattingStrategy extends DefaultFormattingStrategy {

    @Override
    public String format(final String content, final boolean isLineStart, final String indentation, final int[] positions) {
        return lineSeparator + content;
    }

}