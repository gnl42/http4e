package me.glindholm.plugin.http4e2.httpclient.core.client.view;

import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreImages;
import me.glindholm.plugin.http4e2.httpclient.core.CoreMessages;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.AssistConstants;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.AssistUtils;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.DocumentUtils;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.HConfiguration;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.HContentAssistProcessor;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.ModelTrackerListener;
import me.glindholm.plugin.http4e2.httpclient.core.misc.Styles;
import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.ResourceUtils;

public abstract class AbstractView {
    protected final StyledText styledText;
    final ItemModel model;
    protected ParamsAttachManager attachManager;
    private final Composite parent;
    private final IControlView textView;
    private final CLabel titleLabel;

    public AbstractView(final String title, final boolean autoComplete, final boolean allowAttachment, final ItemModel model, final Composite parent) {
        this.model = model;
        this.parent = parent;
        final ViewForm vForm = toolbarHeader(title, model, parent, allowAttachment);
        titleLabel = (CLabel) vForm.getChildren()[0];
        textView = new ParamTextView(model, vForm);
        styledText = buildEditorText(vForm);
        vForm.setContent(styledText);

        final Display display = parent.getDisplay();
        final Color fg = ResourceUtils.getColor(Styles.BLUE_RGB_TEXT);
        final Color bg = ResourceUtils.getColor(Styles.BLACK_RGB_TEXT);
        final Color a = parent.getDisplay().getSystemColor(SWT.FOREGROUND);
        final Color b = parent.getForeground();
//        fg = b;
//        styledText.setForeground(fg);
        final Color systemColor = display.getSystemColor(SWT.COLOR_YELLOW);
        styledText.setForeground(systemColor);
        styledText.setBackground(bg);

        final int eventResized;
        final int eventFocusGained;
        final int eventFocusLost;
        if (title.equals(CoreConstants.TITLE_HEADERS)) {
            eventResized = ModelEvent.HEADERS_RESIZED;
            eventFocusGained = ModelEvent.HEADERS_FOCUS_GAINED;
            eventFocusLost = ModelEvent.HEADERS_FOCUS_LOST;
        } else if (title.equals(CoreConstants.TITLE_PARAMETERS)) {
            eventResized = ModelEvent.PARAMS_RESIZED;
            eventFocusGained = ModelEvent.PARAMS_FOCUS_GAINED;
            eventFocusLost = ModelEvent.PARAMS_FOCUS_LOST;
        } else if (title.equals(CoreConstants.TITLE_BODY)) {
            eventResized = ModelEvent.BODY_RESIZED;
            eventFocusGained = ModelEvent.BODY_FOCUS_GAINED;
            eventFocusLost = ModelEvent.BODY_FOCUS_LOST;
        } else {
            throw new RuntimeException("invalid title: " + title);
        }

        styledText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(final MouseEvent e) {
                model.fireExecute(new ModelEvent(eventResized, model));
            }
        });

        styledText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent e) {
                model.fireExecute(new ModelEvent(eventFocusGained, model));
            }

            @Override
            public void focusLost(final FocusEvent e) {
                model.fireExecute(new ModelEvent(eventFocusLost, model));
            }
        });

        // /////////////////////////////////
        final Menu popupMenu = new Menu(styledText);
        new ClipboardMenu(styledText, popupMenu);
        styledText.setMenu(popupMenu);

        styledText.addKeyListener(new ExecuteKeyListener(() -> model.fireExecute(new ModelEvent(ModelEvent.REQUEST_START, model))));

        // new MenuItem(popupMenu, SWT.SEPARATOR);
        //
        // MenuItem ctItem = new MenuItem(popupMenu, SWT.CASCADE);
        // ctItem.setText("Content-Type");
        // Menu contypeMenu = new Menu(ctItem);
        // ctItem.setMenu(contypeMenu);
        //
        // MenuItem hItem = new MenuItem(popupMenu, SWT.CASCADE);
        // hItem.setText("Add Header");
        //
        // Menu headersMenu = new Menu(hItem);
        // hItem.setMenu(headersMenu);
        // MenuItem h1 = new MenuItem(headersMenu, SWT.PUSH);
        // h1.setText("Accept");

        // Listener copyListener = new Listener() {
        // public void handleEvent( Event event){
        // if (event.character == '\u0003') {
        // Clipboard clipboard = new Clipboard(Display.getDefault());
        // TextTransfer transfer = TextTransfer.getInstance();
        // String text = (String) clipboard.getContents(transfer);
        // System.out.println("clipboard contents: " + text);
        // clipboard.dispose();
        // }
        // }
        // };
        // styledText.addListener(SWT.KeyDown, copyListener);
        // /////////////////////////////////
    }

    private StyledText buildEditorText(final Composite parent) {
        final SourceViewer sourceViewer = new SourceViewer(parent, null, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);

        final HConfiguration sourceConf = new HConfiguration(HContentAssistProcessor.HEADER_PROCESSOR);
        sourceViewer.configure(sourceConf);
        sourceViewer.setDocument(DocumentUtils.createDocument1());

        sourceViewer.getControl().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (Utils.isAutoAssistInvoked(e)) {
                    final IContentAssistant ca = sourceConf.getContentAssistant(sourceViewer);
                    ca.showPossibleCompletions();
                }
            }
        });

        sourceViewer.addTextListener(e -> {
            final ModelTrackerListener trackerListener = (key, value) -> {
                key = BaseUtils.noNull(key).trim();
                if (key.equalsIgnoreCase(AssistConstants.HEADER_CONTENT_TYPE)) {
                    model.fireExecute(new ModelEvent(ModelEvent.CONTENT_TYPE_CHANGE, model));
                } else {
                    model.fireExecute(new ModelEvent(ModelEvent.PARAMS_FOCUS_LOST, model));
                }
            };
            AssistUtils.addTrackWords(e.getText(), sourceViewer.getDocument(), e.getOffset() - 1, trackerListener);
        });

        return sourceViewer.getTextWidget();
    }

    private ViewForm toolbarHeader(final String title, final ItemModel model, final Composite parent, final boolean allowAttachment) {
        final ViewForm vForm = ViewUtils.buildViewForm(title, model, parent);

        final ToolBar bar = new ToolBar(vForm, SWT.FLAT);

        final ToolItem clearBtn = new ToolItem(bar, SWT.PUSH);
        clearBtn.setImage(ResourceUtils.getImage(CoreConstants.PLUGIN_CORE, CoreImages.DELETE));
        clearBtn.setToolTipText("Clear");
        clearBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected(final SelectionEvent e) {
            }

            @Override
            public void widgetSelected(final SelectionEvent e) {
                styledText.setText("");
            }
        });
        vForm.setTopCenter(bar);
        if (allowAttachment) {
            attachManager = new ParamsAttachManager(model, styledText, bar);
            vForm.setContent(styledText);

        }
        return vForm;
    }

    String getViewText() {
        if (CoreMessages.HEADER_DEFAULTS.equals(styledText.getText())) {
            return CoreConstants.EMPTY_TEXT;
        }
        return styledText.getText();
    }

    String getText() {
        return styledText.getText();
    }

    void setFocus(final boolean focusGained) {
        if (focusGained) {
            if (CoreConstants.EMPTY_TEXT.equals(styledText.getText())) {
                styledText.setText(CoreConstants.EMPTY_TEXT);
            }
//            styledText.setForeground(ResourceUtils.getColor(Styles.BLACK_RGB_TEXT));
        }
    }

    void setText(final String txt) {
        if (CoreConstants.EMPTY_TEXT.equals(txt)) {
            styledText.setText(CoreMessages.PARAM_DEFAULTS);
        } else {
            styledText.setText(txt);
        }
//        if (CoreConstants.EMPTY_TEXT.equals(getViewText())) {
//            styledText.setForeground(ResourceUtils.getColor(Styles.LIGHT_RGB_TEXT));
//        } else {
//            styledText.setForeground(ResourceUtils.getColor(Styles.BLACK_RGB_TEXT));
//        }
    }

    void setEditable(final boolean editable) {
        setEditable(editable, false, false);
    }

    void setEditable(final boolean editable, final boolean isXwwwForm, final boolean isPost) {
        Color fg = ResourceUtils.getColor(Styles.GRAY_RGB_TEXT);
        Color bg = ResourceUtils.getColor(Styles.BACKGROUND_DISABLED);
        fg = bg = null;

        if (editable) {
            // bodyText.setFont(ResourceUtils.getFont(Styles.FONT_COURIER));
            styledText.setForeground(fg);
            styledText.setBackground(bg);
            styledText.setEditable(true);
            // attachManager.setEnabled(true);
            if (isXwwwForm && isPost) {
                attachManager.setEnabled(false);
            } else {
                attachManager.setEnabled(true);
            }
            // if(isXwwForm && isPost){
            // attachManager.setEnabled(false);
            // } else {
            // attachManager.setEnabled(true);
            // }

        } else {
            // bodyText.setFont(ResourceUtils.getFont(Styles.FONT_COURIER));
            styledText.setForeground(fg);
            styledText.setBackground(ResourceUtils.getColor(Styles.GREY_DISABLED));
            styledText.setEditable(false);
            attachManager.setEnabled(false);
        }
    }

    void setForeground(final RGB style) {
        final Color fg = ResourceUtils.getColor(Styles.BLACK_RGB_TEXT);
        final Color bg = ResourceUtils.getColor(Styles.BACKGROUND_DISABLED);

//        styledText.setForeground(fg);
    }

    public void setMultipart(final boolean isMultipart) {
        attachManager.setMultipart(isMultipart);
    }

    boolean isEditable() {
        return styledText.getEditable();
    }

    CLabel getTitleLabel() {
        return titleLabel;
    }

    void setBackground(final Color color) {
//        styledText.setBackground(color);
    }

}
