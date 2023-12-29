package org.roussev.http4e.jmeter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.roussev.http4e.httpclient.core.ExceptionHandler;
import org.roussev.http4e.httpclient.core.util.HttpBean;

public class LiveHttpHeadersParser {

    private final Collection<HttpBean> httpBeans = new ArrayList<>();

    public Collection<HttpBean> getHttpBeans() {
        return httpBeans;
    }

    public void parse(final String file) throws Exception {

        String line;

        // Open the file for reading
        try (final BufferedReader br = new BufferedReader(new FileReader(file))) {
            boolean isResponse = false;
            final StringBuilder methodBuff = new StringBuilder();
            final StringBuilder headBuff = new StringBuilder();
            final StringBuilder bodyBuff = new StringBuilder();

            final HttpBean lastBean = new HttpBean();

            while ((line = br.readLine()) != null) {

                if (line.startsWith("HTTP/1.")) {
                    isResponse = true;
                }

                if (!isResponse) {
                    line = filter(line);
                    readLine(line, methodBuff, headBuff, bodyBuff);

                    if (line.startsWith("https://")) {
                        lastBean.setProtocol("https");
                    } else if (line.startsWith("http://")) {
                        lastBean.setProtocol("http");
                    }
                }

                if (line.startsWith("----------------------------------------------------------")) {
                    isResponse = false;
                }
            }
//			System.out.println("---------------------\t\tline=" + line + "\t\t, methodBuff=" + methodBuff + "\t\t, headBuff=" + headBuff + "\t\t, bodyBuff=" + bodyBuff);

            doMethod(methodBuff.toString(), lastBean);
            doHeaders(headBuff.toString(), lastBean);
            doBody(bodyBuff.toString(), lastBean);
            httpBeans.add(lastBean);

//			readBuffer(line, methodBuff, headBuff, bodyBuff);
        } catch (final IOException e) {
            ExceptionHandler.handle(e);
        }
    }

    private String filter(String line) {
        if (line.contains("&")) {
            line = line.replace("&", "&amp;");
        }
        return line;
    }

    private void readLine(final String line, final StringBuilder methodBuff, final StringBuilder headBuff, final StringBuilder bodyBuff) throws Exception {
        if (line.startsWith("http://") || line.startsWith("https://")) {

//			System.out.println(line + " methodBuff= '" + methodBuff + "'");
            if (methodBuff.length() > 0) {
                final HttpBean bean = new HttpBean();
                bean.setProtocol(line.startsWith("http://") ? "http" : "https");
                httpBeans.add(bean);
                doMethod(methodBuff.toString(), bean);
                doHeaders(headBuff.toString(), bean);
                doBody(bodyBuff.toString(), bean);

                methodBuff.delete(0, methodBuff.length());
                headBuff.delete(0, headBuff.length());
                bodyBuff.delete(0, bodyBuff.length());
            }

        } else {
            boolean isMethod = false;
            boolean isHeader = false;
            for (final String element : Constants.METHODS) {
                if (line.startsWith(element + " /")) {
                    isMethod = true;
                    isHeader = false;
                }
            }
            for (final String element : Constants.HEADERS) {
                if (line.startsWith(element + ": ")) {
                    isMethod = false;
                    isHeader = true;
                }
            }

            final boolean isBody = !isMethod && !isHeader;
            if (isHeader) {
                headBuff.append(line + "\n");
            } else if (isBody) {
                bodyBuff.append(line);
            } else if (isMethod) {
                methodBuff.append(line);
            }
        }
    }

    private void doHeaders(final String hStr, final HttpBean bean) throws Exception {
        final Map<String, String> hMap = new HashMap<>();

        final BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(hStr.getBytes("utf-8"))));
        String hLine = null;
        while ((hLine = br.readLine()) != null) {
            final int delimPos = hLine.indexOf(':');
            final String key = hLine.substring(0, delimPos);
            final String val = hLine.substring(delimPos + 2);
            hMap.put(key, val);
        }
        bean.setHeaders(hMap);
        bean.setDomain(hMap.get("Host"));
    }

    private void doMethod(final String mString, final HttpBean bean) {
        final String[] arr = mString.split(" ");
        bean.setMethod(arr[0]);
        bean.setPath(arr[1]);
    }

    private void doBody(final String bString, final HttpBean bean) {
        bean.setBody(bString);
    }

    public static void main(final String[] args) {

        final LiveHttpHeadersParser t = new LiveHttpHeadersParser();
        try {
            t.parse("C:Users/Mitko/Desktop/live-http-headers.txt");
            for (final HttpBean b : t.getHttpBeans()) {
                System.out.println(b);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
