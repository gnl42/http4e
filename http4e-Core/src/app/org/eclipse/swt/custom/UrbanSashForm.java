package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;

/**
 * The SashForm lays out its children in a Row or Column arrangement (as specified by the
 * orientation) and places a Sash between the children. One child may be maximized to occupy the
 * entire size of the SashForm. The relative sizes of the children may be specfied using weights.
 * 
 * <p>
 * <dl>
 * <dt><b>Styles:</b>
 * <dd>HORIZONTAL, VERTICAL, SMOOTH
 * </dl>
 */
public class UrbanSashForm extends Composite {

    public int SASH_WIDTH = 3;

    int sashStyle;
    Sash[] sashes = {};
    // Remember background and foreground
    // colors to determine whether to set
    // sashes to the default color (null) or
    // a specific color
    Color background = null;
    Color foreground = null;
    Control[] controls = {};
    Control maxControl = null;
    Listener sashListener;
    static final int DRAG_MINIMUM = 20;

    /**
     * Constructs a new instance of this class given its parent and a style value describing its
     * behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in class <code>SWT</code> which is
     * applicable to instances of this class, or must be built by <em>bitwise OR</em>'ing together (that
     * is, using the <code>int</code> "|" operator) two or more of those <code>SWT</code> style
     * constants. The class description lists the style constants that are applicable to the class.
     * Style bits are also inherited from superclasses.
     * </p>
     * 
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style  the style of widget to construct
     * 
     * @exception IllegalArgumentException
     *                                     <ul>
     *                                     <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *                                     </ul>
     * @exception SWTException
     *                                     <ul>
     *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
     *                                     thread that created the parent</li>
     *                                     </ul>
     * 
     * @see SWT#HORIZONTAL
     * @see SWT#VERTICAL
     * @see #getStyle()
     */
    public UrbanSashForm(final Composite parent, final int style) {
        super(parent, checkStyle(style));
        super.setLayout(new UrbanSashFormLayout());
        sashStyle = (style & SWT.VERTICAL) != 0 ? SWT.HORIZONTAL : SWT.VERTICAL;
        if ((style & SWT.BORDER) != 0) {
            sashStyle |= SWT.BORDER;
        }
        if ((style & SWT.SMOOTH) != 0) {
            sashStyle |= SWT.SMOOTH;
        }
        sashListener = this::onDragSash;
    }

    static int checkStyle(final int style) {
        final int mask = SWT.BORDER | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
        return style & mask;
    }

    /**
     * Returns SWT.HORIZONTAL if the controls in the SashForm are laid out side by side or SWT.VERTICAL
     * if the controls in the SashForm are laid out top to bottom.
     * 
     * @return SWT.HORIZONTAL or SWT.VERTICAL
     */
    @Override
    public int getOrientation() {
        // checkWidget();
        return (sashStyle & SWT.VERTICAL) != 0 ? SWT.HORIZONTAL : SWT.VERTICAL;
    }

    @Override
    public int getStyle() {
        int style = super.getStyle();
        style |= getOrientation() == SWT.VERTICAL ? SWT.VERTICAL : SWT.HORIZONTAL;
        if ((sashStyle & SWT.SMOOTH) != 0) {
            style |= SWT.SMOOTH;
        }
        return style;
    }

    /**
     * Answer the control that currently is maximized in the SashForm. This value may be null.
     * 
     * @return the control that currently is maximized or null
     */
    public Control getMaximizedControl() {
        // checkWidget();
        return maxControl;
    }

    /**
     * Answer the relative weight of each child in the SashForm. The weight represents the percent of
     * the total width (if SashForm has Horizontal orientation) or total height (if SashForm has
     * Vertical orientation) each control occupies. The weights are returned in order of the creation of
     * the widgets (weight[0] corresponds to the weight of the first child created).
     * 
     * @return the relative weight of each child
     * 
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
     *                         created the receiver</li>
     *                         </ul>
     */

    public int[] getWeights() {
        checkWidget();
        final Control[] cArray = getControls(false);
        final int[] ratios = new int[cArray.length];
        for (int i = 0; i < cArray.length; i++) {
            final Object data = cArray[i].getLayoutData();
            if (data != null && data instanceof SashFormData) {
                ratios[i] = (int) (((SashFormData) data).weight * 1000 >> 16);
            } else {
                ratios[i] = 200;
            }
        }
        return ratios;
    }

