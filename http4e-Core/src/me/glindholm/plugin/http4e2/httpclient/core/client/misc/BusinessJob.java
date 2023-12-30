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
package me.glindholm.plugin.http4e2.httpclient.core.client.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.TraceMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.eclipse.swt.widgets.Composite;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreMessages;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.AuthItem;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ProxyItem;
import me.glindholm.plugin.http4e2.httpclient.core.misc.CoreException;
import me.glindholm.plugin.http4e2.httpclient.core.util.ParseUtils;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
@SuppressWarnings({ "deprecation", "unchecked" })
public class BusinessJob {

    private ItemModel model;
    private HttpMethod httpMethod = null;
    private boolean buzDisposed = false;
    private static Map<String, String> contentTypes;

    public void execute(final ItemModel model, final Composite composite) {
        buzDisposed = false;
        this.model = model;

        final Map<String, String> parameterizedArgs = (Map<String, String>) CoreContext.getContext().getObject(CoreObjects.PARAMETERIZE_ARGS);

        try {
            if (CoreConstants.HTTP_GET.equals(model.getHttpMethod())) {
                httpMethod = new GetMethod(model.getUrl());
                addHeaders(httpMethod, parameterizedArgs);

            } else if (CoreConstants.HTTP_POST.equals(model.getHttpMethod())) {

                if (isMultipart(model)) {
                    httpMethod = new MultipartPostMethod(model.getUrl());
                    addHeaders(httpMethod, parameterizedArgs);
                    addMultipart_Parts((MultipartPostMethod) httpMethod, parameterizedArgs);
                    // addFileParts((MultipartPostMethod) httpMethod, model);

                } else {
                    httpMethod = new PostMethod(model.getUrl());
                    addHeaders(httpMethod, parameterizedArgs);
                    addParams((PostMethod) httpMethod, parameterizedArgs);
                    try {
                        ((PostMethod) httpMethod).setRequestEntity(getRequestEntity(model));
                    } catch (final UnsupportedEncodingException e) {
                        throw new CoreException(CoreException.UNSUPPORTED_ENCODING, e);
                    }
                }

            } else if (CoreConstants.HTTP_HEAD.equals(model.getHttpMethod())) {
                httpMethod = new HeadMethod(model.getUrl());
                addHeaders(httpMethod, parameterizedArgs);

            } else if (CoreConstants.HTTP_PUT.equals(model.getHttpMethod())) {
                httpMethod = new PutMethod(model.getUrl());
                addHeaders(httpMethod, parameterizedArgs);
                try {
                    ((PutMethod) httpMethod).setRequestEntity(getRequestEntity(model));
                } catch (final UnsupportedEncodingException e) {
                    throw new CoreException(CoreException.UNSUPPORTED_ENCODING, e);
                }

            } else if (CoreConstants.HTTP_DELETE.equals(model.getHttpMethod())) {
                httpMethod = new DeleteMethod(model.getUrl());
                addHeaders(httpMethod, parameterizedArgs);

            } else if (CoreConstants.HTTP_TRACE.equals(model.getHttpMethod())) {
                httpMethod = new TraceMethod(model.getUrl());
                addHeaders(httpMethod, parameterizedArgs);

            } else if (CoreConstants.HTTP_OPTIONS.equals(model.getHttpMethod())) {
                httpMethod = new OptionsMethod(model.getUrl());
                addHeaders(httpMethod, parameterizedArgs);

            } else {
                throw new CoreException(CoreException.HTTP_METHOD_NOT_IMPLEMENTED, "Method '" + model.getHttpMethod() + "' not implemented.");
            }

            doExecute(httpMethod, model.getCurrentProxy(), model.getAvailableKeystore(), composite);

        } catch (final Throwable e) {
            e.printStackTrace();
            abort(true);

        } finally {
            // System.out.println("Before invoking HTTP_ACTION_STOP ..");
            // model.fireExecute(new ModelEvent(EventType.REQUEST_STOPED, model));
        }
    }

    private boolean isMultipart(final ItemModel model) {
        final List<String> headers = model.getHeaders().get("Content-Type");
        if (headers == null) {
            return false;
        }
        final String hVal = headers.get(0);
        return "multipart/form-data".equalsIgnoreCase(hVal);
    }

    // private void addFileParts( MultipartPostMethod postMethod, final ItemModel
    // model){
    // String[] files = model.getBody().split("@");
    // for (String fi : files) {
    // if (BaseUtils.isEmpty(fi)) {
    // continue;
    // }
    // File f = new File(fi.trim());
    // try {
    // // postMethod.addParameter("file", f);
    // postMethod.addPart(new FilePart("file", f));
    // } catch (FileNotFoundException e) {
    // ExceptionHandler.handle(e);
    // }
    // }
    // }

