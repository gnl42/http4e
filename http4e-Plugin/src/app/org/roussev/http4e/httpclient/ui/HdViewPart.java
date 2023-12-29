package org.roussev.http4e.httpclient.ui;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.roussev.http4e.httpclient.core.CoreConstants;
import org.roussev.http4e.httpclient.core.CoreContext;
import org.roussev.http4e.httpclient.core.CoreImages;
import org.roussev.http4e.httpclient.core.CoreObjects;
import org.roussev.http4e.httpclient.core.client.model.AuthItem;
import org.roussev.http4e.httpclient.core.client.model.ProxyItem;
import org.roussev.http4e.httpclient.core.client.view.FolderView;
import org.roussev.http4e.httpclient.core.util.ResourceUtils;
import org.roussev.http4e.httpclient.ui.actions.AddTabAction;
import org.roussev.http4e.httpclient.ui.actions.AuthenticationAction;
import org.roussev.http4e.httpclient.ui.actions.DuplicateTabAction;
import org.roussev.http4e.httpclient.ui.actions.ExportMenuAction;
import org.roussev.http4e.httpclient.ui.actions.HelpDropDownAction;
import org.roussev.http4e.httpclient.ui.actions.ImportMenuAction;
import org.roussev.http4e.httpclient.ui.actions.ParameterizeAction;
import org.roussev.http4e.httpclient.ui.actions.ProxyAction;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class HdViewPart extends ViewPart {

    private HdContentViewer viewer;

    public HdViewPart() {
    }

    public FolderView getFolderView() {
        return viewer.getFolderView();
    }

    @Override
    public void createPartControl(final Composite parent) {

        final IActionBars actionBars = getViewSite().getActionBars();
        final IToolBarManager toolbar = actionBars.getToolBarManager();

        final ControlContribution space = new ControlContribution("Space") {

            @Override
            public Control createControl(final Composite parent) {
                final Label space = new Label(parent, SWT.NONE);
                space.setSize(10, 0);
                return space;
            }

            @Override
            protected int computeWidth(final Control control) {
                return 10;
            }
        };

        // ---- TODO add a maginfier control
        // tbmanager.add(new ControlContribution("magn"){
        // public Control createControl( Composite parent){
        // final Label space = new Label(parent, SWT.NONE);
        // space.setSize(20, 0);
        // space.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI,
        // CoreImages.MAGN));
        // return space;
        // }
        // protected int computeWidth( Control control){
        // return 20;
        // }
        // });
        //

        // ---- TODO add a Search|Find search control
        // tbmanager.add(new ControlContribution("Find") {
        // public Control createControl( Composite parent){
        // final Text find = new Text(parent, SWT.NONE);
        // find.setSize(100, 0);
        // find.setToolTipText("Find");
        // find.setForeground(ResourceUtils.getColor(Styles.PROC_INSTR));
        // find.setBackground(ResourceUtils.getColor(Styles.BACKGROUND_DISABLED));
        // return find;
        // }
        // protected int computeWidth( Control control){
        // return 100;
        // }
        // });

        toolbar.add(space);

        final AddTabAction tabAction = new AddTabAction(this);
        tabAction.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.TAB_OPEN)));
        toolbar.add(tabAction);

        final DuplicateTabAction tabDuplicate = new DuplicateTabAction(this);
        tabDuplicate.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.TAB_DUPLICATE)));
        toolbar.add(tabDuplicate);

        final ParameterizeAction parameterizeAction = new ParameterizeAction(this);
        parameterizeAction.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.PARAMETERIZE)));
        toolbar.add(parameterizeAction);

        // TabCloseAction tabCloseAction = new TabCloseAction(this);
        // tabCloseAction.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI,
        // CoreImages.TAB_DEL)));
        // toolbar.add(tabCloseAction);

        final ImportMenuAction importAction = new ImportMenuAction(this);
        importAction.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.IMPORT)));
        toolbar.add(importAction);

        // ExportJavaHttp3Action exportAction = new ExportJavaHttp3Action(this);
        // exportAction.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI,
        // CoreImages.EXPORT)));
        // toolbar.add(exportAction);

        // ToolsAction toolsAction = new ToolsAction(this);
        // toolsAction.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI,
        // CoreImages.TOOLS)));
        // toolbar.add(toolsAction);

        final ExportMenuAction exportMenuAction = new ExportMenuAction(this);
        exportMenuAction.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.EXPORT)));
        toolbar.add(exportMenuAction);

        final AuthenticationAction authAction = new AuthenticationAction(this);
        authAction.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.AUTH)));
        toolbar.add(authAction);

        final ProxyAction proxyAction = new ProxyAction(this);
        proxyAction.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.PROXY)));
        toolbar.add(proxyAction);

        final HelpDropDownAction helpAction = new HelpDropDownAction(this);
        helpAction.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.HELP)));
        helpAction.setEnabled(true);
        toolbar.add(helpAction);

        viewer = new HdContentViewer(parent);

        final CoreContext ctx = CoreContext.getContext();
        final ProxyItem proxyItem = (ProxyItem) ctx.getObject(CoreObjects.PROXY_ITEM);
        final AuthItem authItem = (AuthItem) ctx.getObject(CoreObjects.AUTH_ITEM);
        fireAuthEnable(authItem != null && (authItem.isBasic() || authItem.isDigest()));
        fireProxyEnable(proxyItem != null && proxyItem.isProxy());

        // folderView=new FolderView(frame, new FolderModel());
        parent.addDisposeListener(e -> viewer.doDispose());

        // ToolBarManager tbar = new ToolBarManager(typeViewerToolBar);
        // fTypeViewerViewForm.setTopLeft(typeViewerToolBar);
        // set the filter menu items
        // IActionBars actionBars= getViewSite().getActionBars();
        // IMenuManager viewMenu= actionBars.getMenuManager();
        // for (int i= 0; i < fViewActions.length; i++) {
        // ToggleViewAction action= fViewActions[i];
        // viewMenu.add(action);
        // action.setEnabled(false);
        // }
        // viewMenu.add(new Separator());

        getSite().getShell().addShellListener(new ShellListener() {

            @Override
            public void shellActivated(final ShellEvent e) {
                // folderView.setFocus(true);
            }

            @Override
            public void shellDeactivated(final ShellEvent e) {
                // folderView.setFocus(false);
            }

            @Override
            public void shellClosed(final ShellEvent e) {
            }

            @Override
            public void shellDeiconified(final ShellEvent e) {
            }

            @Override
            public void shellIconified(final ShellEvent e) {
            }
        });
    }

    /**
     * Called when we must grab focus.
     * 
     * @see org.eclipse.ui.part.ViewPart#setFocus
     */
    @Override
    public void setFocus() {
        // folderView.focus();
        viewer.getControl().setFocus();
    }

    /**
     * Called when the View is to be disposed
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    public void fireAuthEnable(final boolean enabled) {
        final IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
        final IContributionItem[] items = toolbar.getItems();

        for (final IContributionItem it : items) {
            if (it instanceof final ActionContributionItem aci) {
                if (aci.getAction() instanceof AuthenticationAction) {
                    final AuthenticationAction aa = (AuthenticationAction) aci.getAction();
                    String img = null;
                    if (enabled) {
                        img = CoreImages.AUTH_ENABLED;
                    } else {
                        img = CoreImages.AUTH;
                    }
                    aa.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, img)));
                }
            }
        }
    }

    public void fireProxyEnable(final boolean enabled) {
        final IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
        final IContributionItem[] items = toolbar.getItems();
        for (final IContributionItem it : items) {
            if (it instanceof final ActionContributionItem aci) {
                if (aci.getAction() instanceof ProxyAction) {
                    final ProxyAction aa = (ProxyAction) aci.getAction();
                    String img = null;
                    if (enabled) {
                        img = CoreImages.PROXY_ENABLED;
                    } else {
                        img = CoreImages.PROXY;
                    }
                    aa.setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, img)));
                }
            }
        }
    }

}