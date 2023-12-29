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
package org.roussev.http4e.httpclient.core.client.view;

import java.io.IOException;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolItem;
import org.roussev.http4e.crypt.HexUtils;
import org.roussev.http4e.httpclient.core.CoreConstants;
import org.roussev.http4e.httpclient.core.misc.CoreException;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
class HexManager {

    private String text;
    private final StyledText swtText;
    private final ToolItem swtItem;
    private boolean isHex = false;

    HexManager(final StyledText swtText, final ToolItem swtItem) {
        this.swtText = swtText;
        this.swtItem = swtItem;
        this.swtItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (!isHex) {
                    asHex();
                    isHex = true;
                } else {
                    asString();
                    isHex = false;
                }
            }
        });
    }

    public void rebind() {
        isHex = false;
        swtItem.setSelection(false);
        text = swtText.getText();
    }

    private void asHex() {
        text = swtText.getText();
        try {
            swtText.setText(HexUtils.toHex(text.getBytes(CoreConstants.UTF8)));
        } catch (final IOException e) {
            throw CoreException.getInstance(CoreException.UNSUPPORTED_ENCODING, e);
        }
    }

    private void asString() {
        swtText.setText(text);
    }

    public void setText(final String txt) {
        swtText.setText(txt);
        rebind();
    }

    public String getText() {
        return swtText.getText();
    }

}
