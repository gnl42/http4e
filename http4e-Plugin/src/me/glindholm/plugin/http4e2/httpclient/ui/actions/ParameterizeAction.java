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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.FolderView;
import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class ParameterizeAction extends Action {

    private final HdViewPart view;
    private ParameterizeDialog dialog;

    public ParameterizeAction(final HdViewPart view) {
        this.view = view;
        setText("Parameterize your call");
        setDescription("Parameterize your call");
        setToolTipText("Parameterize your call");
    }

    // public void dispose(){
    // // action is reused, can be called several times.
    // if (fMenu != null) {
    // fMenu.dispose();
    // fMenu = null;
    // }
    // }

    @Override
    public void run() {
        if (dialog == null) {
            dialog = new ParameterizeDialog(view);
        }
        try {
            final MouseAdapter okListener = new MouseAdapter() {

                @Override
                public void mouseUp(final MouseEvent e) {
                    final String dialogText = dialog.getText();
                    final Properties props = new Properties();

                    try {
                        final InputStream is = new ByteArrayInputStream(dialogText.getBytes("UTF-8"));
                        props.load(is);
                    } catch (final IOException e1) {
                        ExceptionHandler.handle(e1);
                    }

                    final CoreContext ctx = CoreContext.getContext();
                    ctx.putObject(CoreObjects.PARAMETERIZE_ARGS, props);
                    final FolderView folderView = view.getFolderView();
                    folderView.getModel().fireExecute(new ModelEvent(ModelEvent.PARAMETERIZE_CHANGE, CoreConstants.NULL_MODEL));
                    // ItemModel iModel = new ItemModel(folderView.getModel(), item);
                    // HdViewPart hdViewPart = (HdViewPart) view;
                    // hdViewPart.getFolderView().buildTab(iModel);
                }
            };
            dialog.setOkListener(okListener);
            dialog.open();

        } catch (final Exception e) {
            ExceptionHandler.handle(e);
        }
    }

}
