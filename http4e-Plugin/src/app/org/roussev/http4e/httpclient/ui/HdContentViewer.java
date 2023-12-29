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
package org.roussev.http4e.httpclient.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.roussev.http4e.httpclient.core.client.model.FolderModel;
import org.roussev.http4e.httpclient.core.client.view.FolderView;
import org.roussev.http4e.httpclient.ui.preferences.PreferenceConstants;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class HdContentViewer extends ContentViewer {

    private final FolderView folderView;

    public HdContentViewer(final Composite parent) {
        final IPreferenceStore store = HdPlugin.getDefault().getPreferenceStore();
        final String proxyList = store.getString(PreferenceConstants.P_PROXY_LIST);
        final String keystoreFile = store.getString(PreferenceConstants.P_KEYSTORE_LIST);
        folderView = new FolderView(parent, new FolderModel(proxyList, keystoreFile));
    }

    public FolderView getFolderView() {
        return folderView;
    }

    /**
     * @Override
     */
    @Override
    public Control getControl() {
        return folderView.getControl();
    }

    /**
     * @Override
     */
    @Override
    public ISelection getSelection() {
        return null;
    }

    /**
     * @Override
     */
    @Override
    public void refresh() {
    }

    /**
     * @Override
     */
    @Override
    public void setSelection(final ISelection selection, final boolean arg1) {
    }

    public void doDispose() {
        folderView.doDispose();
    }

}
