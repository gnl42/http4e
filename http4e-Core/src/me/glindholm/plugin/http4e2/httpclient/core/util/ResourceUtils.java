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
package me.glindholm.plugin.http4e2.httpclient.core.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.Utils;
import me.glindholm.plugin.http4e2.httpclient.core.misc.ResourceCache;
import me.glindholm.plugin.http4e2.httpclient.core.misc.Styles.FontStyle;

/**
 * A resource(Images, Fonts, etc) utilities class.
 *
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class ResourceUtils {

    private static ResourceCache resourceCache;

    static {
        resourceCache = (ResourceCache) CoreContext.getContext().getObject(CoreObjects.RESOURCE_CACHE);
        if (resourceCache == null) {
            resourceCache = new ResourceCache();
            CoreContext.getContext().putObject(CoreObjects.RESOURCE_CACHE, resourceCache);
        }
    }

    public static Image getImage(final String pluginID, final String image) {
        /*--- create from class file location
        ImageDescriptor iDescr = ImageDescriptor.createFromFile(CoreResources.class, name);
        Image image = resourceCache.getImage(iDescr);
        return image;
        --- get workbench shared image
        IWorkbench workbench = PlatformUI.getWorkbench();
        ISharedImages images = workbench.getSharedImages();
        image = images.getImage(ISharedImages.IMG_OBJ_FOLDER);
        --- image from plugin
        MyPlugin.getImageDescriptor("icons/a_image.gif").createImage();
        AbstractUIPlugin.imageDescriptorFromPlugin(myPluginID, image)).createImage();
         */
        if (Utils.isIDE()) {
            return resourceCache.getImage(AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, image));
        } else {
            return resourceCache.getImage(image);
        }
    }

    public static void disposeResources() {
        resourceCache.dispose();
    }

    public static Font getFont(final FontStyle fontStyle) {
        return resourceCache.getFont(fontStyle);
    }

    public static Color getColor(final RGB rgb) {
        return resourceCache.getColor(rgb);
    }

    public static Cursor getCursor(final int id) {
        return resourceCache.getCursor(id);
    }

    public static ResourceCache getResourceCache() {
        return resourceCache;
    }

    public static String getBundleResourcePath(final String pluginID, final String uri) {
        try {
            final URL url = FileLocator.find(Platform.getBundle(pluginID), new Path(uri), null);
            return FileLocator.resolve(url).toExternalForm();
        } catch (final IOException e1) {
            ExceptionHandler.handle(e1);
        }
        return null;
    }

    public static byte[] getBundleResourceBytes(final String pluginID, final String uri) {
        byte[] data = {};
        try {
            final InputStream in = getBundleResourceStream(pluginID, uri);
            data = new byte[in.available()];
            in.read(data);
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
        }
        return data;
    }

    public static InputStream getBundleResourceStream(final String pluginID, final String uri) {
        try {
            if (Utils.isIDE()) {
                return FileLocator.openStream(Platform.getBundle(pluginID), new Path(uri), false);
            } else {
                return new FileInputStream(uri);
            }
        } catch (final Throwable e) {
            ExceptionHandler.handle(e);
        }
        return null;
    }

    public static InputStream getBundleResourceStream2(final String pluginID, final String uri) throws IOException {
        if (Utils.isIDE()) {
            return FileLocator.openStream(Platform.getBundle(pluginID), new Path(uri), false);
        } else {
            return new FileInputStream(uri);
        }
    }

    public static Properties getBundleProperties(final String pluginID, final String uri) {
        final Properties properties = new Properties();
        try {
            final InputStreamReader inR = new InputStreamReader(getBundleResourceStream(pluginID, uri), "UTF8");
            final BufferedReader bufR = new BufferedReader(inR);
            properties.load(bufR);
        } catch (final Throwable e) {
            ExceptionHandler.handle(e);
        }
        return properties;
    }

}
