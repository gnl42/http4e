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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class HAssistInfoMap {

    private final Properties data;

    public HAssistInfoMap(final String file) {
        data = new Properties();
        try {
            final InputStream in = ResourceUtils.getBundleResourceStream2(CoreConstants.PLUGIN_CORE, file);
            if (in != null) {
                data.load(in);
            }
        } catch (final IOException ignore) {
            ExceptionHandler.warn(ignore);
        }
    }

    public String getInfo(final String key) {
        final String val = data.getProperty(key);
        return val;
    }

    @Override
    public String toString() {
        return "HAssistInfoMap:" + data;
    }

}
