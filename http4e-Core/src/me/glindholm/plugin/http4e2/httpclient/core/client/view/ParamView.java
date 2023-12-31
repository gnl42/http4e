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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.CoreMessages;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.misc.Styles;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
class ParamView {

    private final IControlView textView;
    private final CLabel titleLabel;
    private final ParamsAttachManager attachManager;
    private final boolean isMultipart = false;

    ParamView(final ItemModel model, final Composite parent) {
        final ViewForm vForm = ViewUtils.buildViewForm(CoreConstants.TITLE_PARAMETERS, model, parent);
        textView = new ParamTextView(model, vForm);
        titleLabel = (CLabel) vForm.getChildren()[0];
        final ToolBar bar = new ToolBar(vForm, SWT.FLAT);
        vForm.setContent(textView.getControl());
        final ToolItem clearBtn = new ToolItem(bar, SWT.PUSH);
        clearBtn.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.DELETE));
        clearBtn.setToolTipText("Clear");
        clearBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }

            @Override
            public void widgetSelected(final SelectionEvent e) {
                ParamView.this.setParamText("");
            }
        });
        attachManager = new ParamsAttachManager(model, (StyledText) textView.getControl(), bar);
        vForm.setContent(textView.getControl());
        vForm.setTopCenter(bar);
    }

    String getParamText() {
        final StyledText st = (StyledText) textView.getControl();
        if (CoreMessages.PARAM_DEFAULTS.equals(st.getText())) {
            return CoreConstants.EMPTY_TEXT;
        }
        final String txt = st.getText();

        return txt;
    }

    void setParamText(final String txt) {
        final StyledText st = (StyledText) textView.getControl();
        if (CoreConstants.EMPTY_TEXT.equals(txt)) {
            st.setText(CoreMessages.PARAM_DEFAULTS);
        } else {
            st.setText(txt);
        }
        if (CoreConstants.EMPTY_TEXT.equals(getParamText())) {
            st.setForeground(ResourceUtils.getColor(Styles.LIGHT_RGB_TEXT));
        } else {
            st.setForeground(ResourceUtils.getColor(Styles.DARK_RGB_TEXT));
        }
    }

    void setFocus(final boolean focusGained) {
        final StyledText st = (StyledText) textView.getControl();
        if (focusGained) {
            if (CoreConstants.EMPTY_TEXT.equals(getParamText())) {
                st.setText(CoreConstants.EMPTY_TEXT);
            }
            st.setForeground(ResourceUtils.getColor(Styles.DARK_RGB_TEXT));
        }
    }

    void setEditable(final boolean editable) {
        final StyledText st = (StyledText) textView.getControl();
        Color fg = ResourceUtils.getColor(Styles.GRAY_RGB_TEXT);
        Color bg = ResourceUtils.getColor(Styles.BACKGROUND_DISABLED);
        fg= bg = null;
        if (editable) {
            // bodyText.setFont(ResourceUtils.getFont(Styles.FONT_COURIER));
            st.setForeground(fg);
            st.setBackground(bg);
            st.setEditable(true);
            attachManager.setEnabled(true);

        } else {
            // bodyText.setFont(ResourceUtils.getFont(Styles.FONT_COURIER));
            st.setForeground(fg);
            st.setBackground(ResourceUtils.getColor(Styles.GREY_DISABLED));
            st.setEditable(false);
            attachManager.setEnabled(false);
        }
    }

    CLabel getTitleLabel() {
        return titleLabel;
    }

    boolean isEditable() {
        final StyledText st = (StyledText) textView.getControl();
        return st.getEditable();
    }

    public void setMultipart(final boolean isMultipart) {
        attachManager.setMultipart(isMultipart);
    }

    void setBackground(final Color color) {
        final StyledText st = (StyledText) textView.getControl();
        st.setBackground(color);
    }

}