    private RequestEntity getRequestEntity(final ItemModel model) throws UnsupportedEncodingException {

        RequestEntity re = null;
        if (model.getBody().startsWith(CoreConstants.FILE_PREFIX)) {
            String file = model.getBody();
            file = file.substring(1);
            try {
                re = new InputStreamRequestEntity(new FileInputStream(file));
            } catch (final Exception e) {
                ExceptionHandler.handle(e);
                re = new StringRequestEntity(model.getBody(), null, CoreConstants.UTF8);
            }
        } else {
            re = new StringRequestEntity(model.getBody(), null, CoreConstants.UTF8);
        }

        return re;
    }

    private void addParams(final PostMethod postMethod, final Map<String, String> parameterizedMap) {
        for (Entry<String, List<String>> me : model.getParameters().entrySet()) {
            final String key = me.getKey();
            final List<String> values = me.getValue();
            final StringBuilder sb = new StringBuilder();
            int cnt = 0;
            for (final Iterator<String> it2 = values.iterator(); it2.hasNext();) {
                final String val = it2.next();
                if (cnt != 0) {
                    sb.append(",");
                }
                sb.append(val);
                cnt++;
            }

            final String parameterizedVal = ParseUtils.getParametizedArg(sb.toString(), parameterizedMap);
            postMethod.setParameter(key, parameterizedVal);
        }
    }

    private void addMultipart_Parts(final MultipartPostMethod method, final Map<String, String> parameterizedMap) {
        for (final Entry<String, List<String>> me : model.getParameters().entrySet()) {
            final String key = me.getKey();
            final List<String> values = me.getValue();
            final StringBuilder sb = new StringBuilder();
            int cnt = 0;
            for (final Iterator<String> it2 = values.iterator(); it2.hasNext();) {
                final String val = it2.next();
                if (cnt != 0) {
                    sb.append(",");
                }
                sb.append(val);
                cnt++;
            }

            final String parameterizedVal = ParseUtils.getParametizedArg(sb.toString(), parameterizedMap);

            if (parameterizedVal.startsWith("@")) {
                String path = "";
                String contentType = "";
                try {
                    path = parameterizedVal.substring(1).trim();
                    path = path.replace('\\', '/');
                    contentType = getContentType(getFileExt(path));
                    final File f = new File(path);
                    method.addPart(new FilePart(key, f, contentType, null));
                    // postMethod.addParameter("file", f);
                    // postMethod.addPart(new FilePart("file", f));
                } catch (final FileNotFoundException fnfe) {
                    ExceptionHandler.handle(fnfe);
                }

            } else {
                method.addPart(new StringPart(key, parameterizedVal));

            }
        }
    }

    private void addHeaders(final HttpMethod httpMethod, final Map<String, String> parameterizedMap) {
        for (final Entry<String, List<String>> me : model.getHeaders().entrySet()) {
            final String key = me.getKey();
            final List<String> values = me.getValue();
            final StringBuilder sb = new StringBuilder();
            int cnt = 0;
            for (final Iterator<String> it2 = values.iterator(); it2.hasNext();) {
                final String val = it2.next();
                if (cnt != 0) {
                    sb.append(",");
                }
                sb.append(val);
                cnt++;
            }

            final String parameterizedVal = ParseUtils.getParametizedArg(sb.toString(), parameterizedMap);
            httpMethod.addRequestHeader(key, parameterizedVal);
        }

        // add User-Agent: ProjectName
        boolean userAgentExist = false;
        final Header[] hhh = httpMethod.getRequestHeaders();
        for (final Header h : hhh) {
            if (CoreConstants.HEADER_USER_AGENT.equalsIgnoreCase(h.getName())) {
                userAgentExist = true;
            }
        }

        if (!userAgentExist) {
            httpMethod.addRequestHeader(CoreConstants.HEADER_USER_AGENT, CoreMessages.PLUGIN_NAME_SHORT + "/" + CoreMessages.VERSION);
        }
    }

