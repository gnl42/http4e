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

import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.CoreMessages;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.AssistConstants;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.AssistUtils;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.DocumentUtils;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.HConfiguration;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.HContentAssistProcessor;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.ModelTrackerListener;
import me.glindholm.plugin.http4e2.httpclient.core.misc.Styles;
import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
class HeaderView {

    StyledText headersText;
    final ItemModel model;

    HeaderView(final ItemModel iModel, final Composite parent) {
        model = iModel;
        final ViewForm vForm = toolbarHeader("Headers", model, parent);
        headersText = buildEditorText(vForm);
        vForm.setContent(headersText);

        headersText.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDoubleClick(final MouseEvent e) {
                model.fireExecute(new ModelEvent(ModelEvent.HEADERS_RESIZED, model));
            }
        });

        headersText.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(final FocusEvent e) {
                model.fireExecute(new ModelEvent(ModelEvent.HEADERS_FOCUS_GAINED, model));
            }

            @Override
            public void focusLost(final FocusEvent e) {
                model.fireExecute(new ModelEvent(ModelEvent.HEADERS_FOCUS_LOST, model));
            }
        });

        // /////////////////////////////////
        final Menu popupMenu = new Menu(headersText);
        new ClipboardMenu(headersText, popupMenu);
        headersText.setMenu(popupMenu);
        setText(CoreConstants.CONTENT_TYPE_X_WWW_FORM);

        headersText.addKeyListener(new ExecuteKeyListener(() -> model.fireExecute(new ModelEvent(ModelEvent.REQUEST_START, model))));

        // new MenuItem(popupMenu, SWT.SEPARATOR);
        //
        // MenuItem ctItem = new MenuItem(popupMenu, SWT.CASCADE);
        // ctItem.setText("Content-Type");
        // Menu contypeMenu = new Menu(ctItem);
        // ctItem.setMenu(contypeMenu);
        //
        // MenuItem hItem = new MenuItem(popupMenu, SWT.CASCADE);
        // hItem.setText("Add Header");
        //
        // Menu headersMenu = new Menu(hItem);
        // hItem.setMenu(headersMenu);
        // MenuItem h1 = new MenuItem(headersMenu, SWT.PUSH);
        // h1.setText("Accept");

        // Listener copyListener = new Listener() {
        // public void handleEvent( Event event){
        // if (event.character == '\u0003') {
        // Clipboard clipboard = new Clipboard(Display.getDefault());
        // TextTransfer transfer = TextTransfer.getInstance();
        // String text = (String) clipboard.getContents(transfer);
        // System.out.println("clipboard contents: " + text);
        // clipboard.dispose();
        // }
        // }
        // };
        // headersText.addListener(SWT.KeyDown, copyListener);
        // /////////////////////////////////
    }

    private StyledText buildEditorText(final Composite parent) {
        final SourceViewer sourceViewer = new SourceViewer(parent, null, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);

        final HConfiguration sourceConf = new HConfiguration(HContentAssistProcessor.HEADER_PROCESSOR);
        sourceViewer.configure(sourceConf);
        sourceViewer.setDocument(DocumentUtils.createDocument1());

        sourceViewer.getControl().addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent e) {
                if (Utils.isAutoAssistInvoked(e)) {
                    final IContentAssistant ca = sourceConf.getContentAssistant(sourceViewer);
                    ca.showPossibleCompletions();
                }
            }
        });

        sourceViewer.addTextListener(e -> {
            final ModelTrackerListener trackerListener = (key, value) -> {
                key = BaseUtils.noNull(key).trim();
                if (key.equalsIgnoreCase(AssistConstants.HEADER_CONTENT_TYPE)) {
                    model.fireExecute(new ModelEvent(ModelEvent.CONTENT_TYPE_CHANGE, model));
                }
            };
            AssistUtils.addTrackWords(e.getText(), sourceViewer.getDocument(), e.getOffset() - 1, trackerListener);
        });

        return sourceViewer.getTextWidget();
    }

    private ViewForm toolbarHeader(final String title, final ItemModel model, final Composite parent) {
        final ViewForm vForm = ViewUtils.buildViewForm(title, model, parent);

        final ToolBar bar = new ToolBar(vForm, SWT.FLAT);

        final ToolItem clearBtn = new ToolItem(bar, SWT.PUSH);
        clearBtn.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.DELETE));
        clearBtn.setToolTipText("Clear");
        clearBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }

            @Override
            public void widgetSelected(final SelectionEvent e) {
                HeaderView.this.setText("");
            }
        });
        vForm.setTopCenter(bar);

        return vForm;
    }

    String getHeaderText() {
        if (CoreMessages.HEADER_DEFAULTS.equals(headersText.getText())) {
            return CoreConstants.EMPTY_TEXT;
        }
        return headersText.getText();
    }

    String getText() {
        return headersText.getText();
    }

    void setText(final String txt) {
        headersText.setText(txt);
    }

    void setForeground(final RGB style) {
        Color fg = ResourceUtils.getColor(Styles.GRAY_RGB_TEXT);
        Color bg = ResourceUtils.getColor(Styles.BACKGROUND_DISABLED);
        fg= bg = null;

        headersText.setForeground(fg);
    }

}
