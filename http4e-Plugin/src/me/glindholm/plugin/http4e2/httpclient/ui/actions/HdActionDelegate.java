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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import me.glindholm.plugin.http4e2.httpclient.core.client.view.FolderView;
import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;

/**
 * Action delegate for all toolbar push-buttons.
 * 
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class HdActionDelegate implements IViewActionDelegate {

    public HdViewPart hdPart = null;

    @Override
    public void init(final IViewPart viewPart) {
        if (viewPart instanceof HdViewPart) {
            hdPart = (HdViewPart) viewPart;
        }
    }

    @Override
    public void run(final IAction action) {
        final String id = action.getId();
        final FolderView folderView = hdPart.getFolderView();

        if (id.equals("add.tab")) {
            folderView.addTab();

        } else if (id.equals("remove.tab")) {
            folderView.removeTab();
        }
    }

    /**
     * Not implemented.
     */
    @Override
    public void selectionChanged(final IAction action, final ISelection selection) {
    }

}
