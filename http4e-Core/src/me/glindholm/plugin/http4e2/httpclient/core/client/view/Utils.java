package me.glindholm.plugin.http4e2.httpclient.core.client.view;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.AuthItem;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ProxyItem;
import me.glindholm.plugin.http4e2.httpclient.core.misc.CoreException;
import me.glindholm.plugin.http4e2.httpclient.core.util.HttpBean;
import me.glindholm.plugin.http4e2.httpclient.core.util.ParseUtils;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class Utils {

    public static boolean isIDE() {
        return CoreContext.getContext().getObject(CoreObjects.IS_STANDALONE) == null;
    }

    public static boolean isAutoAssistInvoked(final KeyEvent e) {
        if (e.keyCode == 32 && (e.stateMask & SWT.CTRL) != 0 || e.keyCode == 32 && (e.stateMask & SWT.COMMAND) != 0) {
            return true;

        } else if (e.character == ' ' && (e.stateMask & SWT.CTRL) != 0 || e.character == ' ' && (e.stateMask & SWT.COMMAND) != 0) {
            return true;

        }
        return false;

    }

    public static ToolItem getItem(final int position, final ViewForm vForm) {
        final ToolItem item = null;
        final Control[] arr1 = vForm.getChildren();
        for (final Control element : arr1) {
            if (element instanceof final ToolBar tBar) {
                final ToolItem[] arr2 = tBar.getItems();
                return arr2[position];
            }
        }
        return item;
    }

    public static boolean isHttp(final String url) {
        if (url != null && url.startsWith(CoreConstants.PROTOCOL_HTTP)) {
            return true;
        }
        return false;
    }

    public static boolean isHttpS(final String url) {
        if (url != null && url.startsWith(CoreConstants.PROTOCOL_HTTPS)) {
            return true;
        }
        return false;
    }

    public static String appendProtocol(String url) {
        if (url != null && !isHttp(url) && !isHttpS(url)) {
            url = CoreConstants.PROTOCOL_HTTP + url;
        }
        return url;
    }

    public static String trimProtocol(String url) {
        if (url != null) {
            if (isHttp(url)) {
                url = url.substring(CoreConstants.PROTOCOL_HTTP.length());
            } else if (isHttpS(url)) {
                url = url.substring(CoreConstants.PROTOCOL_HTTPS.length());
            }
        }
        return url;
    }

    public static void viewToModel(final ItemModel iModel, final ItemView iView) {

        if (iView.getTabName() != null) {
            iModel.setName(iView.getTabName());
        } else {
            iModel.setName(trimProtocol(iView.urlCombo.getText()));
        }
        iModel.setHttpMethod(iView.httpCombo.getText());
        iModel.setUrl(appendProtocol(iView.urlCombo.getText()));
        iModel.setHSashWeights(iView.hSash.getWeights());
        iModel.setVSashWeights(iView.vSash.getWeights());

        // TODO bytes[], mulitpart ?
        iModel.setBody(iView.bodyView.getText());

        iModel.clearHeaders();
        iModel.clearParameters();

        textToModelHeaders(iView.headerView.getHeaderText(), iModel);
        String txt = iView.paramView.getParamText();
        txt = txt.replace('\\', '/');
        textToModelParams(txt, iModel);

        // TODO use Auth and Proxy per item not globally
        final CoreContext ctx = CoreContext.getContext();
        iModel.setProxy((ProxyItem) ctx.getObject(CoreObjects.PROXY_ITEM));
        iModel.setAuth((AuthItem) ctx.getObject(CoreObjects.AUTH_ITEM));
    }

    public static void textToModelHeaders(final String text, final ItemModel iModel) {
        iModel.clearHeaders();
        if (text != null && !text.trim().equals("")) {
            final Properties p = new Properties();
            try {
                final InputStreamReader inR = new InputStreamReader(new ByteArrayInputStream(text.getBytes("UTF8")), "UTF8");
                final BufferedReader buf = new BufferedReader(inR);
                p.load(buf);

            } catch (final UnsupportedEncodingException e) {
                throw new CoreException(CoreException.UNSUPPORTED_ENCODING, e);
            } catch (final IOException e) {
                throw new CoreException(CoreException.IO_EXCEPTION, e);
            }

            for (final Iterator iter = p.entrySet().iterator(); iter.hasNext();) {
                final Map.Entry<String, String> me = (Map.Entry<String, String>) iter.next();
                iModel.addHeader(me.getKey(), me.getValue());
            }
        }
    }

    public static void textToModelParams(final String text, final ItemModel iModel) {
        iModel.clearParameters();
        if (!CoreConstants.EMPTY_TEXT.equals(text)) {
            final Properties p = new Properties();
            try {
                final InputStreamReader inR = new InputStreamReader(new ByteArrayInputStream(text.getBytes("UTF8")), "UTF8");
                final BufferedReader buf = new BufferedReader(inR);
                p.load(buf);

            } catch (final UnsupportedEncodingException e) {
                throw new CoreException(CoreException.UNSUPPORTED_ENCODING, e);
            } catch (final IOException e) {
                throw new CoreException(CoreException.IO_EXCEPTION, e);
            }

            for (final Iterator iter = p.entrySet().iterator(); iter.hasNext();) {
                final Map.Entry<String, String> me = (Map.Entry<String, String>) iter.next();
                iModel.addParameter(me.getKey(), me.getValue());
            }
        }
    }

    public static void modelToView(final ItemModel iModel, final ItemView iView) {

        iView.httpCombo.setText(iModel.getHttpMethod());
        iView.urlCombo.setText(iModel.getUrl());
        if (iModel.getHeaders().isEmpty()) {
            iModel.fireExecute(new ModelEvent(ModelEvent.HEADERS_FOCUS_LOST, iModel));
        } else {
            iView.headerView.setText(listToString(iModel.getHeaders()));
        }
        if (iModel.getParameters().isEmpty()) {
            iModel.fireExecute(new ModelEvent(ModelEvent.PARAMS_FOCUS_LOST, iModel));
        } else {
            iView.paramView.setParamText(listToString(iModel.getParameters()));
        }

        iView.bodyView.setText(iModel.getBody());
        iView.requestView.setHttpText(iModel.getRequest());
        iView.responseView.setHttpText(iModel.getResponse());
        iView.tabItem.setText(ParseUtils.toTitle(iModel.getName()));
        iView.tabeNameText.setText(iView.tabItem.getText());
    }

    private static HttpBean urlToHttpBean(final String url) {

        String domain = "";
        String path = "";
        String port = "";
        int domainInx = -1;
        String domainPortPath = "";

        final String[] splitt = url.split("://");
        final String protocol = splitt[0];

        try {
            domainPortPath = splitt[1];
            domainInx = domainPortPath.indexOf("/");
            if (domainInx < 0) {
                final int questIndx = domainPortPath.indexOf("?");
                if (questIndx < 0) {
                    domainInx = domainPortPath.length();
                } else {
                    domainInx = questIndx;
                }
            }
        } catch (final Exception ignore) {
            // ignore.printStackTrace();
        }
        try {
            domain = domainPortPath.substring(0, domainInx);
            path = domainPortPath.substring(domainInx);

            final String[] domainPort = domain.split(":");
            domain = domainPort[0];
            port = domainPort[1];
        } catch (final Exception ignore) {
            // ignore.printStackTrace();
        }

        final HttpBean bean = new HttpBean();
        bean.setProtocol(protocol);
        bean.setDomain(domain);
        bean.setPath(path);
        bean.setPort(port);

        return bean;
    }

    public static HttpBean modelToHttpBean(final ItemModel iModel) {

        final HttpBean b = urlToHttpBean(iModel.getUrl());

        b.setMethod(iModel.getHttpMethod());

        final boolean headersOnly = "GET".equalsIgnoreCase(iModel.getHttpMethod()) || "HEAD".equalsIgnoreCase(iModel.getHttpMethod())
                || "OPTIONS".equalsIgnoreCase(iModel.getHttpMethod()) || "TRACE".equalsIgnoreCase(iModel.getHttpMethod())
                || "DELETE".equalsIgnoreCase(iModel.getHttpMethod());

        if (headersOnly) {
            b.setBody("");
        } else {
            b.setBody(iModel.getBody());
        }

        for (final String hName : iModel.getHeaders().keySet()) {
            final List<String> hList = iModel.getHeaderValuesIgnoreCase(hName);
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hList.size(); i++) {
                sb.append(hList.get(i));
                if (i < hList.size() - 1) {
                    sb.append(",");
                }
            }
            final String hVal = sb.toString();
            b.getHeaders().put(hName, hVal);
        }

        if ("POST".equalsIgnoreCase(iModel.getHttpMethod())) {
            // TODO it is retrieving only the last parameter from multiple paramset
            for (final String key : iModel.getParameters().keySet()) {
                final List<String> pList = iModel.getParameters().get(key);
                b.getParams().put(key, "");
                if (pList != null) {
                    for (final String pVal : pList) {
                        b.getParams().put(key, pVal);
                    }
                }
            }
        }

        return b;
    }

    public static String listToString(final Map<String, List<String>> map) {
        final StringBuilder sb = new StringBuilder();
        final int size = map.size();
        int cnt = 0;
        for (final Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
            final String key = it.next();
            cnt++;
            sb.append(key + CoreConstants._EQ);
            final List<String> values = map.get(key);
            for (int i = 0; i < values.size(); i++) {
                if (i != 0) {
                    sb.append(CoreConstants._SEMICOL + CoreConstants._SPACE);
                }
                sb.append(values.get(i));
            }
            if (cnt != size) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

}
