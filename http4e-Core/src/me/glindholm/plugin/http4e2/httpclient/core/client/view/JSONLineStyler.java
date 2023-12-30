package me.glindholm.plugin.http4e2.httpclient.core.client.view;

import java.util.HashMap;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

import me.glindholm.plugin.http4e2.httpclient.core.misc.Styles;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

@SuppressWarnings("unchecked")
public class JSONLineStyler implements LineStyleListener {

    JSONcanner scanner = new JSONcanner();

    int[] tokenColors;

    Color[] colors;

    Vector blockComments = new Vector();

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

    public JSONLineStyler() {
        initializeColors();
        scanner = new JSONcanner();
    }

    Color getColor(final int type) {
        if (type < 0 || type >= tokenColors.length) {
            return null;
        }
        return colors[tokenColors[type]];
    }

    void initializeColors() {
        // Display display = Display.getDefault();
        // colors = new Color[] { new Color(display, new RGB(20, 40, 40)), //
        // black
        // new Color(display, new RGB(155, 0, 0)), // red
        // new Color(display, new RGB(0, 140, 0)), // green
        // new Color(display, new RGB(0, 0, 90)) // blue
        // };
        // tokenColors = new int[MAXIMUM_TOKEN];
        // tokenColors[WORD] = 0;
        // tokenColors[WHITE] = 0;
        // tokenColors[KEY] = 3;
        // tokenColors[COMMENT] = 1;
        // tokenColors[STRING] = 2;
        // tokenColors[OTHER] = 0;
        // tokenColors[NUMBER] = 0;

        colors = new Color[] { ResourceUtils.getColor(Styles.DARK_RGB_TEXT), // black
                ResourceUtils.getColor(Styles.KEY), // red
                ResourceUtils.getColor(Styles.KEY_GREEN), // green
                ResourceUtils.getColor(Styles.KEY_GREEN) // blue
        };

        tokenColors = new int[MAXIMUM_TOKEN];
        tokenColors[WORD] = 1;
        tokenColors[WHITE] = 2;
        tokenColors[KEY] = 0;
        tokenColors[COMMENT] = 2;
        tokenColors[STRING] = 1;
        tokenColors[OTHER] = 2;
        tokenColors[NUMBER] = 1;
    }

    // void disposeColors(){
    // for (int i = 0; i < colors.length; i++) {
    // colors[i].dispose();
    // }
    // }

    /**
     * Event.detail line start offset (input) Event.text line text (input) LineStyleEvent.styles
     * Enumeration of StyleRanges, need to be in order. (output) LineStyleEvent.background line
     * background color (output)
     */
    @Override
    public void lineGetStyle(final LineStyleEvent event) {
        final Vector styles = new Vector();
        int token;
        StyleRange lastStyle;
        // If the line is part of a block comment, create one style for the
        // entire line.
        // if (inBlockComment(event.lineOffset, event.lineOffset
        // + event.lineText.length())) {
        // styles.addElement(new StyleRange(event.lineOffset, event.lineText
        // .length(), getColor(COMMENT), null));
        // event.styles = new StyleRange[styles.size()];
        // styles.copyInto(event.styles);
        // return;
        // }
        final Color defaultFgColor = ((Control) event.widget).getForeground();
        scanner.setRange(event.lineText);
        token = scanner.nextToken();
        while (token != EOF) {
            if (token == OTHER) {
                // do nothing for non-colored tokens
            } else if (token != WHITE) {
                final Color color = getColor(token);
                // Only create a style if the token color is different than the
                // widget's default foreground color and the token's style is
                // not
                // bold. Keywords are bolded.
                if (!color.equals(defaultFgColor) || token == KEY) {
                    final StyleRange style = new StyleRange(scanner.getStartOffset() + event.lineOffset, scanner.getLength(), color, null);
                    if (token == KEY) {
                        style.fontStyle = SWT.BOLD;
                    }
                    if (styles.isEmpty()) {
                        styles.addElement(style);
                    } else {
                        // Merge similar styles. Doing so will improve
                        // performance.
                        lastStyle = (StyleRange) styles.lastElement();
                        if (lastStyle.similarTo(style) && lastStyle.start + lastStyle.length == style.start) {
                            lastStyle.length += style.length;
                        } else {
                            styles.addElement(style);
                        }
                    }
                }
            } else if (!styles.isEmpty() && (lastStyle = (StyleRange) styles.lastElement()).fontStyle == SWT.BOLD) {
                final int start = scanner.getStartOffset() + event.lineOffset;
                lastStyle = (StyleRange) styles.lastElement();
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
        event.styles = new StyleRange[styles.size()];
        styles.copyInto(event.styles);
    }

    /**
     * A simple fuzzy scanner for Java
     */
    public class JSONcanner {

        protected HashMap<String, Integer> fgKeys = null;

        protected StringBuilder fBuffer = new StringBuilder();

        protected String fDoc;

        protected int fPos;

        protected int fEnd;

        protected int fStartToken;

        protected boolean fEofSeen = false;

        private final String[] fgKeywords = { "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default", "do", "double",
                "else", "extends", "false", "final", "finally", "float", "for", "if", "implements", "import", "instanceof", "int", "interface", "long",
                "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "super", "switch", "synchronized", "this",
                "throw", "throws", "transient", "true", "try", "void", "volatile", "while", "undefined" };

        public JSONcanner() {
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
            final Integer k = KEY;
            for (final String fgKeyword : fgKeywords) {
                fgKeys.put(fgKeyword, k);
            }
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

                case '"': // key
                    while (true) {
                        c = read();
                        if (c == '"') {
                            return KEY;
                        } else if (c == EOF) {
                            unread(c);
                            return KEY;
                        } else if (c == '\\') {
                            c = read();
                            break;
                        }
                    }

                    // case '[': // value
                    // while (true) {
                    // c = read();
                    // if (c == ']') {
                    // return COMMENT;
                    // }
                    // else if (c == EOF) {
                    // unread(c);
                    // return COMMENT;
                    // }
                    // else if (c == '\\') {
                    // c = read();
                    // break;
                    // }
                    // }

                case ':': // value
                    while (true) {
                        c = read();
                        // if (c == ',') {
                        // return STRING;
                        // } else
                        if (c == EOF) {
                            unread(c);
                            return STRING;
                        } else if (c == '\\') {
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
                        final Integer i = fgKeys.get(fBuffer.toString());
                        if (i != null) {
                            return i;
                        }
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

        public void setRange(final String text) {
            fDoc = text;
            fPos = 0;
            fEnd = fDoc.length() - 1;
        }

        protected void unread(final int c) {
            if (c != EOF) {
                fPos--;
            }
        }
    }

}