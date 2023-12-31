package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.Item;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.FolderView;
import me.glindholm.plugin.http4e2.httpclient.core.util.Translator;
import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;

public class ImportPacketAction extends Action {

    private final ViewPart view;
    private Menu fMenu;

    public ImportPacketAction(final ViewPart view) {
        this.view = view;
        fMenu = null;
        setToolTipText("Import raw HTTP packet");
        setText("Import raw HTTP packet");
    }

    public void dispose() {
        // action is reused, can be called several times.
        if (fMenu != null) {
            fMenu.dispose();
            fMenu = null;
        }
    }

    @Override
    public void run() {

        try {

            final ImportDialog dialog = new ImportDialog(view);
            final MouseAdapter okListener = new MouseAdapter() {

                @Override
                public void mouseUp(final MouseEvent e) {
                    Item item = new Item();
                    try {
                        item = Translator.httppacketToItem(dialog.getText());

                    } catch (final Exception exc) {
                        exc.printStackTrace();
                    }

                    final FolderView folderView = ((HdViewPart) view).getFolderView();
                    final ItemModel iModel = new ItemModel(folderView.getModel(), item);
                    final HdViewPart hdViewPart = (HdViewPart) view;
                    hdViewPart.getFolderView().buildTab(iModel);
                }
            };
            dialog.setOkListener(okListener);
            dialog.open();

        } catch (final Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
