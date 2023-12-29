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
package org.roussev.http4e.httpclient.ui.actions;

import org.eclipse.jface.action.Action;
import org.roussev.http4e.httpclient.core.client.view.FolderView;
import org.roussev.http4e.httpclient.ui.HdViewPart;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class AddTabAction extends Action {

    private final HdViewPart view;

    public AddTabAction(final HdViewPart view) {
        this.view = view;
        setText("Open a new tab");
        setDescription("Open a new tab");
        setToolTipText("Open a new tab");
    }

    @Override
    public void run() {
        final FolderView folderView = view.getFolderView();
        folderView.addTab();
    }

}
