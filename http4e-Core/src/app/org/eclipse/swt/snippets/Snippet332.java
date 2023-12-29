/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.snippets;

/*
 * SWT StyledText snippet: using BidiSegmentEvent#segmentsChars to implement custom bidi segments
 * 
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 * 
 * @since 3.6
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Snippet332 {

    public static void main(final String[] args) {
        final Display display = new Display();
        final Shell shell = new Shell(display);
        final FillLayout layout = new FillLayout();
        layout.marginHeight = layout.marginWidth = 10;
        shell.setLayout(layout);
        final StyledText text = new StyledText(shell, SWT.MULTI | SWT.BORDER);
        final String segment = "Eclipse";
        final String string = "Force RTL direction on this segment \"" + segment + "\".";
        text.setText(string);
        final int[] segments = { string.indexOf(segment), segment.length() };
        final StyleRange[] ranges = { new StyleRange(0, 0, display.getSystemColor(SWT.COLOR_RED), null) };
        text.setStyleRanges(segments, ranges);
        text.setFont(new Font(display, "Tahoma", 16, 0));
        text.addBidiSegmentListener(event -> {
            final String string1 = event.lineText;
            final int start = string1.indexOf(segment);
            event.segments = new int[] { start, start + segment.length() };
//            event.data = new char[] {'\u202e', '\u202C'};
        });
        shell.setSize(500, 200);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}