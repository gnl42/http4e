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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
class ViewUtils {

    static ViewForm buildViewForm(final String title, final ItemModel model, final Composite parent) {
        final ViewForm vForm = new ViewForm(parent, SWT.NONE);

        // -- Label(vForm)
        final CLabel label = new CLabel(vForm, SWT.NONE);
        label.setText(CoreConstants.TITLE_SPACE + title + CoreConstants.TITLE_SPACE);
        label.setAlignment(SWT.LEFT);
        label.setBackground(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.TITLE_LINE));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(final MouseEvent e) {
                int eventType = ModelEvent.UNKNOWN;
                if (CoreConstants.TITLE_HEADERS.equals(title)) {
                    eventType = ModelEvent.HEADERS_RESIZED;

                } else if (CoreConstants.TITLE_PARAMETERS.equals(title)) {
                    eventType = ModelEvent.PARAMS_RESIZED;

                } else if (CoreConstants.TITLE_BODY.equals(title)) {
                    eventType = ModelEvent.BODY_RESIZED;

                } else if (CoreConstants.TITLE_REQUEST.equals(title)) {
                    eventType = ModelEvent.REQUEST_RESIZED;

                } else if (CoreConstants.TITLE_RESPONSE.equals(title)) {
                    eventType = ModelEvent.RESPONSE_RESIZED;
                }
                model.fireExecute(new ModelEvent(eventType, model));
            }
        });

        vForm.setTopLeft(label);

        return vForm;
    }

}
