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
package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;
import me.glindholm.plugin.http4e2.httpclient.ui.HdPlugin;
import me.glindholm.plugin.http4e2.httpclient.ui.preferences.PreferenceConstants;

public class ConfigDialog extends TitleAreaDialog {

    private final int DEFAULT_SIZE = 100;
    private int size = DEFAULT_SIZE;

    private Text sizeBox;

    public ConfigDialog(final ViewPart view) {
        super(view.getViewSite().getShell());
        setTitleImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.LOGO_DIALOG));
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Control contents = super.createContents(parent);
        setTitle("HTTP4e2 Preferences");
        // setMessage("BASIC and DIGEST Authentication.");

        return contents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE | SWT.BORDER);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Composite gr = createControlGroup(composite);

        return gr;
    }

    private Composite createControlGroup(final Composite parent) {

        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout layout1 = new GridLayout();
        layout1.numColumns = 1;
        composite.setLayout(layout1);

        // ------------
        createControlWidgets(parent);

        populate();
        return parent;
    }

    private void createControlWidgets(final Composite controlGroup) {

        final Group group = new Group(controlGroup, SWT.NONE);
        // group.setText("HTTP Response viewable size");
        final GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginHeight = 10;
        layout.marginWidth = 20;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final GridData grData = new GridData(GridData.FILL_HORIZONTAL);
        grData.widthHint = 160;

        new Label(group, SWT.NONE).setText("HTTP Response MAX size");
        sizeBox = new Text(group, SWT.BORDER);
        sizeBox.setLayoutData(grData);

        sizeBox.addModifyListener(e -> {
            try {
                size = Integer.parseInt(sizeBox.getText());
            } catch (final Exception e2) {
                // ignore
            }
        });
        new Label(group, SWT.NONE).setText("KB");
    }

    public void populate() {
        final CoreContext ctx = CoreContext.getContext();
        try {
            final IPreferenceStore store = HdPlugin.getDefault().getPreferenceStore();
            size = store.getInt(PreferenceConstants.P_PAYLOAD_VIEW_SIZE);
        } catch (final Exception e) {
            size = DEFAULT_SIZE;
        }
        ctx.putObject(CoreObjects.RESPONSE_VIEW_SIZE, size);
        sizeBox.setText("" + size);
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        final Button okBtn = createButton(parent, IDialogConstants.OK_ID, "Save", true);
        okBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                CoreContext.getContext().putObject(CoreObjects.RESPONSE_VIEW_SIZE, size);
                final IPreferenceStore store = HdPlugin.getDefault().getPreferenceStore();
                if (size >= 10) {
                    store.setValue(PreferenceConstants.P_PAYLOAD_VIEW_SIZE, size);
                    // System.out.println(store.getInt(PreferenceConstants.P_PAYLOAD_VIEW_SIZE));
                }
                // fireExecuteFolderItems();
            }
        });

        final Button cancelBtn = createButton(parent, IDialogConstants.OK_ID, "Cancel", true);
        cancelBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                // fireExecuteFolderItems();
            }
        });

    }

}
