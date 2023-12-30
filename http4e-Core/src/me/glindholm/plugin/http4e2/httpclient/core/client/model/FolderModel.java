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
package me.glindholm.plugin.http4e2.httpclient.core.client.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.text.StrTokenizer;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.misc.CoreException;
import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class FolderModel implements Model {

    private static final long serialVersionUID = -6512224765472818414L;
    public static final String PREF_VIEW_NAME = "v";

    private final Map<Integer, ItemModel> itemMap = new HashMap<>();
    private final List<ItemModel> itemList = new ArrayList<>();
    private final List<ModelListener> modelListeners = new ArrayList<>();
    private final Set<String> urlHistory = new HashSet<>();
    private final List<ProxyItem> availableProxies = new ArrayList<>();
    private String availableKeystore = null;

    public FolderModel(final String availableProxiesString, final String availableKeystoresString) {
        availableProxies.add(ProxyItem.createDirectConnectionProxy());
        for (final String proxysettings : new StrTokenizer(availableProxiesString, "#").getTokenArray()) {
            try {
                availableProxies.add(ProxyItem.createFromString(proxysettings));
            } catch (final Exception e) {
                // TODO handle error
            }
        }
        availableKeystore = availableKeystoresString;
    }

    @SuppressWarnings("unchecked")
    public List<ItemModel> deserialize(final byte[] data) {
        final List<ItemModel> imList = new ArrayList<>();
        removeAll();
        List<ItemModel> serList;
        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(data);
            final ObjectInputStream ois = new ObjectInputStream(bais);
            serList = (List<ItemModel>) ois.readObject();
            final int size = serList.size();
            for (int i = size - 1; i > -1; i--) {
                final Serializable ser = (Serializable) serList.get(i);
                final ItemModel im = new ItemModel(this);
                im.load(ser);
                if (!im.isEmpty()) {
                    imList.add(im);
                }
            }
            ois.close();

        } catch (final EOFException | FileNotFoundException | ClassNotFoundException ignore) {
        } catch (final IOException e) {
            ExceptionHandler.warn("deserialize: " + e);// warning may be ..
        }

        if (imList.size() < 1) {
            // System.out.println("FolderModel: No items deserialized. Creating one
            // ..");
            final ItemModel im = new ItemModel(this);
            imList.add(im);
        }

        final int cnt = 0;
        // Add available Proxies to ItemModels
        for (final ItemModel itemModel : imList) {
            itemModel.setAvailableProxies(availableProxies);
            itemModel.setAvailableKeystore(availableKeystore);
            if (cnt == 0) { // FIXME. Those values belong to FolderModel, not
                            // ItemModel
                final CoreContext ctx = CoreContext.getContext();
                ctx.putObject(CoreObjects.AUTH_ITEM, itemModel.getAuth());
                ctx.putObject(CoreObjects.PROXY_ITEM, itemModel.getProxy());
                ctx.putObject(CoreObjects.PARAMETERIZE_ARGS, itemModel.getParameteredArgs());
            }
        }

        return imList;
    }

    public byte[] serialize() {
        try {
            // FileOutputStream fout = new
            // FileOutputStream(CoreConstants.SERIALIZED_PATH);
            // ObjectOutputStream oos = new ObjectOutputStream(fout);
            final List<Serializable> serList = new ArrayList<>();

            final CoreContext ctx = CoreContext.getContext();
            final Map<String, String> mapParmzArgs = (Map<String, String>) ctx.getObject(CoreObjects.PARAMETERIZE_ARGS);

            for (final ItemModel im : itemList) {
                im.setParameteredArgs(mapParmzArgs);
                im.fireExecute(new ModelEvent(ModelEvent.FOLDER_INIT, CoreConstants.NULL_MODEL));
                serList.add(im.getSerializable());
            }
            // oos.writeObject(serList);
            // oos.close();

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos2 = new ObjectOutputStream(baos);
            oos2.writeObject(serList);
            oos2.close();

            return baos.toByteArray();

        } catch (final Exception e) {
            throw new CoreException(CoreException.GENERAL, e);
        }
    }

    @Override
    public void addListener(final ModelListener listener) {
        modelListeners.add(listener);
    }

    @Override
    public void removeListener(final ModelListener listener) {
        modelListeners.remove(listener);
    }

    @Override
    public void fireExecute(final ModelEvent e) {
        for (final ModelListener listener : modelListeners) {
            listener.executed(e);
        }
    }

    public void addUrlToHistory(final String url) {
        urlHistory.add(url);
    }

    public String[] getUrlHistory() {
        final String[] urls = new String[urlHistory.size()];
        int i = 0;
        for (final Iterator<String> iter = urlHistory.iterator(); iter.hasNext(); i++) {
            urls[i] = iter.next();
        }
        return urls;
    }

    public synchronized void putItem(final ItemModel itemModel) {
        itemMap.put(itemModel.hashCode(), itemModel);
        itemList.add(itemModel);
    }

    /**
     * Using the overloaded remove(int) instead of remove(Object)
     */
    public synchronized void removeItem(final Integer hashcode) {
        itemList.remove(getItemModel(hashcode));
        itemMap.remove(hashcode);
    }

    public synchronized void removeAll() {
        itemMap.clear();
        itemList.clear();
    }

    public List<ProxyItem> getAvailableProxies() {
        return availableProxies;
    }

    public String getAvailableKeystore() {
        return availableKeystore;
    }

    public synchronized int getItemCount() {
        return itemMap.size();
    }

    public synchronized ItemModel getItemModel(final Integer id) {
        return itemMap.get(id);
    }

    public List<ItemModel> getItemModels() {
        return itemList;
    }

    @Override
    public Serializable getSerializable() {
        throw new RuntimeException("Method not implemented");
    }

    @Override
    public void load(final Serializable data) {
        throw new RuntimeException("Method not implemented");
    }

    public void doDispose() {

        BaseUtils.writeToPrefs(PREF_VIEW_NAME, serialize());
        for (final ItemModel im : itemList) {
            im.fireExecute(new ModelEvent(ModelEvent.ITEM_DISPOSE, CoreConstants.NULL_MODEL));
        }
        removeAll();
    }

    public void doAuth() {
        for (final ItemModel im : itemList) {
            im.fireExecute(new ModelEvent(ModelEvent.AUTH, CoreConstants.NULL_MODEL));
        }
    }

    public void doProxy() {
        for (final ItemModel im : itemList) {
            im.fireExecute(new ModelEvent(ModelEvent.PROXY, CoreConstants.NULL_MODEL));
        }
    }

    @Override
    public String toString() {
        return "FolderModel{" + "items=" + itemMap + "}";
    }

}
