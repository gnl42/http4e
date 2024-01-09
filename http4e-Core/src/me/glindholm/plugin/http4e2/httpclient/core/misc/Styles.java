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

import org.eclipse.jface.resource.FontRegistry;
/**
 * This class contains all custom http4e styles (think of it as css)
 *
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;

public class Styles {

    public static final RGB COMMENT = new RGB(63, 127, 95);
    public static final RGB PROC_INSTR = new RGB(128, 128, 128);
    public static final RGB DEFAULT = new RGB(0, 0, 0);
    public static final RGB KEY = new RGB(42, 0, 255);
    public static final RGB KEY_GREEN = new RGB(0, 140, 0);
    public static final RGB STRING = new RGB(41, 41, 51);
    public static final RGB CONTENT_ASSIST = new RGB(255, 255, 240);

    public static final RGB BACKGROUND_FORM_TYPE = new RGB(242, 255, 242);
    public static final RGB LIGHT_RGB_TEXT = new RGB(180, 160, 150);
    public static final RGB BLACK_RGB_TEXT = new RGB(0, 0, 0);
    public static final RGB WHITE_RGB_TEXT = new RGB(255, 255, 255); // Headers text
    public static final RGB YELLOW_RGB_TEXT = new RGB(0, 255, 0);
    public static final RGB BLUE_RGB_TEXT = new RGB(0, 255, 255);
    public static final RGB GRAY_RGB_TEXT = new RGB(15, 255, 15);
    public static final RGB BACKGROUND_ENABLED = new RGB(255, 255, 255);
    public static final RGB BACKGROUND_DISABLED = new RGB(253, 253, 0);// Headers background
    public static final RGB BACKGROUND_FIND = new RGB(242, 242, 234);
    public static final RGB SSL = new RGB(0, 0, 128);
    public static final RGB GREY_DISABLED = new RGB(220, 220, 0); // Params background
    public static final RGB HTTP_RGB_TEXT = new RGB(105, 0, 105);

    private static Styles styles = null;

    private Styles(final Shell shell) {
        final ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
        final FontRegistry fontRegistry = theme.getFontRegistry();
        final Font font = fontRegistry.get("org.eclipse.debug.ui.consoleFont");
        final FontData fd = font.getFontData()[0];
        fontStyle = new FontStyle(fd.getName(), fd.getHeight(), fd.getStyle());
    }

    public static Styles getInstance(final Shell shell) {
        if (styles == null) {
            styles = new Styles(shell);
        }
        return styles;
    }

//   private static final int      COURIER_SIZE = SwtUtils.isWindows() ? 8 : (SwtUtils.isMac() ? 11 : 10);
//   /*
//    * The default non 'monospaced' makes HEX formatting look bad on Mac.
//    */
//   public static final FontStyle FONT_COURIER = SwtUtils.isMac() ? new FontStyle("DOESN_EXIST", COURIER_SIZE, SWT.NORMAL) : new FontStyle("Courier New", COURIER_SIZE, SWT.NORMAL);

    private FontStyle fontStyle = null;

    public FontStyle getFontMonospaced() {

//      if (fontStyle == null) {
//         Font font = shell.getDisplay().getSystemFont();
//         FontData fd = font.getFontData()[0];
//         fontStyle = new FontStyle(fd.getName(), fd.getHeight(), fd.getStyle());
//      }

        return fontStyle;
    }

    public static class FontStyle {

        String name;
        int height;
        int style;

        FontStyle(final String name, final int height, final int style
        /* , boolean strikeout,boolean underline */) {
            this.name = name;
            this.height = height;
            this.style = style;

            // Shell shell = null;
            // shell.get
        }
    }

}