    private void doExecute(final HttpMethod httpMethod, final ProxyItem proxy, final String sslKeystore, final Composite composite) {
        final JobListener jobListener = new JobListener() {

            private boolean jDisposed;

            @Override
            public boolean isDisposed() {
                return jDisposed;
            }

            @Override
            public void update(final Object obj) {
                synchronized (BusinessJob.this) {
                    if (BusinessJob.this.isDisposed()) {
                        return;
                    }

                    final String[] values = (String[]) obj;
                    model.setRequest(values[0]);
                    model.fireExecute(new ModelEvent(ModelEvent.REQUEST_APPENDED, model));
                    model.setResponse(values[1]);
                    model.fireExecute(new ModelEvent(ModelEvent.REQUEST_STOPPED, model));
                }
                jDisposed = true;
            }

            @Override
            public Object execute() {
                final CoreContext ctx = CoreContext.getContext();
                final AuthItem authItem = (AuthItem) ctx.getObject(CoreObjects.AUTH_ITEM);
                final ProxyItem proxyItem = (ProxyItem) ctx.getObject(CoreObjects.PROXY_ITEM);
                final String[] files = { generateFilePath("REQ.txt", httpMethod), generateFilePath("RESP.txt", httpMethod) };
                model.setPayloadFiles(files);
                model.fireExecute(new ModelEvent(ModelEvent.PAYLOAD_FILES, model));
                final HttpPerformer httpPerformer = new HttpPerformer(httpMethod, proxyItem, authItem, files);
                try {
                    httpPerformer.execute();
                    return new String[] { httpPerformer.getRequest(), httpPerformer.getResponse() };

                } catch (final ConnectException e) {
                    // ExceptionHandler.handle(e);
                    return new String[] { httpPerformer.getRequest(),
                            CoreMessages.CONNECTION_REFUSED + "[" + model.getHttpMethod() + " " + model.getUrl() + "]\n\n\t" + getProxyInfo(model) };

                } catch (final NoHttpResponseException e) {
                    // ExceptionHandler.handle(e);
                    return new String[] { httpPerformer.getRequest(),
                            CoreMessages.CONNECTION_DROPPED + "[" + model.getHttpMethod() + " " + model.getUrl() + "]\n\n\t" + getProxyInfo(model) };

                } catch (final IOException | IllegalStateException e) {
                    // ExceptionHandler.handle(e);
                    return new String[] { httpPerformer.getRequest(),
                            CoreMessages.CONNECTION_IO_ERR + "[" + model.getHttpMethod() + " " + model.getUrl() + "]\n\n\t" + getProxyInfo(model) };

                } catch (final Throwable e) {
                    // ExceptionHandler.handle(e);
                    return new String[] { httpPerformer.getRequest(),
                            CoreMessages.CONNECTION_UNKNOWN + "[" + model.getHttpMethod() + " " + model.getUrl() + "]\n\n\t" + getProxyInfo(model) };
                }
            }
        };
        final JobRunner jobRunner = new JobRunner(composite, jobListener);
        jobRunner.run();
        // BusyIndicator.showWhile(composite.getDisplay(), jobRunner);
    }

    private String getProxyInfo(final ItemModel m) {
        final ProxyItem pi = m.getProxy();
        if (pi.isProxy()) {
            return "using proxy [" + pi.getHost() + ":" + pi.getPort() + "]";
        } else {
            return "";
        }
    }

    public synchronized void abort(final boolean withNotify) {
        if (withNotify) {
            model.fireExecute(new ModelEvent(ModelEvent.REQUEST_ABORTED, model));
        }
        buzDisposed = true;

        if (httpMethod == null) {
            return;
        }
        try {
            httpMethod.abort();
            httpMethod = null;
        } catch (final Exception giveup) {
        }
    }

    public boolean isDisposed() {
        return buzDisposed;
    }

    private String generateFilePath(final String ext, final HttpMethod httpmethod) {

        String urlPath = ParseUtils.toTitle(httpmethod.getPath());
        urlPath = urlPath.replace("/", "-");
        if (urlPath.startsWith("-")) {
            urlPath = urlPath.substring(1);
        }
        final SimpleDateFormat formatter = new SimpleDateFormat("MMddHHmmss");
        return CoreContext.PRODUCT_USER_DIR + File.separator + urlPath + "-" + formatter.format(new Date()) + "." + ext;

    }

    private static String getFileExt(final String path) {
        if (path.contains(".")) {
            try {
                final String[] parts = path.split("\\.");
                return parts[parts.length - 1];

            } catch (final Exception e) {
                // ignore
            }
        }
        return null;
    }