    Control[] getControls(final boolean onlyVisible) {
        final Control[] children = getChildren();
        Control[] result = {};
        for (final Control child : children) {
            if (child instanceof Sash || onlyVisible && !child.getVisible()) {
                continue;
            }

            final Control[] newResult = new Control[result.length + 1];
            System.arraycopy(result, 0, newResult, 0, result.length);
            newResult[result.length] = child;
            result = newResult;
        }
        return result;
    }

    void onDragSash(final Event event) {
        final Sash sash = (Sash) event.widget;
        int sashIndex = -1;
        for (int i = 0; i < sashes.length; i++) {
            if (sashes[i] == sash) {
                sashIndex = i;
                break;
            }
        }
        if (sashIndex == -1) {
            return;
        }

        final Control c1 = controls[sashIndex];
        final Control c2 = controls[sashIndex + 1];
        final Rectangle b1 = c1.getBounds();
        final Rectangle b2 = c2.getBounds();

        final Rectangle sashBounds = sash.getBounds();
        final Rectangle area = getClientArea();
        boolean correction = false;
        if (getOrientation() == SWT.HORIZONTAL) {
            correction = b1.width < DRAG_MINIMUM || b2.width < DRAG_MINIMUM;
            final int totalWidth = b2.x + b2.width - b1.x;
            final int shift = event.x - sashBounds.x;
            b1.width += shift;
            b2.x += shift;
            b2.width -= shift;
            if (b1.width < DRAG_MINIMUM) {
                b1.width = DRAG_MINIMUM;
                b2.x = b1.x + b1.width + sashBounds.width;
                b2.width = totalWidth - b2.x;
                event.x = b1.x + b1.width;
                event.doit = false;
            }
            if (b2.width < DRAG_MINIMUM) {
                b1.width = totalWidth - DRAG_MINIMUM - sashBounds.width;
                b2.x = b1.x + b1.width + sashBounds.width;
                b2.width = DRAG_MINIMUM;
                event.x = b1.x + b1.width;
                event.doit = false;
            }
            Object data1 = c1.getLayoutData();
            if (data1 == null || !(data1 instanceof SashFormData)) {
                data1 = new SashFormData();
                c1.setLayoutData(data1);
            }
            Object data2 = c2.getLayoutData();
            if (data2 == null || !(data2 instanceof SashFormData)) {
                data2 = new SashFormData();
                c2.setLayoutData(data2);
            }
            ((SashFormData) data1).weight = (((long) b1.width << 16) + area.width - 1) / area.width;
            ((SashFormData) data2).weight = (((long) b2.width << 16) + area.width - 1) / area.width;
        } else {
            correction = b1.height < DRAG_MINIMUM || b2.height < DRAG_MINIMUM;
            final int totalHeight = b2.y + b2.height - b1.y;
            final int shift = event.y - sashBounds.y;
            b1.height += shift;
            b2.y += shift;
            b2.height -= shift;
            if (b1.height < DRAG_MINIMUM) {
                b1.height = DRAG_MINIMUM;
                b2.y = b1.y + b1.height + sashBounds.height;
                b2.height = totalHeight - b2.y;
                event.y = b1.y + b1.height;
                event.doit = false;
            }
            if (b2.height < DRAG_MINIMUM) {
                b1.height = totalHeight - DRAG_MINIMUM - sashBounds.height;
                b2.y = b1.y + b1.height + sashBounds.height;
                b2.height = DRAG_MINIMUM;
                event.y = b1.y + b1.height;
                event.doit = false;
            }
            Object data1 = c1.getLayoutData();
            if (data1 == null || !(data1 instanceof SashFormData)) {
                data1 = new SashFormData();
                c1.setLayoutData(data1);
            }
            Object data2 = c2.getLayoutData();
            if (data2 == null || !(data2 instanceof SashFormData)) {
                data2 = new SashFormData();
                c2.setLayoutData(data2);
            }
            ((SashFormData) data1).weight = (((long) b1.height << 16) + area.height - 1) / area.height;
            ((SashFormData) data2).weight = (((long) b2.height << 16) + area.height - 1) / area.height;
        }
        if (correction || event.doit && event.detail != SWT.DRAG) {
            c1.setBounds(b1);
            sash.setBounds(event.x, event.y, event.width, event.height);
            c2.setBounds(b2);
        }
    }

