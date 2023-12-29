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
package org.roussev.http4e.httpclient.core.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roussev.http4e.httpclient.core.CoreConstants;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class ItemModel implements Model {

    private static final long serialVersionUID = -6572164711472818414L;

    /**
     * List<ModelListener> listeners
     */
    private final List<ModelListener> listeners = new ArrayList<>();

    private final FolderModel folderModel;
    private Item item;
    private String[] payloadFiles;

    public String[] getPayloadFiles() {
        return payloadFiles;
    }

    public void setPayloadFiles(final String[] payloadFiles) {
        this.payloadFiles = payloadFiles;
    }

    public ItemModel(final FolderModel folderModel) {
        this(folderModel, new Item());
    }

    public ItemModel(final FolderModel folderModel, final Item item) {
        this.folderModel = folderModel;
        this.item = item;
        this.item.availableProxies = folderModel.getAvailableProxies();
        this.item.availableKeystore = folderModel.getAvailableKeystore();
    }

    @Override
    public ItemModel clone() {
        final ItemModel clone = new ItemModel(folderModel);

        final Map<String, List<String>> headers = getHeaders();
        for (final String h : headers.keySet()) {
            final List<String> hVals = headers.get(h);
            for (final String val : hVals) {
                clone.addHeader(h, val);
            }
        }

        final Map<String, List<String>> params = getParameters();
        for (final String h : params.keySet()) {
            final List<String> hVals = params.get(h);
            for (final String val : hVals) {
                clone.addParameter(h, val);
            }
        }

        clone.setAuth(getAuth());
        clone.setAvailableKeystore(getAvailableKeystore());
        clone.setAvailableProxies(getAvailableProxies()); // TODO
        clone.setBody(getBody());
        clone.setCurrentProxy(getCurrentProxy());
        clone.setHSashWeights(getHSashWeights());
        clone.setHttpMethod(getHttpMethod());
        clone.setName(getName());
        clone.setProxy(getProxy());
        clone.setRequest(getRequest());
        clone.setResponse(getResponse());
        clone.setUrl(getUrl());
        clone.setVSashWeights(getVSashWeights());

        return clone;
    }

    public void init(final int hashcode) {
        item.hashcode = hashcode;
    }

    @Override
    public void addListener(final ModelListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(final ModelListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void fireExecute(final ModelEvent e) {
        for (final ModelListener listener : listeners) {
            listener.executed(e);
        }
    }

    public FolderModel getParent() {
        return folderModel;
    }

    /**
     * Map<String, List<String>> getHeaders
     */
    public Map<String, List<String>> getHeaders() {
        return item.headers;
    }

    public List<String> getHeaderValuesIgnoreCase(final String headerName) {
        for (final Object headKeyObj : getHeaders().keySet()) {
            final String headKey = (String) headKeyObj;
            if (headerName.equalsIgnoreCase(headKey)) {
                return getHeaders().get(headKey);
            }
        }
        return CoreConstants.BLANK_LIST;
    }

    public void clearHeaders() {
        item.headers.clear();
    }

    public void addHeader(final String headerName, final String headerValue) {
        final String ct = "Content-Type";
        String h = headerName;
        if (ct.equalsIgnoreCase(headerName)) {
            h = ct;
        }
        List<String> headColl = item.headers.get(h);
        if (headColl == null) {
            headColl = new ArrayList<>();
        }
        headColl.add(headerValue);
        item.headers.put(h, headColl);
    }

    public String getHttpMethod() {
        return item.httpMethod;
    }

    public void setHttpMethod(final String httpMethod) {
        item.httpMethod = httpMethod;
    }

    public String getName() {
        return item.name;
    }

    public void setName(final String name) {
        item.name = name;
    }

    /**
     * Map<String, List<String>> getParameters()
     */
    public Map<String, List<String>> getParameters() {
        return item.parameters;
    }

    public void clearParameters() {
        item.parameters.clear();
    }

    public void addParameter(final String paramName, final String paramValue) {
        List<String> paramColl = item.parameters.get(paramName);
        if (paramColl == null) {
            paramColl = new ArrayList<>();
        }
        paramColl.add(paramValue);
        item.parameters.put(paramName, paramColl);
    }

    public String getUrl() {
        return item.url;
    }

    public void setUrl(final String url) {
        item.url = url;
    }

    public ProxyItem getProxy() {
        return item.proxyItem;
    }

    public AuthItem getAuth() {
        return item.authItem;
    }

    public void setProxy(final ProxyItem proxyItem) {
        item.proxyItem = proxyItem;
    }

    public void setAuth(final AuthItem authItem) {
        item.authItem = authItem;
    }

    public ProxyItem getCurrentProxy() {
        return item.currentProxy;
    }

    public void setCurrentProxy(final ProxyItem proxy) {
        item.currentProxy = proxy;
    }

    public List<ProxyItem> getAvailableProxies() {
        return item.availableProxies;
    }

    public void setAvailableProxies(final List<ProxyItem> availableProxies) {
        item.availableProxies = availableProxies;
    }

    public String getAvailableKeystore() {
        return item.availableKeystore;
    }

    public void setAvailableKeystore(final String availableKeystore) {
        item.availableKeystore = availableKeystore;
    }

    public String getResponse() {
        return item.response;
    }

    public void setResponse(final String response) {
        item.response = response;
    }

    public String getRequest() {
        return item.request;
    }

    public void setRequest(final String request) {
        item.request = request;
    }

    @Override
    public Serializable getSerializable() {
        return item;
    }

    @Override
    public void load(final Serializable serializable) {
        item = (Item) serializable;
    }

    public String getBody() {
        return item.body;
    }

    public void setBody(final String body) {
        item.body = body;
    }

    public int[] getHSashWeights() {
        if (item.hSashWeights != null) {
            return item.hSashWeights;
        }
        return new int[] { CoreConstants.H_SASH_EQ, CoreConstants.H_SASH_EQ, CoreConstants.H_SASH_EQ };
    }

    public void setHSashWeights(final int[] weights) {
        item.hSashWeights = weights;
    }

    public int[] getVSashWeights() {
        if (item.vSashWeights != null) {
            return item.vSashWeights;
        }
        return new int[] { CoreConstants.V_SASH_EQ, CoreConstants.V_SASH_EQ, CoreConstants.V_SASH_EQ };
    }

    public void setVSashWeights(final int[] weights) {
        item.vSashWeights = weights;
    }

    public void setParameteredArgs(final Map<String, String> parameteredArgs) {
        item.parameteredArgs = parameteredArgs;
    }

    public Map<String, String> getParameteredArgs() {
        return item.parameteredArgs;
    }

    public boolean isEmpty() {
        return CoreConstants.EMPTY_TEXT.equals(item.name);
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj instanceof final ItemModel im) {
            return im.hashCode() == hashCode();
        }
        return false;
    }

    @Override
    public String toString() {
        return "\n\tItemModel" + item + "";
    }

}
