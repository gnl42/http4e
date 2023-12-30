/*
 *  Copyright 2017 Eclipse HttpClient (http4e) http://nextinterfaces.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.glindholm.plugin.http4e2.httpclient.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.FolderView;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.shared.PrinterFacade;
import me.glindholm.plugin.http4e2.httpclient.ui.HdViewPart;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
public class PrintAction extends Action {

    private final ViewPart view;

    public PrintAction(final ViewPart view, final String title) {
        this.view = view;

        setText(title);
        setImageDescriptor(ImageDescriptor.createFromImage(ResourceUtils.getImage(CoreConstants.PLUGIN_UI, CoreImages.HELP)));
        // setDisabledImageDescriptor(PDEPluginImages.DESC_PLUGIN_OBJ);

        setDescription("Print Http Packet");
        setToolTipText("Print Http Packet");
    }

    @Override
    public void run() {
        print(view.getViewSite().getShell());
    }

    void print(final Shell shell) {
        final PrintDialog printDialog = new PrintDialog(shell, SWT.NONE);
        printDialog.setText("Print Http Packets");
        final PrinterData printerData = printDialog.open();
        if (!(printerData == null)) {
            final Printer printer = new Printer(printerData);

            final Thread printingThread = new Thread("Printing") {
                @Override
                public void run() {
                    try {
                        new PrinterFacade(getTextToPrint(), printer).print();
                    } catch (final Exception e) {
                        ExceptionHandler.handle(e);
                    } finally {
                        printer.dispose();
                    }
                }
            };
            printingThread.start();
        }
    }

    private String getTextToPrint() {
        final StringBuilder sb = new StringBuilder();

        final FolderView folderView = ((HdViewPart) view).getFolderView();
        final ItemModel itemModel = folderView.getModel().getItemModel(folderView.getSelectionItemHash());
        sb.append("----------HTTP Request:---------\n");
        sb.append(itemModel.getRequest());
        sb.append("\n\n----------HTTP Response:--------\n");
        sb.append(itemModel.getResponse());

        return sb.toString();
    }

}
