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

import org.eclipse.jface.action.Action;

import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;

public class AuthenticationAction extends Action {

    private final HdViewPart view;

    public AuthenticationAction(final HdViewPart view) {
        this.view = view;
        setText("BASIC and DIGEST Authentication");
        setDescription("BASIC and DIGEST Authentication");
        setToolTipText("BASIC and DIGEST Authentication");
    }

    @Override
    public void run() {
        try {
            final AuthDialog dialog = new AuthDialog(view);
            dialog.open();
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
        }
    }

}
