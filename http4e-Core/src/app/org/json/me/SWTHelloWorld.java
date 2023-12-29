package org.json.me;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SWTHelloWorld {

    public static void main(final String[] args) {
        final Display display = new Display();
        final Shell shell = new Shell(display);

//      Text helloWorldTest = new Text(shell, SWT.NONE);
//      helloWorldTest.setText("Hello World SWT");
//      helloWorldTest.pack();

        final Ch5CompletionEditor ch5CompletionEditor = new Ch5CompletionEditor(shell);

        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}