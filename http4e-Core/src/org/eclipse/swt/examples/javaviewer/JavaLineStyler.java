/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.swt.examples.javaviewer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

/**
 * From <a href="https://github.com/eclipse-platform/eclipse.platform.swt/blob/master/examples/org.eclipse.swt.examples/src/org/eclipse/swt/examples/javaviewer/JavaLineStyler.java">JavaLineStyler</>
 */
public class JavaLineStyler implements LineStyleListener {
    JavaScanner scanner = new JavaScanner();
    int[] tokenColors;
    Color[] colors;
    List<int[]> blockComments = new ArrayList<>();

    public static final int EOF = -1;
    public static final int EOL = 10;

    public static final int WORD = 0;
    public static final int WHITE = 1;
    public static final int KEY = 2;
    public static final int COMMENT = 3;
    public static final int STRING = 5;
    public static final int OTHER = 6;
    public static final int NUMBER = 7;

    public static final int MAXIMUM_TOKEN = 8;

    public JavaLineStyler() {
        initializeColors();
        scanner = new JavaScanner();
    }

    Color getColor(int type) {
        if (type < 0 || type >= tokenColors.length) {
            return null;
        }
        return colors[tokenColors[type]];
    }

    boolean inBlockComment(int start, int end) {
        for (int[] offsets : blockComments) {
            // start of comment in the line
            if ((offsets[0] >= start) && (offsets[0] <= end))
                return true;
            // end of comment in the line
            if ((offsets[1] >= start) && (offsets[1] <= end))
                return true;
            if ((offsets[0] <= start) && (offsets[1] >= end))
                return true;
        }
        return false;
    }

    void initializeColors() {
        colors = new Color[] { new Color(new RGB(0, 0, 0)), // black
                new Color(new RGB(255, 0, 0)), // red
                new Color(new RGB(0, 255, 0)), // green
                new Color(new RGB(0, 0, 255)) // blue
        };
        tokenColors = new int[MAXIMUM_TOKEN];
        tokenColors[WORD] = 0;
        tokenColors[WHITE] = 0;
        tokenColors[KEY] = 3;
        tokenColors[COMMENT] = 1;
        tokenColors[STRING] = 2;
        tokenColors[OTHER] = 0;
        tokenColors[NUMBER] = 0;
    }

    /**
     * Event.detail line start offset (input) Event.text line text (input) LineStyleEvent.styles
     * Enumeration of StyleRanges, need to be in order. (output) LineStyleEvent.background line
     * background color (output)
     */
    @Override
    public void lineGetStyle(LineStyleEvent event) {
        List<StyleRange> styles = new ArrayList<>();
        int token;
        StyleRange lastStyle;
        // If the line is part of a block comment, create one style for the entire line.
        if (inBlockComment(event.lineOffset, event.lineOffset + event.lineText.length())) {
            styles.add(new StyleRange(event.lineOffset, event.lineText.length(), getColor(COMMENT), null));
            event.styles = styles.toArray(new StyleRange[styles.size()]);
            return;
        }
        Color defaultFgColor = ((Control) event.widget).getForeground();
        scanner.setRange(event.lineText);
        token = scanner.nextToken();
        while (token != EOF) {
            if (token == OTHER) {
                // do nothing for non-colored tokens
            } else if (token != WHITE) {
                Color color = getColor(token);
                // Only create a style if the token color is different than the
                // widget's default foreground color and the token's style is not
                // bold. Keywords are bolded.
                if ((!color.equals(defaultFgColor)) || (token == KEY)) {
                    StyleRange style = new StyleRange(scanner.getStartOffset() + event.lineOffset, scanner.getLength(), color, null);
                    if (token == KEY) {
                        style.fontStyle = SWT.BOLD;
                    }
                    if (styles.isEmpty()) {
                        styles.add(style);
                    } else {
                        // Merge similar styles. Doing so will improve performance.
                        lastStyle = styles.get(styles.size() - 1);
                        if (lastStyle.similarTo(style) && (lastStyle.start + lastStyle.length == style.start)) {
                            lastStyle.length += style.length;
                        } else {
                            styles.add(style);
                        }
                    }
                }
            } else if ((!styles.isEmpty()) && ((lastStyle = styles.get(styles.size() - 1)).fontStyle == SWT.BOLD)) {
                int start = scanner.getStartOffset() + event.lineOffset;
                lastStyle = styles.get(styles.size() - 1);
                // A font style of SWT.BOLD implies that the last style
                // represents a java keyword.
                if (lastStyle.start + lastStyle.length == start) {
                    // Have the white space take on the style before it to
                    // minimize the number of style ranges created and the
                    // number of font style changes during rendering.
                    lastStyle.length += scanner.getLength();
                }
            }
            token = scanner.nextToken();
        }
        event.styles = styles.toArray(new StyleRange[styles.size()]);
    }

