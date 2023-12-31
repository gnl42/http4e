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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.AssistUtils;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.DocumentUtils;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.HConfiguration;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.HContentAssistProcessor;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.ModelTrackerListener;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
class ParamTextView implements IControlView {

    private final StyledText styledText;
    final ItemModel model;

    ParamTextView(final ItemModel iModel, final Composite parent) {
        model = iModel;
        styledText = buildEditorText(parent);

        final Menu popupMenu = new Menu(styledText);
        new ClipboardMenu(styledText, popupMenu);
        styledText.setMenu(popupMenu);

        styledText.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDoubleClick(final MouseEvent e) {
                model.fireExecute(new ModelEvent(ModelEvent.PARAMS_RESIZED, model));
            }
        });
        styledText.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(final FocusEvent e) {
                model.fireExecute(new ModelEvent(ModelEvent.PARAMS_FOCUS_GAINED, model));
            }

            @Override
            public void focusLost(final FocusEvent e) {
                model.fireExecute(new ModelEvent(ModelEvent.PARAMS_FOCUS_LOST, model));
            }
        });

        styledText.addKeyListener(new ExecuteKeyListener(() -> model.fireExecute(new ModelEvent(ModelEvent.REQUEST_START, model))));

    }

    private StyledText buildEditorText(final Composite parent) {
        final SourceViewer sourceViewer = new SourceViewer(parent, null, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
        final HConfiguration sourceConf = new HConfiguration(HContentAssistProcessor.PARAM_PROCESSOR);
        sourceViewer.configure(sourceConf);
        sourceViewer.setDocument(DocumentUtils.createDocument1());

        sourceViewer.getControl().addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent e) {
                // if ((e.character == ' ') && ((e.stateMask & SWT.CTRL) != 0)) {
                if (Utils.isAutoAssistInvoked(e)) {
                    final IContentAssistant ca = sourceConf.getContentAssistant(sourceViewer);
                    ca.showPossibleCompletions();
                }
            }
        });

        sourceViewer.addTextListener(e -> {
            final ModelTrackerListener trackerListener = (key, value) -> model.fireExecute(new ModelEvent(ModelEvent.PARAMS_FOCUS_LOST, model));
            AssistUtils.addTrackWords(e.getText(), sourceViewer.getDocument(), e.getOffset() - 1, trackerListener);
        });

        return sourceViewer.getTextWidget();
    }

    @Override
    public Control getControl() {
        return styledText;
    }
}
