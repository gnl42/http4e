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
package me.glindholm.plugin.http4e2.httpclient.core.util.shared;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.examples.javaviewer.JavaLineStyler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import me.glindholm.plugin.http4e2.httpclient.core.misc.Styles;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class ExportJavaViewer {

    private StyledText text;
    private final JavaLineStyler lineStyler = new JavaLineStyler();
    private static int HEIGHT = 650;
    private static int WEIGHT = 540;

    public static void main(final String[] args) {
        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setBounds(100, 100, 430, 575);
        shell.setLayout(new FillLayout());

        final ExportJavaViewer javaViewer = new ExportJavaViewer(shell);
        InputStream in;
        byte[] data = null;
        try {
            in = new FileInputStream("C:/_dev/workspace/Java5/src/enums/HttpStatusException.java");
            data = new byte[in.available()];
            in.read(data);
            final String javaSrc = new String(data, "UTF8");
            System.out.println("javaSrc=" + javaSrc);
            javaViewer.open(javaSrc);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

//   void createShell( Shell shell/*Display display*/){
////      shell = new Shell(display);
//      shell.setText("Window");
//      GridLayout layout = new GridLayout();
//      layout.numColumns = 1;
//      shell.setLayout(layout);
//      shell.addShellListener(new ShellAdapter() {
//         public void shellClosed( ShellEvent e){
//            lineStyler.disposeColors();
//            text.removeLineStyleListener(lineStyler);
//         }
//      });
//   }

    public ExportJavaViewer(final Composite parent) {
        createStyledText(parent);
    }

    void createStyledText(final Composite parent) {
        text = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
//      text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        final GridData spec = new GridData();
        spec.heightHint = HEIGHT;
        spec.widthHint = WEIGHT;
        spec.horizontalAlignment = GridData.FILL;
        spec.grabExcessHorizontalSpace = true;
        spec.verticalAlignment = GridData.FILL;
        spec.grabExcessVerticalSpace = true;
        text.setLayoutData(spec);
//        text.addLineStyleListener(lineStyler);
        text.setEditable(false);

        final Color bg = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
        text.setForeground(null);
        text.setBackground(null);
    }

//   void displayError( String msg, Shell shell){
//      MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
//      box.setMessage(msg);
//      box.open();
//   }

    public void open(final String sourceString) {
        // Guard against superfluous mouse move events -- defer action until
        // later
        final Display display = text.getDisplay();
//        display.asyncExec(() -> text.setText(sourceString));
        text.setText(sourceString);
        // parse the block comments up front since block comments can go across
        // lines - inefficient way of doing this
//        lineStyler.parseBlockComments(sourceString);
    }
}