    public void parseBlockComments(String text) {
        blockComments = new ArrayList<>();
        try (StringReader buffer = new StringReader(text)) {
            int ch;
            boolean blkComment = false;
            int cnt = 0;
            int[] offsets = new int[2];
            boolean done = false;

            try {
                while (!done) {
                    switch (ch = buffer.read()) {
                    case -1: {
                        if (blkComment) {
                            offsets[1] = cnt;
                            blockComments.add(offsets);
                        }
                        done = true;
                        break;
                    }
                    case '/': {
                        ch = buffer.read();
                        if ((ch == '*') && (!blkComment)) {
                            offsets = new int[2];
                            offsets[0] = cnt;
                            blkComment = true;
                            cnt++;
                        } else {
                            cnt++;
                        }
                        cnt++;
                        break;
                    }
                    case '*': {
                        if (blkComment) {
                            ch = buffer.read();
                            cnt++;
                            if (ch == '/') {
                                blkComment = false;
                                offsets[1] = cnt;
                                blockComments.add(offsets);
                            }
                        }
                        cnt++;
                        break;
                    }
                    default: {
                        cnt++;
                        break;
                    }
                    }
                }
            } catch (IOException e) {
                // ignore errors
            }
        }
    }

    /**
     * A simple fuzzy scanner for Java
     */
    public static class JavaScanner {

        protected Map<String, Integer> fgKeys = null;
        protected StringBuilder fBuffer = new StringBuilder();
        protected String fDoc;
        protected int fPos;
        protected int fEnd;
        protected int fStartToken;
        protected boolean fEofSeen = false;

        private String[] fgKeywords = { "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default", "do", "double", "else",
                "extends", "false", "final", "finally", "float", "for", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new",
                "null", "package", "private", "protected", "public", "return", "short", "static", "super", "switch", "synchronized", "this", "throw", "throws",
                "transient", "true", "try", "void", "volatile", "while" };

        public JavaScanner() {
            initialize();
        }

        /**
         * Returns the ending location of the current token in the document.
         */
        public final int getLength() {
            return fPos - fStartToken;
        }

        /**
         * Initialize the lookup table.
         */
        void initialize() {
            fgKeys = new HashMap<>();
            Integer k = Integer.valueOf(KEY);
            for (String word : fgKeywords)
                fgKeys.put(word, k);
        }

        /**
         * Returns the starting location of the current token in the document.
         */
        public final int getStartOffset() {
            return fStartToken;
        }

        /**
         * Returns the next lexical token in the document.
         */
        public int nextToken() {
            int c;
            fStartToken = fPos;
            while (true) {
                switch (c = read()) {
                case EOF:
                    return EOF;
                case '/': // comment
                    c = read();
                    if (c == '/') {
                        while (true) {
                            c = read();
                            if ((c == EOF) || (c == EOL)) {
                                unread(c);
                                return COMMENT;
                            }
                        }
                    }
                    unread(c);
                    return OTHER;
                case '\'': // char const
                    while (true) {
                        c = read();
                        switch (c) {
                        case '\'':
                            return STRING;
                        case EOF:
                            unread(c);
                            return STRING;
                        case '\\':
                            c = read();
                            break;
                        }
                    }

                case '"': // string
                    while (true) {
                        c = read();
                        switch (c) {
                        case '"':
                            return STRING;
                        case EOF:
                            unread(c);
                            return STRING;
                        case '\\':
                            c = read();
                            break;
                        }
                    }

                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    do {
                        c = read();
                    } while (Character.isDigit((char) c));
                    unread(c);
                    return NUMBER;
                default:
                    if (Character.isWhitespace((char) c)) {
                        do {
                            c = read();
                        } while (Character.isWhitespace((char) c));
                        unread(c);
                        return WHITE;
                    }
                    if (Character.isJavaIdentifierStart((char) c)) {
                        fBuffer.setLength(0);
                        do {
                            fBuffer.append((char) c);
                            c = read();
                        } while (Character.isJavaIdentifierPart((char) c));
                        unread(c);
                        Integer i = fgKeys.get(fBuffer.toString());
                        if (i != null)
                            return i.intValue();
                        return WORD;
                    }
                    return OTHER;
                }
            }
        }

        /**
         * Returns next character.
         */
        protected int read() {
            if (fPos <= fEnd) {
                return fDoc.charAt(fPos++);
            }
            return EOF;
        }

        public void setRange(String text) {
            fDoc = text;
            fPos = 0;
            fEnd = fDoc.length() - 1;
        }

        protected void unread(int c) {
            if (c != EOF)
                fPos--;
        }
    }

}