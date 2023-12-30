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
package me.glindholm.plugin.http4e2.httpclient.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.ui.preferences.PreferenceConstants;

/**
 * Main plugin class.
 * 
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class HdPlugin extends AbstractUIPlugin implements UIConstants {

    // The shared instance
    private static HdPlugin plugin;
    private ResourceBundle resourceBundle;

    public static String getResourceString(final String key) {
        final ResourceBundle bundle = HdPlugin.getDefault().getResourceBundle();
        try {
            return bundle != null ? bundle.getString(key) : key;
        } catch (final MissingResourceException e) {
            return key;
        }
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * The constructor
     */
    public HdPlugin() {
        plugin = this;
        // init images
        // getImageRegistry();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        try {
            resourceBundle = ResourceBundle.getBundle(HdPlugin.class.getName());
        } catch (final MissingResourceException x) {
            resourceBundle = null;
        }
        CoreContext.getContext().putObject("p", HdPlugin.getDefault());

        final IPreferenceStore store = HdPlugin.getDefault().getPreferenceStore();
        int size = store.getInt(PreferenceConstants.P_PAYLOAD_VIEW_SIZE);
        if (size < 10) {
            size = 50;
            store.setValue(PreferenceConstants.P_PAYLOAD_VIEW_SIZE, size);
        }
        CoreContext.getContext().putObject(CoreObjects.RESPONSE_VIEW_SIZE, size);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        cleanupUserDirectoryPackets(".REQ.txt");
        cleanupUserDirectoryPackets(".RESP.txt");
        plugin = null;
        super.stop(context);
    }

    private static void cleanupUserDirectoryPackets(final String ext) {

        final FilenameFilter filter = (dir, name) -> name.endsWith(ext);
        final File dir = new File(CoreContext.PRODUCT_USER_DIR);
        final String[] files = dir.list(filter);

        final int len = files.length;
        for (int i = 0; i < len; i++) {
            final File f = new File(dir, files[i]);
            f.delete();
        }
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static HdPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path
     * 
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(final String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

}
