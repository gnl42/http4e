package me.glindholm.plugin.http4e2.jmeter;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.slf4j.impl.SimpleLoggerFactory;

import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.util.HttpBean;

public class HttpToJmxTransformer {

    private final String templateFile;
    private final Collection<HttpBean> httpBeans;

    // private boolean parameterizeJSessionIds;

    public HttpToJmxTransformer(final String templateFile, final Collection<HttpBean> httpBeans) {
        this.templateFile = templateFile;
        this.httpBeans = httpBeans;
    }

    public void doWrite(final Writer writer) {
        try (InputStreamReader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(templateFile), StandardCharsets.UTF_8)) {
            RuntimeInstance runtimeInstance = new RuntimeInstance();
            runtimeInstance.init();

            Template template = new Template();
            SimpleNode simpleNode = runtimeInstance.parse(reader, template);

            simpleNode.init(new InternalContextAdapterImpl(new VelocityContext()), runtimeInstance);
            template.setData(simpleNode);

            final VelocityContext context = new VelocityContext();
            context.put("httpbeans", httpBeans);

            template.merge(context, writer);
            /*
             * flush and cleanup
             */

            writer.flush();
            writer.close();
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    // public void doRead(Collection<HttpBean> beans) {
    // System.out.println("Parsing http ...");
    //
    // // LiveHttpHeadersParser t = new LiveHttpHeadersParser();
    // try {
    // // t.parse(fileIn);
    // // Collection<HttpBean> bList = t.getHttpBeans();
    // for (HttpBean b : beans) {
    // // if (b.getHeaders().get("Cookie") == null) {
    // // throw new RuntimeException("'Cookie' is missing..");
    // // }
    // if (parameterizeJSessionIds) {
    // b.getHeaders().put("Cookie", "${jsessionid}");
    // //"${__CSVRead(jsessions.csv,${__counter(,cnt)})}");
    // }
    // System.out.println(b);
    // }
    // httpBeans.addAll(t.getHttpBeans());
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    public static void main(final String[] args) throws FileNotFoundException {

        final Collection<HttpBean> httpBeans = new ArrayList<>();

        final HttpBean bean = new HttpBean();
        bean.setBody("tBody");
        bean.setDomain("www.nextinterfaces.com");
        bean.setMethod("GET");
        bean.setPath("/");
        bean.setProtocol("http");
        httpBeans.add(bean);

        final HttpToJmxTransformer t = new HttpToJmxTransformer("./resources/jmx.vm", httpBeans);

        // t.doRead("C:/packets.http");
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:/http4e1.jmx")));

        t.doWrite(writer);
    }
}
