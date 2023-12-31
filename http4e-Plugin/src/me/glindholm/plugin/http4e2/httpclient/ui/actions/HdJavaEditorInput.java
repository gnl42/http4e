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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.internal.ide.AboutInfo;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class HdJavaEditorInput implements IEditorInput {

    private final AboutInfo aboutInfo;

    private final static String FACTORY_ID = "org.eclipse.ui.internal.dialogs.WelcomeEditorInputFactory";

    public final static String FEATURE_ID = "featureId";

    /**
     * WelcomeEditorInput constructor comment.
     */
    public HdJavaEditorInput(final AboutInfo info) {
        if (info == null) {
            throw new IllegalArgumentException();
        }
        aboutInfo = info;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public Object getAdapter(final Class adapter) {
        return null;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public String getName() {
        return IDEWorkbenchMessages.WelcomeEditor_title;
    }

    @Override
    public IPersistableElement getPersistable() {
        return new IPersistableElement() {
            @Override
            public String getFactoryId() {
                return FACTORY_ID;
            }

            @Override
            public void saveState(final IMemento memento) {
                memento.putString(FEATURE_ID, aboutInfo.getFeatureId() + ':' + aboutInfo.getVersionId());
            }
        };
    }

    public AboutInfo getAboutInfo() {
        return aboutInfo;
    }

    @Override
    public boolean equals(final Object o) {
        if (o != null && o instanceof HdJavaEditorInput) {
            if (((HdJavaEditorInput) o).aboutInfo.getFeatureId().equals(aboutInfo.getFeatureId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getToolTipText() {
        return NLS.bind(IDEWorkbenchMessages.WelcomeEditor_toolTip, aboutInfo.getFeatureLabel());
    }
}