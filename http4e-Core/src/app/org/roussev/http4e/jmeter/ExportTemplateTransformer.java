package org.roussev.http4e.jmeter;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.roussev.http4e.httpclient.core.ExceptionHandler;
import org.roussev.http4e.httpclient.core.util.HttpBean;

public class ExportTemplateTransformer {

    private final String templateFile;
    private final HttpBean httpBean;

    public ExportTemplateTransformer(final String templateFile, final HttpBean httpBean) {
        this.templateFile = templateFile;
        this.httpBean = httpBean;
    }

    public void doWrite(final Writer writer) {
        try {

            final Properties p = new Properties();

//          p.setProperty("resource.loader", "file");
////          p.setProperty("file.resource.loader.path", "./src");
//          p.setProperty("file.resource.loader.class",
//          "org.apache.velocity.runtime.resource.loader.FileResourceLoader");

            p.setProperty("resource.loader", "class");
            p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

            Velocity.init(p);

            final VelocityContext context = new VelocityContext();
            context.put("httpBean", httpBean);

            Template template = null;

            try {
                template = Velocity.getTemplate(templateFile);
            } catch (final ResourceNotFoundException | ParseErrorException e) {
                ExceptionHandler.handle(e);
            }

            if (template != null) {
                template.merge(context, writer);
            }
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
