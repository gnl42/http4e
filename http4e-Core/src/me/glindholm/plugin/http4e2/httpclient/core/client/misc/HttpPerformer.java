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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Base64;

import org.apache.commons.httpclient.CircularRedirectException;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import com.gargoylesoftware.htmlunit.ssl.InsecureSSLProtocolSocketFactory;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.AuthItem;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ProxyItem;
import me.glindholm.plugin.http4e2.httpclient.core.misc.CoreException;
import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.SwtUtils;

@SuppressWarnings("deprecation")
public class HttpPerformer {

    // private final static int LIMIT_BYTES = 100 * 1024;

    private Integer maxSize;
    private final StringBuilder requestBuff = new StringBuilder();
    private final StringBuilder respBuff = new StringBuilder();
    private final HttpMethod httpmethod;
    private final ProxyItem proxy;
    private final AuthItem authItem;
    private final String reqPayloadFile;
    private final String respPayloadFile;

    private ApacheHttpListener httpListener;

    private class MyApacheListener implements ApacheHttpListener {

        int currentReqSize = 0;
        Boolean reachedLimit = null;
        // String packetFilePath;
        FileOutputStream packetFile = null;

        // String getPacketFilePath(){
        // if (packetFilePath == null) {
        // packetFilePath = generateFilePath("req");
        // }
        // return packetFilePath;
        // }

        public FileOutputStream getOutputStream() {
            if (packetFile == null) {
                try {
                    final File dir = new File(CoreContext.PRODUCT_USER_DIR);
                    dir.mkdirs();
                    packetFile = new FileOutputStream(new File(reqPayloadFile));
                } catch (final FileNotFoundException e) {
                    ExceptionHandler.handle(e);
                }
            }
            return packetFile;
        }

        @Override
        public void write(final byte[] data) {
            currentReqSize = currentReqSize + data.length;
            try {
                getOutputStream().write(data);
            } catch (final IOException e1) {
                ExceptionHandler.handle(e1);
            }

            if (isReachedLimit()) {
                return;

            } else if (currentReqSize > getSize()) {
                int delta = currentReqSize - getSize();
                delta = delta > getSize() ? getSize() : delta;
                final byte[] deltaArr = new byte[delta];
                for (int i = 0; i < delta; i++) {
                    deltaArr[i] = data[i];
                }
                try {
                    requestBuff.append(new String(deltaArr, "UTF8"));
                } catch (final UnsupportedEncodingException e) {
                    throw new CoreException(CoreException.UNSUPPORTED_ENCODING, e);
                }

                reachedLimit = true;
                final String cmd = SwtUtils.isMac() ? "  [CMD + O] to open\n\n" : "";
                requestBuff.append(
                        "...........................................................................\n\n#############################################\n\n  Payload too big. Raw packet saved at \n\n You can increase the view size at About/Preferences drop menu \n\n file:///"
                                + reqPayloadFile + "\n\n" + cmd + "#############################################\n\n");
                return;
            }
            try {
                requestBuff.append(new String(data, "UTF8"));
            } catch (final UnsupportedEncodingException e) {
                throw new CoreException(CoreException.UNSUPPORTED_ENCODING, e);
            }
        }

        @Override
        public void close() {
            try {
                getOutputStream().close();
            } catch (final IOException e) {
                ExceptionHandler.handle(e);
            }

            // if (!isReachedLimit()) {
            // File fileToClean = new File(reqPayloadFile);
            // fileToClean.delete();
            // }
        }

        private boolean isReachedLimit() {
            return reachedLimit != null && reachedLimit;
        }
    }

    public HttpPerformer(final HttpMethod httpmethod, final ProxyItem proxy, final AuthItem authItem, final String[] payloadFiles) {
        this.httpmethod = httpmethod;
        this.proxy = proxy;
        this.authItem = authItem;
        httpmethod.setApacheHttpListener(getHttpListener());
        reqPayloadFile = payloadFiles[0];
        respPayloadFile = payloadFiles[1];
    }

