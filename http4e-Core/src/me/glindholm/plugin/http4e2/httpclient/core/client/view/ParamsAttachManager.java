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
package me.glindholm.plugin.http4e2.httpclient.core.client.view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.Model;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
class ParamsAttachManager {

    // private final Menu menu;
    // private final ToolItem addBody;
    private final ToolBar toolBar;
    private final ToolItem i_open;
    private boolean isMultipart = false;

    // private final StyledText swtText;
    // private final MenuItem m_attachPart;

    ParamsAttachManager(final ItemModel model, final StyledText styledText, final ToolBar toolbar) {
        // this.swtText = swtText;
        toolBar = toolbar;

        final ParamsOpen open = new ParamsOpen(this, model, styledText);

        i_open = new ToolItem(toolBar, SWT.PUSH);
        i_open.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.FILE_OPEN));
        i_open.setDisabledImage(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.FILE_OPEN_DIS));
        i_open.setToolTipText("Add File");
        i_open.addSelectionListener(open);
    }

    public void setEnabled(final boolean enabled) {
        i_open.setEnabled(enabled);
    }

    public boolean isMultipart() {
        return isMultipart;
    }

    public void setMultipart(final boolean isMultipart) {
        this.isMultipart = isMultipart;
    }

}

class ParamsOpen implements SelectionListener {

    private final StyledText st;
    private final Model model;
    private final ParamsAttachManager manager;

    public ParamsOpen(final ParamsAttachManager manager, final ItemModel model, final StyledText st) {
        this.st = st;
        this.model = model;
        this.manager = manager;
    }

    @Override
    public void widgetSelected(final SelectionEvent event) {
        final FileDialog fd = new FileDialog(st.getShell(), SWT.OPEN);
        fd.setText("Add File Parameter");
        fd.setFilterExtensions(CoreConstants.FILE_FILTER_EXT);
        final String file = fd.open();

        if (file != null) {
            if (manager.isMultipart()) {
                st.setText(st.getText() + CoreConstants.FILE_PREFIX + file);
            } else {
                try {
                    st.setText(readFileAsString(file));
                } catch (final IOException e) {
                    // ignore
                }
            }
            // model.fireExecute(new ModelEvent(ModelEvent.BODY_FOCUS_LOST,
            // model));
            // // force body to refresh itself
            // model.fireExecute(new ModelEvent(ModelEvent.PARAMS_FOCUS_LOST,
            // model));
        }
    }

    private static String readFileAsString(final String filePath) throws java.io.IOException {
        final StringBuilder fileData = new StringBuilder(1000);
        final BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            final String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }

    @Override
    public void widgetDefaultSelected(final SelectionEvent event) {
    }

}
