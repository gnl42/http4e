package org.roussev.http4e.httpclient.ui.actions;

import java.io.StringWriter;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;
import org.roussev.http4e.httpclient.core.CoreConstants;
import org.roussev.http4e.httpclient.core.CoreImages;
import org.roussev.http4e.httpclient.core.ExceptionHandler;
import org.roussev.http4e.httpclient.core.client.model.ItemModel;
import org.roussev.http4e.httpclient.core.client.model.ModelEvent;
import org.roussev.http4e.httpclient.core.client.view.FolderView;
import org.roussev.http4e.httpclient.core.util.BaseUtils;
import org.roussev.http4e.httpclient.core.util.ResourceUtils;
import org.roussev.http4e.httpclient.ui.HdViewPart;

public class ExportPythonAction extends Action {

    private final ViewPart view;
    private Menu fMenu;

    public ExportPythonAction(final ViewPart view) {
        this.view = view;
        fMenu = null;
        setToolTipText("Export call as Python");
        setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.PYTHON)));
        setText("     Python");
    }

    public void dispose() {
        // action is reused, can be called several times.
        if (fMenu != null) {
            fMenu.dispose();
            fMenu = null;
        }
    }

    // protected void addActionToMenu( Menu parent, Action action){
    // ActionContributionItem item = new ActionContributionItem(action);
    // item.fill(parent, -1);
    // }

    @Override
    public void run() {
        try {
            final FolderView folderView = ((HdViewPart) view).getFolderView();
            final ItemModel iModel = folderView.getModel().getItemModel(folderView.getSelectionItemHash());
            iModel.fireExecute(new ModelEvent(ModelEvent.EXPORT, iModel));

            final StringWriter writer = new StringWriter();
            BaseUtils.writePython(iModel, writer);

            final ExportDialog dialog = new ExportDialog(view, "Python Http client", "Your call exported as a Python Http client code.", writer.toString());
            dialog.open();
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
        }
    }

}
