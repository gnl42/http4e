package me.glindholm.plugin.http4e2.jmeter;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.util.HttpBean;

public class ExportTemplateTransformer {

    private final String templateFile;
    private final HttpBean httpBean;

    public ExportTemplateTransformer(final String templateFile, final HttpBean httpBean) {
        this.templateFile = templateFile;
        this.httpBean = httpBean;
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
            context.put("httpBean", httpBean);

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

    public static void main(final String[] args) {

        new ExportTemplateTransformer("/resources/http4e-item.vm", new HttpBean()).doWrite(new StringWriter());
    }
}
