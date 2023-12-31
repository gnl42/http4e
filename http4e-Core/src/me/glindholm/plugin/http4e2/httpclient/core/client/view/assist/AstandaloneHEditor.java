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
package me.glindholm.plugin.http4e2.httpclient.core.client.view.assist;

import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.misc.Styles;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class AstandaloneHEditor {

    private SourceViewer sourceViewer;

    public AstandaloneHEditor(final Composite parent) {
        buildControls(parent);
    }

    public void dispose() {
        ResourceUtils.disposeResources();
    }

    private void buildControls(final Composite parent) {
        parent.setLayout(new FillLayout());

        sourceViewer = new SourceViewer(parent, null, SWT.MULTI | SWT.V_SCROLL);

        final HConfiguration sourceConf = new HConfiguration(HContentAssistProcessor.HEADER_PROCESSOR);
        sourceViewer.configure(sourceConf);
        sourceViewer.setDocument(DocumentUtils.createDocument1());

        // final IContentAssistant assistant = getContentAssistant(null);
        // assistant.install(textViewer);

        final StyledText st = sourceViewer.getTextWidget();

        sourceViewer.getControl().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.character == ' ' && (e.stateMask & SWT.CTRL) != 0) {
                    final IContentAssistant ca = sourceConf.getContentAssistant(sourceViewer);
                    ca.showPossibleCompletions();

                    st.setBackground(ResourceUtils.getColor(Styles.SSL));
                }
                st.setBackground(ResourceUtils.getColor(Styles.BACKGROUND_ENABLED));
            }
        });

        sourceViewer.addTextListener(e -> AssistUtils.addTrackWords(e.getText(), sourceViewer.getDocument(), e.getOffset() - 1, null));

    }

    public static void main(final String[] args) {

        final String ROOT_CORE = "C:/.work/http4e/me.glindholm.plugin.http4e2/Core";
        final String ROOT_UI = "C:/.work/http4e/me.glindholm.plugin.http4e2/Plugin";

        CoreContext.getContext().putObject(CoreObjects.ROOT_PATH_CORE, ROOT_CORE);
        CoreContext.getContext().putObject(CoreObjects.ROOT_PATH_UI, ROOT_UI);
        CoreContext.getContext().putObject(CoreObjects.IS_STANDALONE, "nonull");

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setBounds(700, 350, 500, 350);
        // shell.setLayout(new FillLayout());
        final AstandaloneHEditor view = new AstandaloneHEditor(shell);

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        view.dispose();
        display.dispose();
    }
}
