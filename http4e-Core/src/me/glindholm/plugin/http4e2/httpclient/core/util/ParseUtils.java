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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreMessages;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.AssistConstants;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.DocumentUtils;

/**
 * Class with parsing utilities.
 *
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class ParseUtils {

    private final static String[] shortExt = { "com", "net", "org", "biz", "info", "co", "uk", "ca", "de", "eu", "name", "xxx", "us", };
    private final static String[] longExt = { "bg", "be", "ru", "ie", "fr", "es", "cn", "au", "at", "qc", "bc", "jp", "cz", "dk", "edu", "gov", "hk", "il",
            "fi", "it", "kr", "nz", "se", "nl", "no" };

    private final static Collection<String> shortExtList = Arrays.asList(shortExt);
    private final static Collection<String> longExtList = Arrays.asList(longExt);

    public static String appendParamsToUrl(String url, final String params) {
        final int q = url.indexOf(CoreConstants._Q);
        if (q < 0) {
            url = url + CoreConstants._Q + params;
        } else {
            url = url.substring(0, q + 1) + params;
        }
        if (BaseUtils.isEmpty(params)) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String doUrlToText(final String url, final boolean urlDecode) {
        final Map<String, ParseBean> mparams = urlToMap(url, urlDecode);
        final String textData = mapToText(mparams);
        return textData;
    }

    public static String doBodyToParam(final String body) {
        final Map<String, ParseBean> map = linesToMap(body);
        return mapToText(map);
    }

    static Map<String, ParseBean> urlToMap(final String url, final boolean urlDecode) {
        final int q = url.indexOf(CoreConstants._Q);
        String params = "";
        if (q > 0) {
            params = url.substring(q + 1);
            if (urlDecode) {
                params = urlDecode(params);
            }
            return linesToMap(params);
        }
        return new HashMap<>();
    }

    static String mapToText(final Map<String, ParseBean> map) {
        final StringBuilder sb = new StringBuilder();
        for (final String key : map.keySet()) {
            for (final String val : map.get(key).getValues()) {
                sb.append(key);
                sb.append(AssistConstants.BRACKETS_COMPLETION);
                sb.append(val);
                sb.append(CoreConstants._LF);
            }
        }
        if (sb.length() > 0) {
            return sb.substring(0, sb.length() - 1);
        } else {
            return CoreConstants.EMPTY_TEXT;
        }
    }

    public static Map<String, ParseBean> linesToMap(final String params) {
        final StringTokenizer st = new StringTokenizer(params, AssistConstants.PARAM_LINE_DELIM);
        final Map<String, ParseBean> paramsMap = new LinkedHashMap<>();
        String p = null;
        while (st.hasMoreTokens()) {
            p = st.nextToken().trim();
            if (!BaseUtils.isEmpty(p)) {
                lineToMap(p, paramsMap);
            }
        }
        return paramsMap;
    }

    public static Map linesToMap2(final String params) {
        final Properties map = new Properties();
        try {
            map.load(new ByteArrayInputStream(params.toString().getBytes("UTF8")));

        } catch (final Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    private static void lineToMap(final String pline, final Map<String, ParseBean> pmap) {
        final StringTokenizer st = new StringTokenizer(pline, AssistConstants.PARAM_DELIM_EQ);
        final String kk = st.nextToken();
        if (BaseUtils.isEmpty(kk) || DocumentUtils.isComment(kk)) {
            return;
        }
        String vv = "";
        try {
            vv = pline.substring(kk.length() + 1);
        } catch (final StringIndexOutOfBoundsException e) {
        }
        ParseBean pa = pmap.get(kk);
        if (pa == null) {
            pa = new ParseBean(kk);
        }
        pa.addValue(BaseUtils.noNull(vv));
        pmap.put(kk, pa);
    }

    public static String toUrlParams(final String text, final boolean urlEncode) {

        final Map<String, ParseBean> paramMap = linesToMap(text);
        final StringBuilder sb = new StringBuilder();
        for (String key : paramMap.keySet()) {
            final ParseBean p = paramMap.get(key);
            for (String val : p.getValues()) {
                sb.append(key);
                if (val != null) {
                    sb.append("=");
                    val = val.trim();
                    sb.append(urlEncode ? urlEncode(val) : val);
                }
                sb.append(CoreConstants._AND);
            }
        }
        if (sb.length() > 0) {
            return sb.substring(0, sb.length() - 1);
        } else {
            return CoreConstants.EMPTY_TEXT;
        }
    }

    public static String toTitle(String url) {

        if (BaseUtils.isEmpty(url)) {
            return CoreMessages.EMPTY_TITLE;
        }

        final int q = url.indexOf(CoreConstants._Q);
        if (q > 0) {
            url = url.substring(0, q);
        }
        if (url.startsWith(CoreConstants.PROTOCOL_HTTP)) {
            url = url.substring(CoreConstants.PROTOCOL_HTTP.length());
        } else if (url.startsWith(CoreConstants.PROTOCOL_HTTPS)) {
            url = url.substring(CoreConstants.PROTOCOL_HTTPS.length());
        }
        if (url.startsWith(CoreConstants.WWW + ".")) {
            url = url.substring(3 + 1);
        }

        // Transformin roussev.com/aa/bb/cc to 'rous/aa/bb/cc'
        try {
            final int inx = url.indexOf('/');
            if (url.length() > CoreConstants.MIN_TAB_NAME_SIZE && inx > 0) {
                final String domain = url.substring(0, inx);
                final String path = url.substring(inx);
                String nameWithoutExtension = domain;
                final String[] split = domain.split("\\.");
                for (final String name : split) {
                    if (!shortExtList.contains(name) && !longExtList.contains(name)) {
                        if (!shortExtList.contains(name)) {
                            nameWithoutExtension = name;
                        }
                    }
                }

                if (nameWithoutExtension.length() > 5) {
                    nameWithoutExtension = nameWithoutExtension.substring(0, 4);
                }

                url = nameWithoutExtension + "" + path;
            }
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
        }

        if (url.length() <= CoreConstants.MAX_TAB_NAME_SIZE) {
            return url;
        }

        final int minSize = url.indexOf(CoreConstants._SLASH);
        if (CoreConstants.MAX_TAB_NAME_SIZE > minSize) {
            url = url.substring(0, CoreConstants.MAX_TAB_NAME_SIZE);
        } else {
            url = url.substring(0, minSize);
        }

        return url;
    }

    /**
     * Translates paramText to: List[0] urlText, List[1] bodyText.
     *
     * @return
     */
    public static List<String> paramToUrlAndBody(final String paramText) {
        final List<String> res = new ArrayList<>();
        final Map<String, String> parameterizedArgs = (Map<String, String>) CoreContext.getContext().getObject(CoreObjects.PARAMETERIZE_ARGS);

        final Map<String, ParseBean> paramMap = ParseUtils.linesToMap(paramText);
        final StringBuilder sBody = new StringBuilder();
        final StringBuilder sUrl = new StringBuilder();

        for (final String key : paramMap.keySet()) {
            final ParseBean pp = paramMap.get(key);
            if (pp.getValues().size() == 0) {
                sBody.append(key);
                sBody.append(CoreConstants._AND);
                sUrl.append(urlEncode(key /* parameterizedVal */)); // TODO. What's
                // the meaning
                // of ParseBean.
                // Seams dead
                // class?
                sUrl.append(CoreConstants._AND);
            } else {
                for (String val : pp.getValues()) {
                    final String parameterizedVal = ParseUtils.getParametizedArg(val, parameterizedArgs);
                    // -- body
                    sBody.append(key);
                    sBody.append(CoreConstants._EQ);
                    sBody.append(/* val */parameterizedVal);
                    sBody.append(CoreConstants._AND);
                    // -- url
                    sUrl.append(key);
                    sUrl.append(CoreConstants._EQ);
                    sUrl.append(urlEncode(/* val */parameterizedVal));
                    sUrl.append(CoreConstants._AND);
                }
            }
        }
        res.add(sUrl.toString());
        res.add(sBody.toString());
        // System.out.println("sBody[" + sBody + "]");
        // System.out.println("sUrl [" + sUrl + "]");

        return res;
    }

    public static String getParametizedArg(final String argValue, final Map<String, String> parameterizedMap) {

        String parameterizedVal = null;
        if (parameterizedMap != null && argValue != null && argValue.startsWith("@")) {
            final String parameterizedKey = argValue.substring(0);
            parameterizedVal = parameterizedMap.get(parameterizedKey);
        }

        if (parameterizedVal != null) {
            return parameterizedVal;

        } else {
            return argValue;
        }
    }

    public static String urlEncode(final String sUrl) {
        if (sUrl == null) {
            return "";
        }
        try {
            return URLEncoder.encode(sUrl, CoreConstants.UTF8);
        } catch (final UnsupportedEncodingException e) {
            ExceptionHandler.handle(e);
            return sUrl;
        }
    }

    public static String urlDecode(final String sUrl) {
        if (sUrl == null) {
            return "";
        }
        try {
            return URLDecoder.decode(sUrl, CoreConstants.UTF8);
        } catch (final UnsupportedEncodingException e) {
            ExceptionHandler.handle(e);
            return sUrl;
        }
    }

    public static String doUrlToParam2(final String url, final Map<String, String> existingParams, final boolean urlDecode) {
        final int q = url.indexOf(CoreConstants._Q);
        String params = "";
        if (q > 0) {
            params = url.substring(q + 1);
            if (urlDecode) {
                params = urlDecode(params);
            }
        }
        final StringTokenizer st = new StringTokenizer(params, "" + CoreConstants._AND);
        final StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            String line = st.nextToken();

            // existing param. Check if its being parameterized
            final String parKey = line.substring(0, line.indexOf("="));
            final String tokVal = existingParams.get(parKey);
            if (tokVal != null && tokVal.startsWith("@")) {
                line = parKey + "=" + tokVal;
            }
            sb.append(line);
            sb.append(CoreConstants._LF);
        }
        return sb.toString();
    }

    public static String bodyToParam2(final String body) {
        final StringTokenizer st = new StringTokenizer(body, "" + CoreConstants._AND);
        final StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            final String line = st.nextToken();
            sb.append(line);
            sb.append(CoreConstants._LF);
        }
        return sb.toString();
    }

    public static void main(final String[] args) throws Exception {
        // System.out.println(decode("qwe", "ISO-8859-1"));
        // System.out.println(decode("qwe", "UTF8"));
        // System.out.println(encode("qwe", "ISO-8859-1"));
        // System.out.println(encode("qwe", "UTF8"));
        // System.out.println( paramsToURL( "http://qwe.com?asd", "dd=1"));
        // System.out.println( paramsToURL( "?asd", "dd=1"));
        // System.out.println( paramsToURL( "asd", "dd=1"));
        // System.out.println(doUrlToText("asd=1&dgf=2 7&i"));

        // System.out.println( doTextToUrl("http://qwe.com", toUrlParams("dd:
        // =1")));
        // System.out.println( toUrlParams("dd: 6 \n \r x=9"));
        // String params = "aa:1 \r\n bb=2";
        // System.out.println(appendParamsToUrl("http://qwe.com", "555555"));
        // String orig = "k1 1\n \rk1 2\nk2=a";
        // String url = "http://asd.com?" + paramToUrlAndBody(orig).get(0);
        // System.out.println( url);
        // System.out.println( doUrlToText( url, true));
        // String body = (String)paramToUrlAndBody(orig).get(1);
        // StringTokenizer st = new StringTokenizer(body, ""+CoreConstants._AND);
        // StringBuilder sb = new StringBuilder();
        // while(st.hasMoreTokens()){
        // String line = st.nextToken();
        // sb.append(line);
        // sb.append(CoreConstants._LF);
        // }
        // System.out.println(sb);

        // System.out.println( doUrlToText2( "q?k1=++2&k1=1&k2=a&", true));

    }

}