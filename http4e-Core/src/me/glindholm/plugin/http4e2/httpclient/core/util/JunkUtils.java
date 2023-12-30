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
package me.glindholm.plugin.http4e2.httpclient.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.swt.custom.StyledText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.glindholm.plugin.http4e2.crypt.HexUtils;
import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.misc.ApacheHttpListener;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.AssistConstants;
import me.glindholm.plugin.http4e2.httpclient.core.misc.CoreException;
import nu.xom.Builder;
import nu.xom.Serializer;

/**
 * A class with misc utils.
 *
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class JunkUtils {

    private static String XML_LINE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    private final static ApacheHttpListener httpListener = new ApacheHttpListener() {
        @Override
        public void write(final byte[] data) {
            // blank
        }

        @Override
        public void close() {
            // blank
        }
    };

    private final static ResponseReader responseReader = httpMethod -> {
//          HttpUtils.dumpResponse(httpMethod, System.out);
    };

    public static boolean isXwwwFormType(final ItemModel model) {
        final List<String> headers = model.getHeaderValuesIgnoreCase(AssistConstants.HEADER_CONTENT_TYPE);
        if (headers == null) {
            return false;
        }
        try {
            final String lastContType = headers.get(headers.size() - 1);
            return AssistConstants.CONTENT_TYPE_X_WWW_FORM.equalsIgnoreCase(lastContType);

        } catch (final Exception ignore) {
        }

        return false;
    }

    public static boolean isMultiartFormType(final ItemModel model) {
        final List<String> headers = model.getHeaderValuesIgnoreCase(AssistConstants.HEADER_CONTENT_TYPE);
        if (headers == null) {
            return false;
        }
        try {
            final String lastContType = headers.get(headers.size() - 1);
            return AssistConstants.CONTENT_TYPE_MULTIPART.equalsIgnoreCase(lastContType);

        } catch (final Exception ignore) {
        }

        return false;
    }

    public static void hexText(final StyledText styledText, final String text) {
        try {
            styledText.setText(HexUtils.toHex(text.getBytes(CoreConstants.UTF8)));
        } catch (final IOException e) {
            throw CoreException.getInstance(CoreException.UNSUPPORTED_ENCODING, e);
        }
    }

    public static String prettyText(String text) {

        final int inx = text.indexOf(CoreConstants.CRLF + CoreConstants.CRLF);
        if (inx > 5) {
            text = text.substring(inx + 4);
        }

        text = text.trim();

        if (text.startsWith("<?xml")) {
            return prettyXml(text, null);

        } else if (text.startsWith("<")) {
            return prettyXml(text, XML_LINE);

        } else if (text.startsWith("[") || text.startsWith("{")) {
            return jsonText(text, true);

        } else {
            return text;
        }
    }

    public static String jsonText(String txt, final boolean bypassXML) {

        try {
            final boolean isGWTok = txt.startsWith("//OK");
            final boolean isGWTerr = txt.startsWith("//EX");
            if (isGWTok || isGWTerr) {
                txt = txt.substring(4);
            }
            return toJSON(txt, isGWTok, isGWTerr);

        } catch (final JSONException e) {
//         ExceptionHandler.handle(e);
            if (bypassXML) {
                return txt;
            } else {
                final String line = txt.startsWith("<?xml") ? null : XML_LINE;
                return prettyXml(txt, line);
            }

        } catch (final Exception e) {
            ExceptionHandler.handle(e);
            if (bypassXML) {
                return txt;
            } else {
                final String line = txt.startsWith("<?xml") ? null : XML_LINE;
                return prettyXml(txt, line);
            }
        }

    }

    private static String toJSON(final String txt, final boolean isGWTok, final boolean isGWTerr) throws JSONException {

        String res = null;

        if (txt.startsWith("[")) {
            final JSONArray arr = new JSONArray(txt);
            res = arr.toString(4);
        } else {
            final JSONObject obj = new JSONObject(txt);
            res = obj.toString(4);
        }
        if (isGWTok) {
            return "//OK" + res;
        } else if (isGWTerr) {
            return "//EX" + res;
        } else {
            return res;
        }
    }

    public static String prettyXml(final String xml, final String firstLine) {
        try {

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final Serializer serializer = new Serializer(out);
            serializer.setIndent(2);
            if (firstLine != null) {
                serializer.write(new Builder().build(firstLine + xml, ""));
            } else {
                serializer.write(new Builder().build(xml, ""));
            }
            final String ret = out.toString("UTF-8");
            if (firstLine != null) {
                return ret.substring(firstLine.length()).trim();
            } else {
                return ret;
            }
        } catch (final Exception e) {
//         ExceptionHandler.handle(e);
            return xml;
        }
    }

    public static String getHdToken(final String url, final String md) {
        final HttpClient client = new HttpClient();
        final PostMethod post = new PostMethod(url);
        post.setRequestHeader("content-type", "application/x-www-form-urlencoded");
        post.setParameter("v", md);

        post.setApacheHttpListener(httpListener);
        try {
            HttpUtils.execute(client, post, responseReader);
            final Header header = post.getResponseHeader("hd-token");
            if (header != null) {
//            System.out.println("hd-token:" + header.getValue() + "'");
                return header.getValue();
            }
        } catch (final Exception ignore) {
            ExceptionHandler.handle(ignore);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
        return null;
    }

}
