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
package org.roussev.http4e.httpclient.core.misc;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.roussev.http4e.editor.xml.ColorManager;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class ColorManagerAdaptor implements ColorManager {

    private final ResourceCache cache;

    public ColorManagerAdaptor(final ResourceCache cache) {
        this.cache = cache;
    }

    @Override
    public void dispose() {
        cache.disposeColors();
    }

    @Override
    public Color getColor(final RGB rgb) {
        return cache.getColor(rgb);
    }
}
