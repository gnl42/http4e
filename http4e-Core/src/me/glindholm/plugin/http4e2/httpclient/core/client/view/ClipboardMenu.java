/*
 *  Copyright 2017 Eclipse HttpClient (http4e) http://nextinterfaces.com
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
package me.glindholm.plugin.http4e2.httpclient.core.client.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
class ClipboardMenu {

    ClipboardMenu(final StyledText styledText, final Menu menu) {

        styledText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.stateMask == SWT.CTRL) {
                    if (e.character == 1) { // ^A
                        styledText.selectAll();
                    } else if (e.character == 3) { // ^C
                        styledText.copy();
                    } else if (e.character == 16) { // ^V
                        styledText.paste();
                    }
                }
            }
        });

        final MenuItem cutItem = new MenuItem(menu, SWT.PUSH);
        cutItem.setText("Cut \t Ctrl+X");
        cutItem.addListener(SWT.Selection, event -> styledText.cut());

        final MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
        copyItem.setText("Copy \t Ctrl+C");
        copyItem.addListener(SWT.Selection, event -> styledText.copy());
        final MenuItem pasteItem = new MenuItem(menu, SWT.PUSH);
        pasteItem.setText("Paste \t Ctrl+P");
        pasteItem.addListener(SWT.Selection, event -> styledText.paste());
    }

}
