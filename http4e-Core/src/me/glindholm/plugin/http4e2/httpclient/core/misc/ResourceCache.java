/*
 *  Copyright 2017 Eclipse HttpClient (http4e) https://nextinterfaces.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.glindholm.plugin.http4e2.httpclient.core.misc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.misc.Styles.FontStyle;

/**
 * This class retrieves http4e resources and caches them. The resources should be disposed when
 * plugin is closed.
 *
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class ResourceCache {

    private final Map<Object, Image> imageMap = new HashMap<>();
    private final Map<String, Font> fontMap = new HashMap<>();
    private final Map<String, Color> colorMap = new HashMap<>();
    private final Map<Integer, Cursor> cursorMap = new HashMap<>();

    // ////////////////////////////
    // Images support
    // ////////////////////////////

    public Image getImage(final ImageDescriptor imageDescriptor) {
        if (imageDescriptor == null) {
            return null;
        }
        return imageMap.computeIfAbsent(imageDescriptor, i -> imageDescriptor.createImage());
    }

    public Image getImage(final String name) {
        return imageMap.computeIfAbsent(name, i -> new Image(Display.getCurrent(), name));
    }

    public void disposeImages() {
        for (final Image image : imageMap.values()) {
            image.dispose();
        }
        imageMap.clear();
    }

    // ////////////////////////////
    // Font support
    // ////////////////////////////
    /**
     * Returns a font based on its name, height and style
     *
     * @param name   String The name of the font
     * @param height int The height of the font
     * @param style  int The style of the font
     * @return Font The font matching the name, height and style
     */
    public Font getFont(final FontStyle fontStyle) {
        return getFont(fontStyle.name, fontStyle.height, fontStyle.style, false, false);
    }

    /**
     * Returns a font based on its name, height and style. Windows-specific strikeout and underline
     * flags are also supported.
     *
     * @param name      String The name of the font
     * @param size      int The size of the font
     * @param style     int The style of the font
     * @param strikeout boolean The strikeout flag (warning: Windows only)
     * @param underline boolean The underline flag (warning: Windows only)
     * @return Font The font matching the name, height, style, strikeout and underline
     */
    private Font getFont(final String name, final int size, final int style, final boolean strikeout, final boolean underline) {
        final String fontName = name + '|' + size + '|' + style + '|' + strikeout + '|' + underline;
        return fontMap.computeIfAbsent(fontName, f -> createFont(name, size, style, strikeout, underline));
    }

    private static Font createFont(final String name, final int size, final int style, final boolean strikeout, final boolean underline) {
        Font font;
        final FontData fontData = new FontData(name, size, style);
        if (strikeout || underline) {
            try {
                final Class<?> logFontClass = Class.forName("org.eclipse.swt.internal.win32.LOGFONT");
                final Object logFont = FontData.class.getField("data").get(fontData);
                if (logFont != null && logFontClass != null) {
                    if (strikeout) {
                        logFontClass.getField("lfStrikeOut").set(logFont, Byte.valueOf((byte) 1));
                    }
                    if (underline) {
                        logFontClass.getField("lfUnderline").set(logFont, Byte.valueOf((byte) 1));
                    }
                }
            } catch (final Throwable e) {
                ExceptionHandler.warn("Unable to set underline or strikeout" + " (probably on a non-Windows platform). " + e);
            }
        }
        font = new Font(Display.getCurrent(), fontData);
        return font;
    }

    /**
     * Dispose all of the cached fonts
     */
    public void disposeFonts() {
        for (final Font font : fontMap.values()) {
            font.dispose();
        }
        fontMap.clear();
    }

    // ////////////////////////////
    // Color support
    // ////////////////////////////

    public Color getColor(final int red, final int green, final int blue) {
        return getColor(new RGB(red, green, blue));
    }

    public Color getColor(final RGB rgb) {
        final String key = rgb.blue + "|" + rgb.green + "|" + rgb.red;

        return colorMap.computeIfAbsent(key, c -> new Color(Display.getCurrent(), rgb));
    }

    public void disposeColors() {
        for (final Color color : colorMap.values()) {
            color.dispose();
        }
        colorMap.clear();
    }

    // ////////////////////////////
    // Cursor support
    // ////////////////////////////

    /**
     * Returns the system cursor matching the specific ID
     *
     * @param id int The ID value for the cursor
     * @return Cursor The system cursor matching the specific ID
     */
    public Cursor getCursor(final int id) {
        return cursorMap.computeIfAbsent(id, c -> new Cursor(Display.getDefault(), id));
    }

    /**
     * Dispose all of the cached cursors
     */
    public void disposeCursors() {
        for (final Cursor cursor : cursorMap.values()) {
            cursor.dispose();
        }
        cursorMap.clear();
    }

    public void dispose() {
        disposeImages();
        disposeCursors();
        disposeFonts();
        disposeColors();
    }

}