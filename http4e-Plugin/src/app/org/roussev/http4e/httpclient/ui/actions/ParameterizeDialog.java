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
package org.roussev.http4e.httpclient.ui.actions;

import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ViewPart;
import org.roussev.http4e.httpclient.core.CoreConstants;
import org.roussev.http4e.httpclient.core.CoreContext;
import org.roussev.http4e.httpclient.core.CoreImages;
import org.roussev.http4e.httpclient.core.CoreObjects;
import org.roussev.http4e.httpclient.core.client.view.ParameterizeTextView;
import org.roussev.http4e.httpclient.core.util.BaseUtils;
import org.roussev.http4e.httpclient.core.util.ResourceUtils;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class ParameterizeDialog extends TitleAreaDialog {

    private StyledText text;
    private static int HEIGHT = 400;
    private static int WEIGHT = 300;
    private MouseListener okListener;

    public ParameterizeDialog(final ViewPart view) {
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
        setTitle("Parameterize HTTP call");
        setMessage("Parameterize your [Headers] and [Params] with arguments from this dialog");
        return contents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        // Composite composite = (Composite) super.createDialogArea(parent);
        // text = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL |
        // SWT.H_SCROLL);
        // GridData spec = new GridData();
        // spec.heightHint = HEIGHT;
        // spec.widthHint = WEIGHT;
        // spec.horizontalAlignment = GridData.FILL;
        // spec.grabExcessHorizontalSpace = true;
        // spec.verticalAlignment = GridData.FILL;
        // spec.grabExcessVerticalSpace = true;
        // text.setLayoutData(spec);
        // // text.setForeground(ResourceUtils.getColor(GRAY_RGB_TEXT));
        // text.setText("#Typing \"myArg=xyz\" here " +
        // "\n# and \"myArg={?}\" or \"myArg={myArg}\" at Header/Param area " +
        // "\n# the latter will get automatically injected with \"myArg\" value.");

        // final boolean[] isClicked = {false};
        // text.addMouseListener(new MouseListener(){
        //
        // public void mouseDoubleClick( MouseEvent e){
        // // TODO Auto-generated method stub
        //
        // }
        //
        // public void mouseDown( MouseEvent e){
        // if(!isClicked[0]){
        // text.setText("");
        // text.setForeground(ResourceUtils.getColor(GRAY_NORMAL_TEXT));
        // }
        // isClicked[0] = true;
        // }
        //
        // public void mouseUp( MouseEvent e){
        // }
        // });

        final ParameterizeTextView pView = new ParameterizeTextView(parent);
        text = (StyledText) pView.getControl();

        final GridData spec = new GridData();
        spec.heightHint = HEIGHT;
        spec.widthHint = WEIGHT;
        spec.horizontalAlignment = GridData.FILL;
        spec.grabExcessHorizontalSpace = true;
        spec.verticalAlignment = GridData.FILL;
        spec.grabExcessVerticalSpace = true;
        text.setLayoutData(spec);

        final CoreContext ctx = CoreContext.getContext();
        final Map<String, String> parArgs = (Map<String, String>) ctx.getObject(CoreObjects.PARAMETERIZE_ARGS);
        if (parArgs == null || parArgs.isEmpty()) {
            text.setText("""
                    #############################################################
                    #       Providing "@something=123" here\s
                    #
                    #       and "header1=@something" at Headers panel\s
                    #
                    #       or "param1=@something" at Params panel\s
                    #
                    #       the latter will be substituted with the "@something" value.
                    #############################################################
                    """);
        } else {
            final StringBuilder sb = new StringBuilder();
            for (final String key : parArgs.keySet()) {
                sb.append(key + "=" + BaseUtils.noNull(parArgs.get(key)) + "\n");
            }
            text.setText(sb.toString());
        }

        return text;
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        final Button ok = createButton(parent, IDialogConstants.OK_ID, "Close", true);
        // createButton(parent, IDialogConstants.CANCEL_ID,
        // IDialogConstants.CANCEL_LABEL, false);
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
