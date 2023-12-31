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
package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ViewPart;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class ImportDialog extends TitleAreaDialog {

    private StyledText text;
    private static int HEIGHT = 400;
    private static int WEIGHT = 300;
    private MouseListener okListener;
    private static final RGB GRAY_NORMAL_TEXT = new RGB(15, 15, 15);
    private static final RGB GRAY_RGB_TEXT = new RGB(180, 180, 180);

    public ImportDialog(final ViewPart view) {
        super(view.getViewSite().getShell());
        setTitleImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.LOGO_DIALOG));
    }

    public void setOkListener(final MouseListener okListener) {
        this.okListener = okListener;
    }

    @Override
    public boolean close() {
        return super.close();
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Control contents = super.createContents(parent);
        setTitle("Import HTTP packet");
        setMessage("Import a raw HTTP packet to Client.");
        return contents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        text = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        final GridData spec = new GridData();
        spec.heightHint = HEIGHT;
        spec.widthHint = WEIGHT;
        spec.horizontalAlignment = GridData.FILL;
        spec.grabExcessHorizontalSpace = true;
        spec.verticalAlignment = GridData.FILL;
        spec.grabExcessVerticalSpace = true;
        text.setLayoutData(spec);
        text.setForeground(ResourceUtils.getColor(GRAY_RGB_TEXT));
        text.setText(
                "POST /user/some HTTP/1.1\nContent-Type: application/xml\nUser-Agent: http4e/3.1.5\nHost: www.nextinterfaces.com\nContent-Length: 11\n\nsample body long data..");

        final boolean[] isClicked = { false };
        text.addMouseListener(new MouseListener() {

            @Override
            public void mouseDoubleClick(final MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseDown(final MouseEvent e) {
                if (!isClicked[0]) {
                    text.setText("");
                    text.setForeground(ResourceUtils.getColor(GRAY_NORMAL_TEXT));
                }
                isClicked[0] = true;
            }

            @Override
            public void mouseUp(final MouseEvent e) {
            }
        });
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        final Button ok = createButton(parent, IDialogConstants.OK_ID, "Import Packet", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        ok.addMouseListener(okListener);
        ok.addTraverseListener(e -> {
            if (SWT.TRAVERSE_RETURN == e.detail) {
            }
        });
    }

    public String getText() {
        return text.getText();
    }

}