    private static String getContentType(final String fileExtension) {
        if (contentTypes == null) {
            contentTypes = new HashMap<>();
            contentTypes.put("au", "audio/basic");
            contentTypes.put("avi", "video/x-msvideo");
            contentTypes.put("avx", "video/x-rad-screenplay");
            contentTypes.put("bcpio", "application/x-bcpio");
            contentTypes.put("bin", "application/octet-stream");
            contentTypes.put("bmp", "image/bmp");
            contentTypes.put("body", "text/html");
            contentTypes.put("class", "application/java");
            contentTypes.put("css", "text/css");
            contentTypes.put("dib", "image/bmp");
            contentTypes.put("doc", "application/msword");
            contentTypes.put("dtd", "application/xml-dtd");
            contentTypes.put("eps", "application/postscript");
            contentTypes.put("exe", "application/octet-stream");
            contentTypes.put("gif", "image/gif");
            contentTypes.put("gtar", "application/x-gtar");
            contentTypes.put("gz", "application/x-gzip");
            contentTypes.put("htm", "text/html");
            contentTypes.put("html", "text/html");
            contentTypes.put("jad", "text/vnd.sun.j2me.app-descriptor");
            contentTypes.put("jar", "application/java-archive");
            contentTypes.put("java", "text/plain");
            contentTypes.put("jnlp", "application/x-java-jnlp-file");
            contentTypes.put("jpe", "image/jpeg");
            contentTypes.put("jpeg", "image/jpeg");
            contentTypes.put("jpg", "image/jpeg");
            contentTypes.put("js", "text/javascript");
            contentTypes.put("m3u", "audio/x-mpegurl");
            contentTypes.put("mac", "image/x-macpaint");
            contentTypes.put("mid", "audio/x-midi");
            contentTypes.put("midi", "audio/x-midi");
            contentTypes.put("mov", "video/quicktime");
            contentTypes.put("movie", "video/x-sgi-movie");
            contentTypes.put("mp3", "audio/x-mpeg");
            contentTypes.put("mp4", "video/mp4");
            contentTypes.put("mpe", "video/mpeg");
            contentTypes.put("mpeg", "video/mpeg");
            contentTypes.put("mpg", "video/mpeg");
            contentTypes.put("ogx", "application/ogg");
            contentTypes.put("ogv", "video/ogg");
            contentTypes.put("oga", "audio/ogg");
            contentTypes.put("ogg", "audio/ogg");
            contentTypes.put("spx", "audio/ogg");
            contentTypes.put("flac", "audio/flac");
            contentTypes.put("pct", "image/pict");
            contentTypes.put("pdf", "application/pdf");
            contentTypes.put("pic", "image/pict");
            contentTypes.put("pict", "image/pict");
            contentTypes.put("png", "image/png");
            contentTypes.put("pnm", "image/x-portable-anymap");
            contentTypes.put("ppt", "application/vnd.ms-powerpoint");
            contentTypes.put("pps", "application/vnd.ms-powerpoint");
            contentTypes.put("ps", "application/postscript");
            contentTypes.put("psd", "image/x-photoshop");
            contentTypes.put("qt", "video/quicktime");
            contentTypes.put("qti", "image/x-quicktime");
            contentTypes.put("qtif", "image/x-quicktime");
            contentTypes.put("rdf", "application/rdf+xml");
            contentTypes.put("rgb", "image/x-rgb");
            contentTypes.put("rtf", "application/rtf");
            contentTypes.put("rtx", "text/richtext");
            contentTypes.put("sh", "application/x-sh");
            contentTypes.put("src", "application/x-wais-source");
            contentTypes.put("swf", "application/x-shockwave-flash");
            contentTypes.put("tar", "application/x-tar");
            contentTypes.put("tex", "application/x-tex");
            contentTypes.put("texi", "application/x-texinfo");
            contentTypes.put("texinfo", "application/x-texinfo");
            contentTypes.put("tif", "image/tiff");
            contentTypes.put("tiff", "image/tiff");
            contentTypes.put("tr", "application/x-troff");
            contentTypes.put("tsv", "text/tab-separated-values");
            contentTypes.put("txt", "text/plain");
            contentTypes.put("xbm", "image/x-xbitmap");
            contentTypes.put("xht", "application/xhtml+xml");
            contentTypes.put("xhtml", "application/xhtml+xml");
            contentTypes.put("xls", "application/vnd.ms-excel");
            contentTypes.put("xml", "application/xml");
            contentTypes.put("xpm", "image/x-xpixmap");
            contentTypes.put("xsl", "application/xml");
            contentTypes.put("xslt", "application/xslt+xml");
            contentTypes.put("wmv", "video/x-ms-wmv");
            contentTypes.put("Z", "application/x-compress");
            contentTypes.put("z", "application/x-compress");
            contentTypes.put("zip", "application/zip");
        }
        if (fileExtension != null) {
            return contentTypes.get(fileExtension.toLowerCase().trim());
        } else {
            return null;
        }
    }

}