    public void execute() throws IOException {

        final HostConfiguration hostConf = httpmethod.getHostConfiguration();
        final String host = hostConf.getHost();
        final int port = hostConf.getPort();
        final HttpClient client = new HttpClient();

        doSSL(client, hostConf, host, 443);

        doAuthentication(client, host, port);

        try {
            // set proxy, if one is configured
            if (proxy.isProxy()) {
                client.getHostConfiguration().setProxy(proxy.getHost(), proxy.getPort());
            }

            // client.getHttpConnectionManager().getParams().setConnectionTimeout(3000);
            httpmethod.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, false);

            // Execute the POST method
            final int statusCode = client.executeMethod(httpmethod);
            respBuff.append(httpmethod.getStatusLine().toString());
            respBuff.append("\r\n");

            if (statusCode != -1) {
                final Header[] h = httpmethod.getResponseHeaders();
                for (final Header element : h) {
                    respBuff.append(element.getName() + CoreConstants._COL + CoreConstants._SPACE + element.getValue());
                    respBuff.append("\r\n");
                }
            }
            respBuff.append("\r\n");

            if (!"HEAD".equals(httpmethod.getName())) {
                _writeResponse(respPayloadFile);
                // InputStreamReader inR = new
                // InputStreamReader(httpmethod.getResponseBodyAsStream(), "UTF8");
                // BufferedReader buf = new BufferedReader(inR);
                // String line;
                // int counter = 0;
                // while ((line = buf.readLine()) != null) {
                // counter = counter + line.length();
                // if (counter > LIMIT_BYTES) {
                // respBuff.append("........................\n......\n###########################\n Payload too big.
                // Raw packet saved at\n "
                // + "getPacketFilePath()" + "\n###########################\n\n");
                // break;
                // } else {
                // respBuff.append(line);
                // respBuff.append("\r\n");
                // }
                // }
            }

        } catch (final CircularRedirectException e) {
            // redirecting .. TODO weird error. keep on eye on it.
            throw new CoreException(CoreException.GENERAL, e);

        } catch (final IllegalStateException | IOException e) { // Stream closed, socket closed
            throw e;

        } catch (final Exception e) {
            throw CoreException.getInstance(CoreException.GENERAL, e);

        } finally {
            httpmethod.releaseConnection();
            getHttpListener().close();
        }
    }

    private void _writeResponse(final String filePath) throws UnsupportedEncodingException, IOException {

        final BufferedInputStream bis = new BufferedInputStream(httpmethod.getResponseBodyAsStream());
        // String filePath = generateFilePath("resp");
        final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));

        // System.out.println("respBuff.toString() '" + respBuff.toString() +
        // "'");
        bos.write(respBuff.toString().getBytes());
        final int readSoFar = respBuff.length();
        final int maxSize = getSize() > readSoFar ? getSize() - readSoFar : 0;

        int bytesRead = 0;
        byte[] buffer = new byte[1024];
        int counter = 0;
        Boolean limitReached = null;

        try {

            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer);
                counter = counter + bytesRead;
                if (counter > maxSize) {
                    if (limitReached == null) {
                        final String cmd = SwtUtils.isMac() ? "  [CMD + O] to open\n\n" : "";
                        respBuff.append(
                                "...........................................................................\n\n#############################################\n\n  Payload too big. Raw packet saved at \n\n  file:///"
                                        + filePath + "\n\n" + cmd
                                        + "  Increase the view size at (i)/Preferences drop menu \n\n#############################################\n\n");
                        // respBuff.append("...........................................................................\n\n#############################################\n\n
                        // Payload too big. Raw packet saved \n\n @"
                        // + filePath + "\n\n [" + ctrlName +
                        // " + O] to open\n\n#############################################\n\n");
                    }
                    limitReached = true;

                } else {
                    final String chunk = new String(buffer, 0, bytesRead);
                    respBuff.append(chunk);
                    // respBuff.append("\r\n");
                }
                buffer = new byte[1024];
            }

            if (bis != null) {
                bis.close();
            }
            if (bos != null) {
                bos.flush();
                bos.close();
            }

            // try {
            // if (limitReached == null || !limitReached) {
            // File f = new File(filePath);
            // if (f.exists()) {
            // f.delete();
            // }
            // }
            // } catch (Exception e) {
            // ExceptionHandler.handle(e);
            // }
        } catch (final Exception e) {
            // e.printStackTrace();

            // 304, etc empty body fails with IO exception, Can't connect to server
            ExceptionHandler.handle(e);
        }

    }

    private void doSSL(final HttpClient client, final HostConfiguration hostConf, final String host, final int port) {
        if (hostConf.getProtocol().isSecure()) {

            // System.setProperty("javax.net.ssl.trustStore", sslKeystore);
            // Protocol sslPprotocol = new Protocol("https", new
            // org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory(),
            // 443);
            // client.getHostConfiguration().setHost(host, 443, sslPprotocol);

            try {
                final ProtocolSocketFactory factory = new InsecureSSLProtocolSocketFactory();
                final Protocol https = new Protocol("https", factory, port);
                Protocol.registerProtocol("https", https);
                client.getHostConfiguration().setHost(host, port, https);

            } catch (final GeneralSecurityException e) {
                throw new CoreException(CoreException.SSL, e);
            }
        }
    }

    private void doAuthentication(final HttpClient client, final String host_, final Integer port_) {
        if (authItem == null) {
            return;
        }
        final String user = authItem.getUsername();
        final String pass = authItem.getPass();

        if (BaseUtils.isEmpty(user) || BaseUtils.isEmpty(pass)) {
            return;
        }

        if (authItem.isBasic()) {
            try {
                final byte[] base64Str = Base64.getEncoder().encode((user + ":" + pass).getBytes("UTF8"));
                httpmethod.setRequestHeader("Authorization", "Basic " + new String(base64Str, "UTF8"));
            } catch (final UnsupportedEncodingException e) {
                ExceptionHandler.handle(e);
            }

        } else if (authItem.isDigest()) {
            try {
                final String host = host_;
                Integer port = null;
                try {
                    port = !BaseUtils.isEmpty(authItem.getPort()) ? Integer.valueOf(authItem.getPort()) : port_;
                } catch (final NumberFormatException ignore) {
                }
                final String realm = !BaseUtils.isEmpty(authItem.getRealm()) ? authItem.getRealm() : AuthScope.ANY_REALM;

                final Credentials defaultcreds = new UsernamePasswordCredentials(user, pass);
                client.getState().setCredentials(new AuthScope(host, port, realm), defaultcreds);
            } catch (final Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    // private String generateFilePath( String ext){
    //
    // String urlPath = ParseUtils.toTitle(httpmethod.getPath());
    // urlPath = urlPath.replace("/", "-");
    // if (urlPath.startsWith("-")) {
    // urlPath = urlPath.substring(1, urlPath.length());
    // }
    // SimpleDateFormat formatter = new SimpleDateFormat("MMddHHmmss");
    // return CoreContext.PRODUCT_USER_DIR + File.separator + (urlPath + "-" +
    // formatter.format(new Date())) + "." + ext;
    //
    // }

    public ApacheHttpListener getHttpListener() {
        if (httpListener == null) {
            httpListener = new MyApacheListener();
        }
        return httpListener;
    }

    public String getResponse() {
        return respBuff.toString();
    }

    public String getRequest() {
        return requestBuff.toString();
    }

    private int getSize() {
        if (maxSize == null) {
            maxSize = (Integer) CoreContext.getContext().getObject(CoreObjects.RESPONSE_VIEW_SIZE);
            maxSize = maxSize * 1024;
            // if(size == null){
            // size = 10;
            // }
            // maxSize =
        }

        return maxSize;
        // IPreferenceStore store = HdPlugin.getDefault().getPreferenceStore();
    }

}
