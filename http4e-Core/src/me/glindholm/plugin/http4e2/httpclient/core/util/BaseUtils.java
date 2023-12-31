package me.glindholm.plugin.http4e2.httpclient.core.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.FolderModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.Utils;
import me.glindholm.plugin.http4e2.httpclient.core.misc.CoreException;
import me.glindholm.plugin.http4e2.jmeter.ExportSessionsTransformer;
import me.glindholm.plugin.http4e2.jmeter.ExportTemplateTransformer;
import me.glindholm.plugin.http4e2.jmeter.HttpToJmxTransformer;
import me.glindholm.plugin.http4e2.jmeter.LiveHttpHeadersParser;

public class BaseUtils {

    public static String getJavaVersion() {
        String ver = null;
        try {
            ver = "1.0";
            Class.forName("java.lang.Void");
            ver = "1.1";
            Class.forName("java.lang.ThreadLocal");
            ver = "1.2";
            Class.forName("java.lang.StrictMath");
            ver = "1.3";
            Class.forName("java.net.URI");
            ver = "1.4";
            Class.forName("java.util.Scanner");
            ver = "5";
            Class.forName("javax.annotation.processing.Completions");
            ver = "6";
        } catch (final Throwable t) {
        }
        return ver;
    }

    public static void writeToPrefs(final String prefName, final byte[] prefData) {
        try {
            final Plugin pl = (Plugin) CoreContext.getContext().getObject("p");
            final Preferences prefs = pl.getPluginPreferences();

            final String str64 = new String(Base64.getEncoder().encode(prefData), "UTF8");
            prefs.setValue(prefName, str64);
            pl.savePluginPreferences();

        } catch (final Exception ignore) {
            ExceptionHandler.handle(ignore);
        }
    }

    public static byte[] readFromPrefs(final String prefName) {
        try {
            final Plugin pl = (Plugin) CoreContext.getContext().getObject("p");
            final Preferences prefs = pl.getPluginPreferences();
            final String str64 = prefs.getString(prefName);
            final byte[] data = Base64.getDecoder().decode(str64.getBytes("UTF8"));
            return data;

        } catch (final Exception ignore) {
            ExceptionHandler.handle(ignore);
        }
        return null;
    }

    /**
     * Decodes a String from a given charset
     */
    public static String decode(final String text, final String charsetName) {
        final Charset charset = Charset.forName(charsetName);
        final CharsetDecoder decoder = charset.newDecoder();
        ByteBuffer byteBuff;
        try {
            byteBuff = ByteBuffer.wrap(text.getBytes(charsetName));
            return decoder.decode(byteBuff).toString();

        } catch (final CharacterCodingException e) {
            throw new CoreException(CoreException.UNSUPPORTED_ENCODING, "CharacterCodingException", e);
        } catch (final UnsupportedEncodingException ue) {
            throw new CoreException(CoreException.UNSUPPORTED_ENCODING, ue);
        }
    }

    /**
     * Encodes a String to given charset
     */
    public static String encode(final String text, final String charsetName) {
        try {
            final Charset charset = Charset.forName(charsetName);
            final CharsetEncoder encoder = charset.newEncoder();

            final ByteBuffer byteBuff = encoder.encode(CharBuffer.wrap(text));
            return new String(byteBuff.array(), charsetName);

        } catch (final CharacterCodingException e) {
            throw new CoreException(CoreException.UNSUPPORTED_ENCODING, "CharacterCodingException", e);
        } catch (final UnsupportedEncodingException ue) {
            throw new CoreException(CoreException.UNSUPPORTED_ENCODING, ue);
        }
    }

    public static boolean isEmpty(final String str) {
        return str == null || str.trim().equals("");
    }

    public static String noNull(final String str) {
        return str != null ? str : CoreConstants.EMPTY_TEXT;
    }

    public static String noNull(final String str, final String val) {
        return str != null ? str : val;
    }

