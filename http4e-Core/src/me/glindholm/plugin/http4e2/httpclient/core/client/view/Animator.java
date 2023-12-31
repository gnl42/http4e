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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Control;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.misc.CoreException;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
class Animator {

    private AnimatedGIFRunner gifRunner;
    private final Control parent;
    private final Image backgroundImage;

    Animator(final Control parent, final Image backgroundImage) {
        this.parent = parent;
        this.backgroundImage = backgroundImage;
    }

    void start() {
        if (gifRunner != null) {
            gifRunner.stop();
        }
        try {
            final ImageLoader imageLoader = new ImageLoader();
            imageLoader.load(ResourceUtils.getBundleResourceStream(CoreConstants.PLUGIN_CORE, CoreImages.LOADING_ON));
            gifRunner = new AnimatedGIFRunner(parent, imageLoader, backgroundImage);
        } catch (final Exception e) {
            throw CoreException.getInstance(CoreException.GENERAL, e);
        }
        final Thread animeThread = new Thread(gifRunner);
        animeThread.setDaemon(true);
        animeThread.start();
    }

    void stop() {
        if (gifRunner != null) {
            gifRunner.stop();
        }
    }

}
