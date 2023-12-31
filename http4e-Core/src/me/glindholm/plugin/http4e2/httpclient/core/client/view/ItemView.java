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
package me.glindholm.plugin.http4e2.httpclient.core.client.view;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.CoreMessages;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.client.misc.BusinessJob;
import me.glindholm.plugin.http4e2.httpclient.core.client.misc.EclipseHttpJob;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.AuthItem;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelListener;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ProxyItem;
import me.glindholm.plugin.http4e2.httpclient.core.misc.Styles;
import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.JunkUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.ParseUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

/**
 * Tab Item GUI
 *
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
@SuppressWarnings({ "unchecked", "serial" })
class ItemView implements ModelListener, Serializable {

    private static final RGB GRAY_RGB_TEXT = new RGB(180, 180, 180);

    private final ItemModel model;
    final CTabItem tabItem;
    Text tabeNameText;
    Combo httpCombo;
    Combo urlCombo;
    HeaderView headerView;
    ParamView paramView;
    BodyView bodyView;
    RequestView requestView;
    ResponseView responseView;
    SashForm hSash;
    SashForm vSash;

    private Animator loadingAnimator;
    private ToolItem stopItem;
    private Label lblAuth;
    private Label lblProxy;

    private final ItemState state = new ItemState();
    private final BusinessJob buzJob = new BusinessJob();

    Job httpJob;
    private boolean maximizedHeaders = false;
    private boolean maximizedParams = false;
    private boolean maximizedBody = false;
    private boolean maximizedRequest = false;
    private boolean maximizedResponse = false;
    private Combo proxyCombo;

    public ItemView(final CTabItem tabItem, final ItemModel model) {
        this.model = model;
        model.addListener(this);
        this.tabItem = tabItem;
        tabItem.setControl(getItemControl(tabItem.getParent()));
        colorizeIfSSL();
    }

    private void colorizeIfSSL() {
        if (Utils.isHttpS(urlCombo.getText())) {
            urlCombo.setBackground(ResourceUtils.getColor(Styles.SSL));
        } else {
            urlCombo.setBackground(ResourceUtils.getColor(Styles.HTTP_RGB_TEXT));
        }
    }

    public String getTabName() {
        final String txt = tabeNameText.getText().trim();
        if (CoreMessages.EMPTY_TITLE_NAME.equals(txt)) {
            return null;
        }
        return txt;
    }

    /**
     * Callback method
     */
    @Override
    public void executed(final ModelEvent e) {

        if (e.getType() == ModelEvent.REQUEST_START) {
            if (state.getState() == ItemState.HTTP_STARTED) {
                return;
            }
            colorizeIfSSL();

            Utils.viewToModel(model, this);
            tabItem.setText(ParseUtils.toTitle(model.getName()));
            urlCombo.setText(model.getUrl());

            requestView.setHttpText(CoreConstants.EMPTY_TEXT);
            responseView.setHttpText(CoreConstants.EMPTY_TEXT);
            loadingAnimator.start();
            stopItem.setEnabled(true);
            state.setState(ItemState.HTTP_STARTED);
            if (Utils.isIDE()) {
                httpJob = new EclipseHttpJob(3000, true, true);
            }

            // starting asyncr thread
            buzJob.execute(ItemView.this.model, vSash);

            if (tabItem.isDisposed()) {
                return;
            }
            if (!isPost()) {
                final String textParams = ParseUtils.doUrlToParam2(urlCombo.getText(), ParseUtils.linesToMap2(paramView.getParamText()), true);
                paramView.setParamText(textParams);
            }

            urlCombo.add(urlCombo.getText());
            model.getParent().addUrlToHistory(urlCombo.getText());

        } else if (e.getType() == ModelEvent.REQUEST_APPENDED) {
            requestView.appendHttpText(((ItemModel) e.getModel()).getRequest());

        } else if (e.getType() == ModelEvent.PAYLOAD_FILES) {
            final String[] files = ((ItemModel) e.getModel()).getPayloadFiles();
            requestView.setPayloadFilename(files[0]);
            responseView.setPayloadFilename(files[1]);

        } else if (e.getType() == ModelEvent.REQUEST_STOPPED) {
            if (state.getState() != ItemState.HTTP_STOPPED) {
                responseView.setHttpText(((ItemModel) e.getModel()).getResponse());
                loadingAnimator.stop();
                stopItem.setEnabled(false);
                state.setState(ItemState.HTTP_STOPPED);
                if (httpJob != null) {
                    httpJob.cancel();
                }
            }

        } else if (e.getType() == ModelEvent.REQUEST_ABORTED) {
            if (state.getState() != ItemState.HTTP_ABORTED) {
                responseView.setHttpText(CoreMessages.ABORTED);
                loadingAnimator.stop();
                stopItem.setEnabled(false);
                state.setState(ItemState.HTTP_ABORTED);
                if (httpJob != null) {
                    httpJob.cancel();
                }
            }

        } else if (e.getType() == ModelEvent.FOLDER_INIT) {
            Utils.viewToModel(model, this);

        } else if (e.getType() == ModelEvent.ITEM_DISPOSE) {
            doDispose();

        } else if (e.getType() == ModelEvent.HEADERS_FOCUS_GAINED) {
            if (CoreConstants.EMPTY_TEXT.equals(headerView.getHeaderText())) {
                headerView.setForeground(Styles.BLACK_RGB_TEXT);
                headerView.setText(CoreConstants.EMPTY_TEXT);
            }

        } else if (e.getType() == ModelEvent.HEADERS_FOCUS_LOST) {
            if (CoreConstants.EMPTY_TEXT.equals(headerView.getText())) {
                headerView.setForeground(Styles.LIGHT_RGB_TEXT);
                headerView.setText(CoreMessages.HEADER_DEFAULTS);
            }
            model.fireExecute(new ModelEvent(ModelEvent.CONTENT_TYPE_CHANGE, model));

        } else if (e.getType() == ModelEvent.PARAMS_FOCUS_GAINED) {
            paramView.setFocus(true);

        } else if (e.getType() == ModelEvent.PARAMS_FOCUS_LOST) {
            final List<String> translated = ParseUtils.paramToUrlAndBody(paramView.getParamText());
            if (isParamsEditable()) {
                if (isPost()) {
                    if (!bodyView.getText().startsWith(CoreConstants.FILE_PREFIX)) {
                        bodyView.setText(translated.get(1));
                    }
                } else {
                    urlCombo.setText(ParseUtils.appendParamsToUrl(urlCombo.getText(), translated.get(0)));
                }
            }

        } else if (e.getType() == ModelEvent.URL_FOCUS_LOST) {
            if (!isPost()) {
                final String textParams = ParseUtils.doUrlToParam2(urlCombo.getText(), ParseUtils.linesToMap2(paramView.getParamText()), true);
                paramView.setParamText(textParams);
            }

        } else if (e.getType() == ModelEvent.CONTENT_TYPE_CHANGE) {
            paramView.setMultipart(false);
            if (isPost()) {
                if (isXwwwType()) {
                    // change from nonedit-to-edit , pull data from body
                    if (!paramView.isEditable()) {
                        paramView.setParamText(ParseUtils.bodyToParam2(bodyView.getText()));
                    }

                    paramView.setEditable(true);
                    bodyView.setEditable(true, true, true);
                    paramView.setBackground(ResourceUtils.getColor(Styles.BACKGROUND_FORM_TYPE));
                    bodyView.setBackground(ResourceUtils.getColor(Styles.BACKGROUND_FORM_TYPE));
                    paramView.getTitleLabel().setText(CoreConstants.TITLE_PARAMETERS_X_WWW);
                    bodyView.getTitleLabel().setText(CoreConstants.TITLE_BODY_X_WWW);

                } else if (isMultipartType()) {
                    paramView.setMultipart(true);
                    if (!paramView.isEditable()) {
                        paramView.setParamText(ParseUtils.bodyToParam2(bodyView.getText()));
                    }
                    bodyView.setText("");

                    paramView.setEditable(true);
                    bodyView.setEditable(false, false, false);
                    paramView.setBackground(ResourceUtils.getColor(Styles.BACKGROUND_FORM_TYPE));
                    bodyView.setBackground(ResourceUtils.getColor(Styles.GREY_DISABLED));
                    paramView.getTitleLabel().setText(CoreConstants.TITLE_PARAMETERS_MULTIPART);
                    bodyView.getTitleLabel().setText(CoreConstants.TITLE_BODY);

                } else {
                    // paramView.setParamText(CoreConstants.EMPTY_TEXT);
                    paramView.setBackground(ResourceUtils.getColor(Styles.BACKGROUND_ENABLED));
                    bodyView.setBackground(ResourceUtils.getColor(Styles.BACKGROUND_ENABLED));
                    paramView.getTitleLabel().setText(CoreConstants.TITLE_PARAMETERS);
                    bodyView.getTitleLabel().setText(CoreConstants.TITLE_BODY);

                    paramView.setEditable(false);
                    bodyView.setEditable(true, false, true);
                }
            } else {
                paramView.setEditable(true);
                bodyView.setEditable(isPut(), false/*
                                                    * doesn't really matter as long as it is not post
                                                    */, false);
                paramView.getTitleLabel().setText(CoreConstants.TITLE_PARAMETERS);
                bodyView.getTitleLabel().setText(CoreConstants.TITLE_BODY);
            }

        } else if (e.getType() == ModelEvent.HTTP_METHOD_CHANGE) {
            if (isPost()) {
                bodyView.setEditable(true, isXwwwType(), true);
            } else if (isPut()) {
                bodyView.setEditable(true, isXwwwType(), false);
            } else {
                bodyView.setEditable(false, false, false);
            }
            if (state.getState() == ItemState.POST_ENABLED) {
                if (isXwwwType()) {
                    bodyView.setText(ParseUtils.toUrlParams(paramView.getParamText(), false));
                }
                urlCombo.setText(ParseUtils.appendParamsToUrl(urlCombo.getText(), CoreConstants.EMPTY_TEXT));

            } else if (state.getState() == ItemState.POST_DISABLED) {
                // bodyView.setText(CoreConstants.EMPTY_TEXT);
                final String translatedParams = ParseUtils.paramToUrlAndBody(paramView.getParamText()).get(0);
                urlCombo.setText(ParseUtils.appendParamsToUrl(urlCombo.getText(), translatedParams));
            }
            model.fireExecute(new ModelEvent(ModelEvent.CONTENT_TYPE_CHANGE, model));

        } else if (e.getType() == ModelEvent.BODY_FOCUS_LOST) {
            if (isParamsEditable() && !isPut()) {
                paramView.setParamText(ParseUtils.bodyToParam2(bodyView.getText()));
            }

        } else if (e.getType() == ModelEvent.HEADERS_RESIZED) {
            if (maximizedHeaders) {
                hSash.setWeights(CoreConstants.H_SASH_EQ, CoreConstants.H_SASH_EQ, CoreConstants.H_SASH_EQ);
                maximizedHeaders = false;
            } else {
                hSash.setWeights(CoreConstants.H_SASH_MAX, CoreConstants.H_SASH_MIN, CoreConstants.H_SASH_MIN);
                maximizedHeaders = true;
                maximizedParams = false;
                maximizedBody = false;
            }

        } else if (e.getType() == ModelEvent.PARAMS_RESIZED) {
            if (maximizedParams) {
                hSash.setWeights(CoreConstants.H_SASH_EQ, CoreConstants.H_SASH_EQ, CoreConstants.H_SASH_EQ);
                maximizedParams = false;
            } else {
                hSash.setWeights(CoreConstants.H_SASH_MIN, CoreConstants.H_SASH_MAX, CoreConstants.H_SASH_MIN);
                maximizedHeaders = false;
                maximizedParams = true;
                maximizedBody = false;
            }

        } else if (e.getType() == ModelEvent.BODY_RESIZED) {
            if (maximizedBody) {
                hSash.setWeights(CoreConstants.H_SASH_EQ, CoreConstants.H_SASH_EQ, CoreConstants.H_SASH_EQ);
                maximizedBody = false;
            } else {
                hSash.setWeights(CoreConstants.H_SASH_MIN, CoreConstants.H_SASH_MIN, CoreConstants.H_SASH_MAX);
                maximizedHeaders = false;
                maximizedParams = false;
                maximizedBody = true;
            }

        } else if (e.getType() == ModelEvent.REQUEST_RESIZED) {
            if (maximizedRequest) {
                vSash.setWeights(CoreConstants.V_SASH_EQ, CoreConstants.V_SASH_EQ, CoreConstants.V_SASH_EQ);
                maximizedRequest = false;
            } else {
                vSash.setWeights(CoreConstants.V_SASH_EQ, CoreConstants.V_SASH_MAX, CoreConstants.V_SASH_MIN);
                maximizedRequest = true;
                maximizedResponse = false;
            }

        } else if (e.getType() == ModelEvent.RESPONSE_RESIZED) {
            if (maximizedResponse) {
                vSash.setWeights(CoreConstants.V_SASH_EQ, CoreConstants.V_SASH_EQ, CoreConstants.V_SASH_EQ);
                maximizedResponse = false;
            } else {
                vSash.setWeights(CoreConstants.V_SASH_EQ, CoreConstants.V_SASH_MIN, CoreConstants.V_SASH_MAX);
                maximizedRequest = false;
                maximizedResponse = true;
            }

        } else if (e.getType() == ModelEvent.EXPORT) {
            Utils.viewToModel(model, this);

        } else if (e.getType() == ModelEvent.AUTH) {
            final CoreContext ctx = CoreContext.getContext();
            final AuthItem au = (AuthItem) ctx.getObject(CoreObjects.AUTH_ITEM);
            if (au != null && (au.isBasic() || au.isDigest())) {
                lblAuth.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.AUTH_ENABLED));
            } else {
                lblAuth.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.AUTH_TAB));
            }
        } else if (e.getType() == ModelEvent.PROXY) {
            final CoreContext ctx = CoreContext.getContext();
            final ProxyItem pr = (ProxyItem) ctx.getObject(CoreObjects.PROXY_ITEM);
            if (pr != null && pr.isProxy()) {
                lblProxy.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.PROXY_ENABLED));
            } else {
                lblProxy.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.PROXY_TAB));
            }
        } else {
//         System.err.println("Unknown event: " + e);
        }
    }

    /**
     * GUI's
     */
    private Control getItemControl(final CTabFolder folder) {
        final Layout layout = new GridLayout();
        ((GridLayout) layout).numColumns = 1;

        final Composite itemComposite = new Composite(folder, SWT.NULL);
        itemComposite.setLayout(layout);

        buildTopControl(itemComposite);
        buildBottomControl(itemComposite);

        return itemComposite;
    }

    private Control buildTopControl(final Composite parent) {

        final GridLayout layout = new GridLayout();
        layout.numColumns = 8;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 3;
        layout.verticalSpacing = 0;

        final Composite top = new Composite(parent, SWT.NONE);
        top.setLayout(layout);
        final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        top.setLayoutData(gridData);

        initHttpCombo(top);
        initUrlCombo(top);
        initUrlButtons(top);
        initProxyCombo(top);
//      initHttpCombo(top);
        initAuthProxyBtns(top);
        initTabeNameText(top);

        return top;
    }

    private void initAuthProxyBtns(final Composite parent) {
        lblAuth = new Label(parent, SWT.NONE);
        lblAuth.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.AUTH_TAB));
        lblAuth.setToolTipText("Using BASIC, DIGEST Authentication");

        lblProxy = new Label(parent, SWT.NONE);
        lblProxy.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.PROXY_TAB));
        lblProxy.setToolTipText("Using Proxy Connection");
    }

    private Control buildBottomControl(final Composite parent) {
        final Composite bottom = new Composite(parent, SWT.NULL);
        final FillLayout layout = new FillLayout();
        bottom.setLayout(layout);
        bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

        vSash = new SashForm(bottom, SWT.HORIZONTAL);
        vSash.setLayout(layout);

        final Composite leftBottom = new Composite(vSash, SWT.NULL);
        leftBottom.setLayout(new FillLayout(SWT.VERTICAL));
        hSash = new SashForm(leftBottom, SWT.VERTICAL);
        hSash.setLayout(layout);

        requestView = new RequestView(model, vSash);
        responseView = new ResponseView(model, vSash);

        vSash.setWeights(model.getVSashWeights());

        headerView = new HeaderView(model, hSash);
        paramView = new ParamView(model, hSash);
        bodyView = new BodyView(model, hSash);

        hSash.setWeights(model.getHSashWeights());

        return bottom;
    }

    private void initUrlButtons(final Composite parent) {
        final ViewForm vForm = new ViewForm(parent, SWT.NONE);
        final ToolBar bar = new ToolBar(vForm, SWT.FLAT);

        final ToolItem startItem = new ToolItem(bar, SWT.PUSH);
        stopItem = new ToolItem(bar, SWT.PUSH);
        stopItem.setEnabled(false);

        startItem.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.GO));
        startItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent event) {
                model.fireExecute(new ModelEvent(ModelEvent.PARAMS_FOCUS_LOST, model));
                model.fireExecute(new ModelEvent(ModelEvent.REQUEST_START, model));
            }
        });

        stopItem.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.PROGRESS_STOP_ON));
        stopItem.setDisabledImage(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.PROGRESS_STOP_OFF));
        stopItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent event) {
                buzJob.abort(true);
            }
        });

        final Image backgroundImage = ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.LOADING_OFF);
        final CLabel ctrl = new CLabel(parent, SWT.NONE);
        ctrl.setImage(backgroundImage);
        loadingAnimator = new Animator(ctrl, backgroundImage);

        vForm.setTopCenter(bar);
    }

    /**
     * <p>
     * Create Combo for Proxies, when there is at least one real proxy configured.
     * </p>
     *
     * @param top
     */
    private void initProxyCombo(final Composite top) {
        // show combo only, if there is at least one proxy configured
        if (model.getAvailableProxies().size() > 1) {
            proxyCombo = new Combo(top, SWT.READ_ONLY);
            final Map<String, ProxyItem> proxyItems = new HashMap<>();

            for (final ProxyItem proxy : model.getAvailableProxies()) {
                proxyCombo.add(proxy.getName());
                proxyItems.put(proxy.getName(), proxy);
            }
            proxyCombo.select(proxyCombo.indexOf(model.getCurrentProxy().getName()));
            proxyCombo.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetDefaultSelected(final SelectionEvent e) {
                    final String selectedText = proxyCombo.getText();
                    if (proxyItems.containsKey(selectedText)) {
                        model.setCurrentProxy(proxyItems.get(selectedText));
                    }
                }

                @Override
                public void widgetSelected(final SelectionEvent e) {
                    final String selectedText = proxyCombo.getText();
                    if (proxyItems.containsKey(selectedText)) {
                        model.setCurrentProxy(proxyItems.get(selectedText));
                    }

                }

            });
        }
    }

    private void initUrlCombo(final Composite top) {

        urlCombo = new Combo(top, SWT.DROP_DOWN);
        urlCombo.setText(model.getUrl());
        urlCombo.setLayoutData(new GridData(GridData.FILL_BOTH));

        urlCombo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
                final String[] items = urlCombo.getItems();
                if (items != null && !Arrays.asList(items).contains(urlCombo.getText())) {
                    urlCombo.add(urlCombo.getText());
                }
            }

            @Override
            public void widgetSelected(final SelectionEvent e) {
            }
        });
        urlCombo.addTraverseListener(e -> {
            if (SWT.TRAVERSE_RETURN == e.detail) {
                model.fireExecute(new ModelEvent(ModelEvent.PARAMS_FOCUS_LOST, model));
                model.fireExecute(new ModelEvent(ModelEvent.REQUEST_START, model));
            }
        });

        urlCombo.addFocusListener(new FocusListener() {

            String prevUrl = urlCombo.getText();

            @Override
            public void focusGained(final FocusEvent e) {
                // setParentFocus(true);
            }

            @Override
            public void focusLost(final FocusEvent e) {
                // setParentFocus(false);
                if (!prevUrl.equals(urlCombo.getText())) {
                    urlCombo.setText(BaseUtils.noNull(urlCombo.getText()).trim());
                    prevUrl = urlCombo.getText();
                    model.fireExecute(new ModelEvent(ModelEvent.URL_FOCUS_LOST, model));
                }
            }
        });
    }

    private void initHttpCombo(final Composite top) {
        httpCombo = new Combo(top, SWT.READ_ONLY);
        httpCombo.setItems(CoreConstants.HTTP11_METHODS);
        httpCombo.setText(model.getHttpMethod());
        httpCombo.addSelectionListener(new SelectionAdapter() {

            private String prevMethod = model.getHttpMethod();

            @Override
            public void widgetSelected(final SelectionEvent e) {
                // becomes GET, HEAD, PUT, etc
                if (CoreConstants.HTTP_POST.equals(prevMethod) && !CoreConstants.HTTP_POST.equals(httpCombo.getText())) {
                    state.setState(ItemState.POST_DISABLED);
                    // becomes POST
                } else if (!CoreConstants.HTTP_POST.equals(prevMethod) && CoreConstants.HTTP_POST.equals(httpCombo.getText())) {
                    state.setState(ItemState.POST_ENABLED);
                    // no update
                } else {
                    state.setState(ItemState.POST_NO_UPDATE);
                }
                prevMethod = httpCombo.getText();
                model.fireExecute(new ModelEvent(ModelEvent.HTTP_METHOD_CHANGE, model));
            }
        });
    }

    private void initTabeNameText(final Composite top) {

        tabeNameText = new Text(top, SWT.NONE);
        tabeNameText.setText(CoreMessages.EMPTY_TITLE_NAME);
        tabeNameText.setTextLimit(20);
        tabeNameText.setToolTipText("Tab Name");
        tabeNameText.setForeground(ResourceUtils.getResourceCache().getColor(GRAY_RGB_TEXT));
        tabeNameText.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(final FocusEvent e) {
                final String txt = tabeNameText.getText().trim();
                if ("".equals(txt)) {
                    tabeNameText.setText(CoreMessages.EMPTY_TITLE_NAME);
                } else {
                    tabeNameText.setText(txt);
                    tabItem.setText(txt);
                    model.setName(txt);
                }
            }

            @Override
            public void focusGained(final FocusEvent e) {
                final String txt = tabeNameText.getText().trim();
                if (CoreMessages.EMPTY_TITLE_NAME.equals(txt)) {
                    tabeNameText.setText("");
                }
            }
        });
    }

    private boolean isPost() {
        return httpCombo.getText().equals(CoreConstants.HTTP_POST);
    }

    private boolean isPut() {
        return httpCombo.getText().equals(CoreConstants.HTTP_PUT);
    }

    private boolean isXwwwType() {
        Utils.textToModelHeaders(headerView.getHeaderText(), model);
        return JunkUtils.isXwwwFormType(model);
    }

    private boolean isMultipartType() {
        Utils.textToModelHeaders(headerView.getHeaderText(), model);
        return JunkUtils.isMultiartFormType(model);
    }

    private boolean isParamsEditable() {
        return paramView.isEditable();
    }

    private void doDispose() {
        if (httpJob != null) {
            httpJob.cancel();
        }
        buzJob.abort(false);
    }

    @Override
    public String toString() {
        return "ItemView{" + "httpCombo=" + httpCombo.getText() + ",urlCombo=" + urlCombo.getText() + ",headersText=" + headerView.getHeaderText()
                + ",paramsText=" + paramView.getParamText() + ",bodyText=" + bodyView.getText() + "}";
    }

}