    public static Properties loadProperties(final String propResource) {
        Properties properties = null;
        if (propResource == null) {
            throw new IllegalArgumentException("propertiesResource not provided !");
        }

        InputStream is = null;
        try {
            try {
                is = new FileInputStream(propResource);

            } catch (final FileNotFoundException e) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(propResource);
            }

            try {
                if (is == null || is.available() < 1) {
                    throw new RuntimeException("Properties '" + propResource + "' not initilized. Skipping..");
                }
                properties = new Properties();
                final InputStreamReader inR = new InputStreamReader(is, "UTF8");
                final BufferedReader bufR = new BufferedReader(inR);
                properties.load(bufR);

            } catch (final IOException ioe) {
                throw new RuntimeException(ioe);
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (final IOException e) {
                }
            }
        }
        return properties;
    }

    public static List<String> loadList(final InputStream is) {

        if (is == null) {
            throw new IllegalArgumentException("InputStream empty.");
        }
        byte[] data;
        try {
            data = new byte[is.available()];
            is.read(data);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final String str = new String(data);
        final StringTokenizer st = new StringTokenizer(str, "\n\r");
        final List<String> result = new ArrayList<>();
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }

    public static boolean isHttp4eIdentifier(final char c) {
        return Character.isJavaIdentifierPart(c) || c == '-';
    }

    public static void writeJMX(final String fileName, final FolderModel folderModel) throws FileNotFoundException {
        final Collection<HttpBean> httpBeans = new ArrayList<>();

        for (final ItemModel iModel : folderModel.getItemModels()) {
            final HttpBean bean = Utils.modelToHttpBean(iModel);
            bean.filterXml();
            httpBeans.add(bean);
        }

        final HttpToJmxTransformer t = new HttpToJmxTransformer("/resources/jmx.vm", httpBeans);

        t.doWrite(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))));

        // write JMX CSV data file
        try {
            final File f = new File(new File(fileName).getParent() + File.separatorChar + "http4e-jmx.csv");
            if (!f.exists()) {
                try (FileWriter fstream = new FileWriter(f);
                        BufferedWriter out = new BufferedWriter(fstream)) {
                    out.write("################################################\n");
                    out.write("## Configure varA, varB entries as bellow\n");
                    out.write("## aa=xxx, bb=xxx\n");
                    out.write("################################################\n");
                    out.write("JSESSIONID=xxxxxxxxxx,userId=1\n");
                    out.write("JSESSIONID=yyyyyyyyyy,userId=2\n");
                    out.write("JSESSIONID=zzzzzzzzzz,userId=3\n");
                }
            }
        } catch (final Exception ignore) {
            //
        }
    }

    public static void writeJavaHttpClient3(final ItemModel iModel, final Writer writer) throws FileNotFoundException {
        final HttpBean bean = Utils.modelToHttpBean(iModel);
        bean.filterJava();
        new ExportTemplateTransformer("/resources/java3.vm", bean).doWrite(writer);
    }

    public static void writeJavaHttpComponent4(final ItemModel iModel, final Writer writer) throws FileNotFoundException {
        final HttpBean bean = Utils.modelToHttpBean(iModel);
        bean.filterJava();
        new ExportTemplateTransformer("/resources/java.vm", bean).doWrite(writer);
    }

    public static void writeJsPrototype(final ItemModel iModel, final Writer writer) throws FileNotFoundException {
        final HttpBean bean = Utils.modelToHttpBean(iModel);
        bean.filterJava();
        new ExportTemplateTransformer("/resources/js-prototype.vm", bean).doWrite(writer);
    }

    public static void writeJsJQuery(final ItemModel iModel, final Writer writer) throws FileNotFoundException {
        final HttpBean bean = Utils.modelToHttpBean(iModel);
        bean.filterJava();
        new ExportTemplateTransformer("/resources/js-jquery.vm", bean).doWrite(writer);
    }

    public static void writeJsXhr(final ItemModel iModel, final Writer writer) throws FileNotFoundException {
        final HttpBean bean = Utils.modelToHttpBean(iModel);
        bean.filterJava();
        new ExportTemplateTransformer("/resources/js-xhr.vm", bean).doWrite(writer);
    }

    public static void writePython(final ItemModel iModel, final Writer writer) throws FileNotFoundException {
        final HttpBean bean = Utils.modelToHttpBean(iModel);
        bean.filterJava();
        new ExportTemplateTransformer("/resources/python.vm", bean).doWrite(writer);
    }

    public static void writeCsharp(final ItemModel iModel, final Writer writer) throws FileNotFoundException {
        final HttpBean bean = Utils.modelToHttpBean(iModel);
        bean.filterJava();
        bean.filterCSharpSpecialHeaders();
        new ExportTemplateTransformer("/resources/csharp.vm", bean).doWrite(writer);
    }

    public static void writeVisualBasic(final ItemModel iModel, final Writer writer) throws FileNotFoundException {
        final HttpBean bean = Utils.modelToHttpBean(iModel);
        bean.filterJava();
        bean.filterCSharpSpecialHeaders();
        new ExportTemplateTransformer("/resources/vb.vm", bean).doWrite(writer);
    }

    public static void writeFlex(final ItemModel iModel, final Writer writer) throws FileNotFoundException {
        final HttpBean bean = Utils.modelToHttpBean(iModel);
        bean.filterJava();
        bean.setMethod(bean.getMethod().toUpperCase());
        final List<String> contentTypeHeader = iModel.getHeaderValuesIgnoreCase("Content-Type");
        if (contentTypeHeader != null && contentTypeHeader.size() > 0) {
            bean.setContentType(contentTypeHeader.get(0));
        }
        new ExportTemplateTransformer("/resources/flex.vm", bean).doWrite(writer);
    }

    public static void writeRuby(final ItemModel iModel, final Writer writer) throws FileNotFoundException {
        final HttpBean bean = Utils.modelToHttpBean(iModel);
        bean.filterJavaSrcipt();
        bean.setMethod(bean.getMethod().toLowerCase());
        new ExportTemplateTransformer("/resources/ruby.vm", bean).doWrite(writer);
    }

    public static void writePHP(final ItemModel iModel, final Writer writer) throws FileNotFoundException {
        final HttpBean bean = Utils.modelToHttpBean(iModel);
        bean.filterJava();
        new ExportTemplateTransformer("/resources/php.vm", bean).doWrite(writer);
    }

    public static void writeObjectiveC(final ItemModel iModel, final Writer writer) throws FileNotFoundException {
        final HttpBean bean = Utils.modelToHttpBean(iModel);
        bean.filterJava();
        if ("GET".equalsIgnoreCase(bean.getMethod())) {
            new ExportTemplateTransformer("/resources/cocoa-get.vm", bean).doWrite(writer);

        } else if ("POST".equalsIgnoreCase(bean.getMethod())) {
            new ExportTemplateTransformer("/resources/cocoa-post.vm", bean).doWrite(writer);

        } else if ("PUT".equalsIgnoreCase(bean.getMethod())) {
            new ExportTemplateTransformer("/resources/cocoa-put.vm", bean).doWrite(writer);

        } else if ("DELETE".equalsIgnoreCase(bean.getMethod())) {
            new ExportTemplateTransformer("/resources/cocoa-delete.vm", bean).doWrite(writer);

        } else {
            try {
                writer.write("\n\n       /* Method not allowed. Only GET, POST, PUT and DELETE are supported. */");
                writer.flush();
                writer.close();
            } catch (final IOException e) {
                // ignore
            }
        }
    }

    public static void writeHttp4eSessionsModel(final Writer writer, final FolderModel folderModel) throws FileNotFoundException {
        final Collection<HttpBean> httpBeans = new ArrayList<>();

        int inx = 0;
        for (final ItemModel iModel : folderModel.getItemModels()) {
            final HttpBean bean = Utils.modelToHttpBean(iModel);
            bean.filterXml();

            bean.setId(inx);
            final List<String> contentTypeHeader = iModel.getHeaderValuesIgnoreCase("Content-Type");
            if (contentTypeHeader != null && contentTypeHeader.size() > 0) {
                bean.setContentType(contentTypeHeader.get(0));
            } else {
                bean.setContentType("");
            }

            httpBeans.add(bean);
            inx++;
        }

        final ExportSessionsTransformer t = new ExportSessionsTransformer("/resources/http4e-sessions.vm", httpBeans);
        t.doWrite(writer);
    }

    public static void writeHttp4eItemModel(final int inx, final ItemModel iModel, final Writer writer) throws FileNotFoundException {
        final HttpBean bean = Utils.modelToHttpBean(iModel);
        bean.setId(inx);
        final List<String> contentTypeHeader = iModel.getHeaderValuesIgnoreCase("Content-Type");
        if (contentTypeHeader != null && contentTypeHeader.size() > 0) {
            bean.setContentType(contentTypeHeader.get(0));
        } else {
            bean.setContentType("");
        }
        bean.setRequest(iModel.getRequest());
        bean.setResponse(iModel.getResponse());
        bean.filterXml();
        new ExportTemplateTransformer("/resources/http4e-item.vm", bean).doWrite(writer);
    }

    public static void writeHttp4eSessions(final String file, final FolderModel folderModel) {

        try {
            final File exportedFile = new File(file);
            final File rawDir = new File(exportedFile.getParent() + File.separator + "raw");
            if (!rawDir.exists()) {
                rawDir.mkdir();
            }
            // String tmpDir =
            // "C:/Users/Mitko/Desktop/tmp/";//System.getProperty("java.io.tmpdir");

            try (FileWriter fileWriter = new FileWriter(new File(exportedFile.getParent() + File.separator + "index-sessions.html"))) {
                BaseUtils.writeHttp4eSessionsModel(fileWriter, folderModel);
            }

            try (FileWriter fstream = new FileWriter(exportedFile.getParent() + File.separator + "index-sessions.txt");
                    BufferedWriter outTxt = new BufferedWriter(fstream)) {
                int inx = 0;
                for (final ItemModel im : folderModel.getItemModels()) {
                    try (FileWriter fileWriter2 = new FileWriter(
                            new File(exportedFile.getParent() + File.separator + "raw" + File.separator + "00" + inx + "_http4e.html"))) {

                        BaseUtils.writeHttp4eItemModel(inx, im, fileWriter2);

                        outTxt.write("\n----------------------------------------------------------\n");
                        outTxt.write(im.getRequest());
                        outTxt.write("\n");
                        outTxt.write(im.getResponse());
                    } catch (final IOException e) {
                        ExceptionHandler.handle(e);
                    }
                    inx++;
                }
            }
            final byte[] data = folderModel.serialize();
            final String str64 = new String(Base64.getEncoder().encode(data), "UTF8");
            try (BufferedWriter out = new BufferedWriter(new FileWriter(exportedFile))) {
                out.write(str64);
            }

        } catch (final IOException e) {
            ExceptionHandler.handle(e);
        }

//      try {
//         FileOutputStream fos = new FileOutputStream(file);
//         ZipOutputStream zos = new ZipOutputStream(fos);
//         ZipEntry ze= new ZipEntry(zipFile.getParent() + File.separator + "index.html");
//         zos.putNextEntry(ze);
//         zos.closeEntry();
//
//         ze= new ZipEntry(rawDir + File.separator + "http4e.ser");
//         zos.putNextEntry(ze);
//         zos.closeEntry();
//
//         inx = 0;
//         for (ItemModel im : folderModel.getItemModels()) {
//            ze= new ZipEntry(zipFile.getParent() + File.separator + "raw" + File.separator + "00" + inx + "_http4e.html");
//            zos.putNextEntry(ze);
//            zos.closeEntry();
//            inx++;
//         }
//         zos.close();
//
//       } catch (FileNotFoundException e) {
//         e.printStackTrace();
//       } catch (IOException e) {
//         e.printStackTrace();
//       }
    }

    public static List<ItemModel> importHttp4eSessions(final String file, final FolderModel folderModel) {
        try {
            final byte[] data = Base64.getDecoder().decode(getContents(new File(file)).getBytes("UTF8"));
            final List<ItemModel> items = new FolderModel(null, null).deserialize(data);

            return items;

        } catch (final Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<ItemModel> importLiveHttpHeaders(final String file, final FolderModel folderModel) {
        try {

            final LiveHttpHeadersParser parser = new LiveHttpHeadersParser();
            parser.parse(file);
            final List<ItemModel> items = new ArrayList<>();
            final Collection<HttpBean> beans = parser.getHttpBeans();
            for (final HttpBean b : beans) {
                final ItemModel iModel = toItemModel(folderModel, b);
                items.add(iModel);
            }
            return items;

        } catch (final Exception e) {
            return new ArrayList<>();
        }
    }

    public static ItemModel toItemModel(final FolderModel folderModel, final HttpBean b) {
        final ItemModel iModel = new ItemModel(folderModel);
        final Map<String, String> headers = b.getHeaders();
        for (final String hKey : headers.keySet()) {
            iModel.addHeader(hKey, headers.get(hKey));
        }

        iModel.setHttpMethod(b.getMethod());
        iModel.setBody(b.getBody());
        iModel.setUrl(b.getUrl());

//      lastBean.setProtocol("https");
//      doMethod(methodBuff.toString(), lastBean);
//      doHeaders(headBuff.toString(), lastBean);
//      doBody(bodyBuff.toString(), lastBean);

        return iModel;
    }

    static public String getContents(final File aFile) {
        // ...checks on aFile are elided
        final StringBuilder contents = new StringBuilder();

        try {
            // use buffering, reading one line at a time
            // FileReader always assumes default encoding is OK!
            final BufferedReader input = new BufferedReader(new FileReader(aFile));
            try (input) {
                String line = null; // not declared within while loop
                /*
                 * readLine is a bit quirky : it returns the content of a line MINUS the newline. it returns null
                 * only for the END of the stream. it returns an empty String if two newlines appear in a row.
                 */
                while ((line = input.readLine()) != null) {
                    contents.append(line);
//               contents.append(System.getProperty("line.separator"));
                }
            }
        } catch (final IOException ex) {
            ExceptionHandler.handle(ex);
        }

        return contents.toString();
    }

    public static void main(final String[] args) {

//    FolderModel folderModel = new FolderModel(null, null);
//    Item item = new Item();
//    item.request = "PUT /helloworld/test/bbb/n/m HTTP/1.1\nContent-Type: application/x-www-form-urlencoded";
//    item.response = "HTTP/1.1 200 OK\nServer: Apache";
//    ItemModel iModel = new ItemModel(folderModel, item);
//    iModel.setBody("aasdsad xd fsdfgsdfg sd sdf sdfg ");
//    iModel.setHttpMethod("POST");
//    iModel.addHeader("Content-type", "text/xml");
//    folderModel.putItem(iModel);
//    iModel.setUrl("http://localhost:8080/helloworld/test/ad/xcbv?aa=1");

//    String file = "C:/Users/Mitko/Desktop/tmp/http4e-sesions.zip";
    }

}
