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
package me.glindholm.plugin.http4e2.httpclient.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
@SuppressWarnings("unchecked")
public class CoreContext {

    public final static String PRODUCT_USER_DIR = System.getProperty("user.home") + File.separator + ".http4e";

    private static CoreContext context;
    static {
        context = new CoreContext();
    }

    public static CoreContext getContext() {
        return context;
    }

    private final Map<Object, Object> objectMap = new HashMap<>();

    public Object getObject(final Object key) {
        return objectMap.get(key);
    }

    public void putObject(final Object key, final Object value) {
        objectMap.put(key, value);
    }

    @Override
    public String toString() {
        return "CoreContext{" + "" + objectMap + "}";
    }

    public static Shell SHELL;

}
