package org.roussev.http4e.httpclient.ui.actions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.ViewPart;
import org.roussev.http4e.httpclient.core.CoreConstants;
import org.roussev.http4e.httpclient.core.CoreContext;
import org.roussev.http4e.httpclient.core.CoreImages;
import org.roussev.http4e.httpclient.core.CoreObjects;
import org.roussev.http4e.httpclient.core.ExceptionHandler;
import org.roussev.http4e.httpclient.core.client.model.ProxyItem;
import org.roussev.http4e.httpclient.core.client.view.FolderView;
import org.roussev.http4e.httpclient.core.util.BaseUtils;
import org.roussev.http4e.httpclient.core.util.ResourceUtils;
import org.roussev.http4e.httpclient.ui.HdViewPart;

public class ProxyDialog extends TitleAreaDialog {

    private Button enabledCheck;
    private Text hostBox;
    private Text portBox;
    private ProxyItem proxyItem;
    private final ViewPart viewPart;

    public ProxyDialog(final ViewPart view) {
        super(view.getViewSite().getShell());
        setTitleImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.LOGO_DIALOG));
        viewPart = view;
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Control contents = super.createContents(parent);
        setTitle("Proxy Configuration");
        setMessage("Proxy Configuration.");

        populate();

        return contents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE | SWT.BORDER);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createControlWidgets(composite);

        return composite;
    }

    private void createControlWidgets(final Composite parent) {

        final Composite composite0 = new Composite(parent, SWT.NONE);
        final GridLayout layout0 = new GridLayout();
        layout0.numColumns = 1;
        composite0.setLayout(layout0);

        final Composite composite = new Composite(composite0, SWT.NONE);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.horizontalSpacing = 10;
        layout.verticalSpacing = 10;
        composite.setLayout(layout);

        final GridData grData = new GridData(GridData.FILL_HORIZONTAL);
        grData.widthHint = 260;

        new Label(composite, SWT.NONE).setText("Host *");
        hostBox = new Text(composite, SWT.BORDER);
        hostBox.setLayoutData(grData);

        new Label(composite, SWT.NONE).setText("Port *");
        portBox = new Text(composite, SWT.BORDER);
        portBox.setLayoutData(grData);

        hostBox.addModifyListener(e -> proxyItem.setHost(hostBox.getText()));

        portBox.addModifyListener(e -> {
            try {
                final int port = Integer.parseInt(portBox.getText());
                proxyItem.setPort(port);
            } catch (final NumberFormatException nfe) {
                portBox.setText("");
            }
        });

        final Group group = new Group(parent, SWT.NONE);
        final GridLayout layout1 = new GridLayout();
        layout1.numColumns = 1;
        layout1.marginHeight = 20;
        layout1.marginWidth = 20;
        group.setLayout(layout1);
        final GridData d1 = new GridData(GridData.FILL_HORIZONTAL);
        group.setLayoutData(d1);
        enabledCheck = new Button(group, SWT.CHECK);
        enabledCheck.setText("Enable Proxy?");

        enabledCheck.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (enabledCheck.getSelection()) {
                    proxyItem.setProxy(true);
                } else {
                    proxyItem.setProxy(false);
                }
            }
        });

    }

    public void populate() {
        final CoreContext ctx = CoreContext.getContext();
        final ProxyItem item = (ProxyItem) ctx.getObject(CoreObjects.PROXY_ITEM);
        try {
            if (item != null) {
                proxyItem = item;
                hostBox.setText(BaseUtils.noNull(item.getHost()));
                portBox.setText("" + item.getPort());
                enabledCheck.setSelection(item.isProxy());

            } else {
                proxyItem = new ProxyItem();
            }
        } catch (final SWTException e) {
            // dispose exception
            ExceptionHandler.handle(e);
        }
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        final Button okBtn = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        okBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                CoreContext.getContext().putObject(CoreObjects.PROXY_ITEM, proxyItem);
                ((HdViewPart) viewPart).fireProxyEnable(proxyItem.isProxy());
                fireExecuteFolderItems();
            }
        });
    }

    private void fireExecuteFolderItems() {
        final IViewSite site = ProxyDialog.this.viewPart.getViewSite();
        final HdViewPart part = (HdViewPart) site.getPart();
        final FolderView folderView = part.getFolderView();
        folderView.enableProxy();
    }

}
