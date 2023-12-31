package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;
import me.glindholm.plugin.http4e2.httpclient.ui.HdPlugin;
import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;
import me.glindholm.plugin.http4e2.httpclient.ui.preferences.PreferenceConstants;

public class ImportLiveHttpHeadersAction extends Action {

    private final ViewPart view;
    private Menu fMenu;

    public ImportLiveHttpHeadersAction(final ViewPart view) {
        this.view = view;
        fMenu = null;
        setToolTipText("Import Firefox LiveHTTP Headers");
//      setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.LOGO)));
        setText("Import Firefox LiveHTTP Headers");
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

            final FileDialog fileDialog = new FileDialog(view.getSite().getShell(), SWT.OPEN);

            fileDialog.setFileName("*.*");
            fileDialog.setFilterNames(new String[] { "Import Firefox LiveHTTP Headers *.*" });
            fileDialog.setFilterExtensions(new String[] { "*.*" });
            fileDialog.setText("Import Firefox LiveHTTP Headers");
            fileDialog.setFilterPath(getUserHomeDir());

            final String path = fileDialog.open();
            if (path != null) {
                final HdViewPart hdView = (HdViewPart) view;
                final List<ItemModel> iteModels = BaseUtils.importLiveHttpHeaders(path, hdView.getFolderView().getModel());
                final HdViewPart hdViewPart = (HdViewPart) view;
                for (final ItemModel im : iteModels) {
                    hdViewPart.getFolderView().buildTab(im);
                }

                updateUserHomeDir(path);
            }

        } catch (final Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    private String getUserHomeDir() {
        final IPreferenceStore store = HdPlugin.getDefault().getPreferenceStore();
        final String userDir = store.getString(PreferenceConstants.P_USER_HOME_DIR);
        if (BaseUtils.isEmpty(userDir)) {
            return System.getProperty("user.home");
        }
        return userDir;
    }

    private void updateUserHomeDir(final String userDir) {

        if (!BaseUtils.isEmpty(userDir)) {
            // Updating using Plugin directly persist the entry on each invokation
            // Plugin pl = (Plugin) CoreContext.getContext().getObject("p");
            // Preferences prefs = pl.getPluginPreferences();
            // prefs.setValue(PreferenceConstants.P_USER_HOME_DIR, userDir);
            // pl.savePluginPreferences();

            // Updating the preference "in memory" and persists "on Eclipse exit"
            final IPreferenceStore store = HdPlugin.getDefault().getPreferenceStore();
            store.setValue(PreferenceConstants.P_USER_HOME_DIR, userDir);
        }
    }
}
