package me.glindholm.plugin.http4e2.httpclient.core.util.shared;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class Exported {

    private StringBuilder buff;
    private final static String MAINCLASS_POINTER = "mainclass";

    // TODO use velocity
    public Exported(final java.io.InputStream srcStream, final String method, final String url, final String body, final Map<String, List<String>> headers,
            final Map<String, List<String>> parameters) {
        try {

            final boolean headersOnly = "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)
                    || "TRACE".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);

            final byte[] data = new byte[srcStream.available()];
            srcStream.read(data);
            buff = new StringBuilder(new String(data));

            final StringBuilder buffMain = new StringBuilder();
            buffMain.append("\n/*---------- this is your Main class -----------*/");
            buffMain.append("\n\npublic class HTTP4e2 {");
            buffMain.append("\n\n    public static void main(String[] args) {");
            buffMain.append("\n        HttpRunner.Bean bean = new HttpRunner.Bean();");
            buffMain.append("\n        bean.method=\"" + method + "\";");
            if (!BaseUtils.isEmpty(url)) {
                buffMain.append("\n        bean.url=\"" + url + "\";");
            }

            if (headers != null && headers.size() > 0) {
                for (final Iterator<String> iter = headers.keySet().iterator(); iter.hasNext();) {
                    final String key = iter.next();
                    final List<String> list = headers.get(key);
                    if (list != null && list.size() > 0) {
                        for (final Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
                            final String val = iterator.next();
                            buffMain.append(
                                    "\n        bean.addHeader(\"" + StringEscapeUtils.escapeJava(key) + "\", \"" + StringEscapeUtils.escapeJava(val) + "\");");
                        }
                    } else {
                        buffMain.append("\n        bean.addHeader(\"" + StringEscapeUtils.escapeJava(key) + "\", \"\");");
                    }
                }
            }

            if (!headersOnly && !BaseUtils.isEmpty(body)) {
                buffMain.append("\n        bean.body=\"" + StringEscapeUtils.escapeJava(body) + "\";");
            }

            buffMain.append("\n        HttpRunner httpRunner = new HttpRunner();");
            buffMain.append("\n        HttpRunner.ResponseReader responseReader = new HttpRunner.ResponseReader() {");
            buffMain.append("\n            public void read(HttpMethod httpMethod) {");
            buffMain.append("\n                try {");
            buffMain.append("\n                    System.out.println(httpMethod.getStatusLine());");
            buffMain.append("\n                    System.out.println(\"\\n\" + httpMethod.getResponseBodyAsString());");
            buffMain.append("\n                } catch (IOException e) {");
            buffMain.append("\n                    e.printStackTrace();");
            buffMain.append("\n                }");
            buffMain.append("\n            }");
            buffMain.append("\n        };");
            buffMain.append("\n        httpRunner.execute(bean, responseReader);");
            buffMain.append("\n    }");
            buffMain.append("\n}");
            buffMain.append("\n/*---------- End of Main class -----------*/");

            final int inx = buff.indexOf(MAINCLASS_POINTER);
            buff.replace(inx, inx + MAINCLASS_POINTER.length(), "");

            buff.insert(inx, buffMain);

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public String getSource() {
        return buff.toString();
    }

    @SuppressWarnings("unchecked")
    public static void main(final String[] args) {

        java.io.InputStream in;
        try {
            in = new java.io.FileInputStream("C:/.tools/eclipse/workspace2/http4e-Core/resources/http-runner-src.txt");
            final byte[] data = new byte[in.available()];
            in.read(data);
            final StringBuilder sb = new StringBuilder(new String(data));

            System.out.println(sb);
            final String mainclass = "mainclass";
            System.out.println(sb.indexOf("mainclass"));
            sb.replace(802, 802 + mainclass.length(), "");
            System.out.println(sb);
//         Map hh = new HashMap();
//         List hhList = new ArrayList();
//         hhList.add("1a");
//         hhList.add("1b");
//         hhList.add("1c");
//         hh.put("h1", hhList);
//         Exported exported = new Exported(in, "GET", "http://asd.com?qe=6", "myBody", hh, null);
//         System.out.println(exported.getSource());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
