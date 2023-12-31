package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import java.io.StringWriter;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.FolderView;
import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;
import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;

public class ExportCocoaAction extends Action {

    private final ViewPart view;
    private Menu fMenu;

    public ExportCocoaAction(final ViewPart view) {
        this.view = view;
        fMenu = null;
        setToolTipText("Export call as Cocoa");
        setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.OC)));
        setText("     Cocoa");
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
            BaseUtils.writeObjectiveC(iModel, writer);
            
            final ExportDialog dialog = new ExportDialog(view, "Cocoa REST client", "Your call exported as Cocoa/Objective-C HTTP client call.",
                    writer.toString());
            dialog.open();
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
        }
    }

}