    /**
     * If orientation is SWT.HORIZONTAL, lay the controls in the SashForm out side by side. If
     * orientation is SWT.VERTICAL, lay the controls in the SashForm out top to bottom.
     * 
     * @param orientation SWT.HORIZONTAL or SWT.VERTICAL
     * 
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
     *                         created the receiver</li>
     *                         <li>ERROR_INVALID_ARGUMENT - if the value of orientation is not
     *                         SWT.HORIZONTAL or SWT.VERTICAL
     *                         </ul>
     */
    @Override
    public void setOrientation(final int orientation) {
        checkWidget();
        if (getOrientation() == orientation) {
            return;
        }
        if (orientation != SWT.HORIZONTAL && orientation != SWT.VERTICAL) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        sashStyle &= ~(SWT.HORIZONTAL | SWT.VERTICAL);
        sashStyle |= orientation == SWT.VERTICAL ? SWT.HORIZONTAL : SWT.VERTICAL;
        for (int i = 0; i < sashes.length; i++) {
            sashes[i].dispose();
            sashes[i] = new Sash(this, sashStyle);
            sashes[i].setBackground(background);
            sashes[i].setForeground(foreground);
            sashes[i].addListener(SWT.Selection, sashListener);
        }
        layout(false);
    }

    @Override
    public void setBackground(final Color color) {
        super.setBackground(color);
        background = color;
        for (final Sash element : sashes) {
            element.setBackground(background);
        }
    }

    @Override
    public void setForeground(final Color color) {
        super.setForeground(color);
        foreground = color;
        for (final Sash element : sashes) {
            element.setForeground(foreground);
        }
    }

    /**
     * Sets the layout which is associated with the receiver to be the argument which may be null.
     * <p>
     * Note: No Layout can be set on this Control because it already manages the size and position of
     * its children.
     * </p>
     * 
     * @param layout the receiver's new layout or null
     * 
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
     *                         created the receiver</li>
     *                         </ul>
     */
    @Override
    public void setLayout(final Layout layout) {
        checkWidget();
        return;
    }

    /**
     * Specify the control that should take up the entire client area of the SashForm. If one control
     * has been maximized, and this method is called with a different control, the previous control will
     * be minimized and the new control will be maximized. If the value of control is null, the SashForm
     * will minimize all controls and return to the default layout where all controls are laid out
     * separated by sashes.
     * 
     * @param control the control to be maximized or null
     * 
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
     *                         created the receiver</li>
     *                         </ul>
     */
    public void setMaximizedControl(final Control control) {
        checkWidget();
        if (control == null) {
            if (maxControl != null) {
                maxControl = null;
                layout(false);
                for (final Sash element : sashes) {
                    element.setVisible(true);
                }
            }
            return;
        }

        for (final Sash element : sashes) {
            element.setVisible(false);
        }
        maxControl = control;
        layout(false);
    }

    /**
     * Specify the relative weight of each child in the SashForm. This will determine what percent of
     * the total width (if SashForm has Horizontal orientation) or total height (if SashForm has
     * Vertical orientation) each control will occupy. The weights must be positive values and there
     * must be an entry for each non-sash child of the SashForm.
     * 
     * @param weights the relative weight of each child
     * 
     * @exception SWTException
     *                         <ul>
     *                         <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                         <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
     *                         created the receiver</li>
     *                         <li>ERROR_INVALID_ARGUMENT - if the weights value is null or of incorrect
     *                         length (must match the number of children)</li>
     *                         </ul>
     */
    public void setWeights(final int[] weights) {
        checkWidget();
        final Control[] cArray = getControls(false);
        if (weights == null || weights.length != cArray.length) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }

        int total = 0;
        for (final int weight : weights) {
            if (weight < 0) {
                SWT.error(SWT.ERROR_INVALID_ARGUMENT);
            }
            total += weight;
        }
        if (total == 0) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        for (int i = 0; i < cArray.length; i++) {
            Object data = cArray[i].getLayoutData();
            if (data == null || !(data instanceof SashFormData)) {
                data = new SashFormData();
                cArray[i].setLayoutData(data);
            }
            ((SashFormData) data).weight = (((long) weights[i] << 16) + total - 1) / total;
        }

        layout(false);
    }
}
