package org.eclipse.swt.custom;

/*******************************************************************************
 * Copyright (c) 2004 Stefan Zeiger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.novocode.com/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Original SashForm implementation
 *     Stefan Zeiger (szeiger@novocode.com) - Bug fixes and new features
 *******************************************************************************/

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;

/**
 * Like a regular SashForm, the LiveSashForm lays out its children in a Row or Column arrangement
 * (as specified by the orientation) and places a Sash between the children. One child may be
 * maximized to occupy the entire size of the LiveSashForm. The relative sizes of the children may
 * be specfied using weights.
 * <p>
 * LiveSashForm supports the following additional features:
 * <ul>
 * <li>Unless the LiveSashForm is created with the NO_LIVE_UPDATE style, the bounds of the children
 * are updated in real time when a Sash is being dragged.</li>
 * <li>Borders can be assigned to the individual child widgets by calling
 * <code>setChildBorder()</code>. This has the same effect as placing the child widgets in
 * FramedComposites except the borders become part of the adjacent sashes, thus making the draggable
 * part bigger without consuming additional screen real estate.</li>
 * </ul>
 * </p>
 * <p>
 * A Selection event is fired when the weights have changed.
 * </p>
 * <p>
 * <dl>
 * <dt><b>Styles:</b>
 * <dd>HORIZONTAL, VERTICAL, LiveSashForm.NO_LIVE_UPDATE</dd>
 * <dt><b>Events:</b>
 * <dd>Selection</dd>
 * </dl>
 * </p>
 *
 * @since Feb 15, 2004
 * @version $Id: LiveSashForm.java,v 1.19 2005/06/04 19:21:45 szeiger Exp $
 */

public class LiveSashForm extends Composite {
    /**
     * Style constant for disabling live update behavior (value is 1&lt;&lt;1).
     * <p>
     * <b>Used By:</b>
     * <ul>
     * <li><code>LiveSashForm</code></li>
     * </ul>
     * </p>
     */

    public static final int NO_LIVE_UPDATE = 1 << 1;

    public int sashWidth = 3;
    public int dragMinimum = 20;
    private final boolean liveUpdate;

    private int orientation = SWT.HORIZONTAL;
    private Control[] sashes = {};
    // Remember background and foreground
    // colors to determine whether to set
    // sashes to the default color (null) or
    // a specific color
    private Color background = null;
    private Color foreground = null;
    private Control[] controls = {};
    private Control maxControl = null;
    private final Listener sashListener;
    private final static String LAYOUT_RATIO = LiveSashForm.class.getName() + ".layoutRatio";
    private final static String CHILD_SHADOW = LiveSashForm.class.getName() + ".childShadow";

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
     * @see #NO_LIVE_UPDATE
     */

    public LiveSashForm(final Composite parent, final int style) {
        super(parent, checkStyle(style));

        liveUpdate = (style & NO_LIVE_UPDATE) == 0;

        if ((style & SWT.VERTICAL) != 0) {
            orientation = SWT.VERTICAL;
        }

        addListener(SWT.Resize, e -> {
            layout(true);
            weightsChanged();
            // redraw();
        });

        sashListener = e -> {
            onDragSash(e);
            redraw();
            if (e.detail != SWT.DRAG) {
                weightsChanged();
            }
        };

        addListener(SWT.Paint, this::onPaint);
    }

    private static int checkStyle(final int style) {
        final int mask = SWT.BORDER | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT | SWT.SHADOW_OUT;
        return style & mask | SWT.NO_BACKGROUND;
    }

    private void weightsChanged() {
        final Event e = new Event();
        e.widget = this;
        e.type = SWT.Selection;
        notifyListeners(SWT.Selection, e);
    }

    @Override
    public Point computeSize(final int wHint, final int hHint, final boolean changed) {
        checkWidget();
        final Control[] cArray = getControls(true);
        if (cArray.length == 0) {
            return new Point(wHint, hHint);
        }

        final int sashwidth = sashes.length > 0 ? sashWidth + sashes[0].getBorderWidth() * 2 : sashWidth;
        int width = 0;
        int height = 0;
        final boolean vertical = orientation == SWT.VERTICAL;
        if (vertical) {
            height += (cArray.length - 1) * sashwidth;
        } else {
            width += (cArray.length - 1) * sashwidth;
        }
        for (final Control element : cArray) {
            final int childShadow = getChildBorder(element);
            final int childShadowSize = shadowSizeForShadow(childShadow);

            if (vertical) {
                final Point size = element.computeSize(wHint, SWT.DEFAULT);
                height += size.y + 2 * childShadowSize;
                width = Math.max(width, size.x + 2 * childShadowSize);
            } else {
                final Point size = element.computeSize(SWT.DEFAULT, hHint);
                width += size.x + 2 * childShadowSize;
                height = Math.max(height, size.y + 2 * childShadowSize);
            }
        }
        if (wHint != SWT.DEFAULT) {
            width = wHint;
        }
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        }

        return new Point(width, height);
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
        return orientation;
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
            final Long ratio = (Long) cArray[i].getData(LAYOUT_RATIO);
            if (ratio != null) {
                ratios[i] = (int) (ratio.longValue() * 1000 >> 16);
            } else {
                ratios[i] = 200;
            }
        }
        return ratios;
    }

    private Control[] getControls(final boolean onlyVisible) {
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

    @Override
    public void layout(final boolean changed) {
        checkWidget();
        final Rectangle area = getClientArea();
        if (area.width == 0 || area.height == 0) {
            return;
        }

        final Control[] newControls = getControls(true);
        if (controls.length == 0 && newControls.length == 0) {
            return;
        }
        controls = newControls;

        if (maxControl != null && !maxControl.isDisposed()) {
            for (final Control control : controls) {
                if (control != maxControl) {
                    control.setBounds(-200, -200, 0, 0);
                } else {
                    final int sh = shadowSizeForShadow(getChildBorder(control));
                    control.setBounds(area.x + sh, area.y + sh, area.width - 2 * sh, area.height - 2 * sh);
                }
            }
            return;
        }

        // keep just the right number of sashes
        if (sashes.length < controls.length - 1) {
            final Control[] newSashes = new Control[controls.length - 1];
            System.arraycopy(sashes, 0, newSashes, 0, sashes.length);
            for (int i = sashes.length; i < newSashes.length; i++) {
                newSashes[i] = createSash();
            }
            sashes = newSashes;
        }
        if (sashes.length > controls.length - 1) {
            if (controls.length == 0) {
                for (final Control element : sashes) {
                    element.dispose();
                }
                sashes = new Control[0];
            } else {
                final Control[] newSashes = new Control[controls.length - 1];
                System.arraycopy(sashes, 0, newSashes, 0, newSashes.length);
                for (int i = controls.length - 1; i < sashes.length; i++) {
                    sashes[i].dispose();
                }
                sashes = newSashes;
            }
        }

        if (controls.length == 0) {
            return;
        }

        final int sashwidth = sashes.length > 0 ? sashWidth + sashes[0].getBorderWidth() * 2 : sashWidth;
        // get the ratios
        final long[] ratios = new long[controls.length];
        long total = 0;
        for (int i = 0; i < controls.length; i++) {
            final Long ratio = (Long) controls[i].getData(LAYOUT_RATIO);
            if (ratio != null) {
                ratios[i] = ratio;
            } else {
                ratios[i] = ((200 << 16) + 999) / 1000;
            }
            total += ratios[i];
        }

        if (orientation == SWT.HORIZONTAL) {
            total += (((long) (sashes.length * sashwidth) << 16) + area.width - 1) / area.width;
        } else {
            total += (((long) (sashes.length * sashwidth) << 16) + area.height - 1) / area.height;
        }

        if (orientation == SWT.HORIZONTAL) {
            int width = (int) (ratios[0] * area.width / total);
            int x = area.x;
            int sh = shadowSizeForShadow(getChildBorder(controls[0]));
            controls[0].setBounds(x + sh, area.y + sh, width - 2 * sh, area.height - 2 * sh);
            x += width;
            for (int i = 1; i < controls.length - 1; i++) {
                final int prevSh = sh;
                sh = shadowSizeForShadow(getChildBorder(controls[i]));
                sashes[i - 1].setBounds(x - prevSh, area.y, sashwidth + prevSh + sh, area.height);
                x += sashwidth;
                width = (int) (ratios[i] * area.width / total);
                controls[i].setBounds(x + sh, area.y + sh, width - 2 * sh, area.height - 2 * sh);
                x += width;
            }
            if (controls.length > 1) {
                final int prevSh = sh;
                sh = shadowSizeForShadow(getChildBorder(controls[controls.length - 1]));
                sashes[sashes.length - 1].setBounds(x - prevSh, area.y, sashwidth + prevSh + sh, area.height);
                x += sashwidth;
                width = area.width - x;
                controls[controls.length - 1].setBounds(x + sh, area.y + sh, width - 2 * sh, area.height - 2 * sh);
            }
        } else {
            int height = (int) (ratios[0] * area.height / total);
            int y = area.y;
            int sh = shadowSizeForShadow(getChildBorder(controls[0]));
            controls[0].setBounds(area.x + sh, y + sh, area.width - 2 * sh, height - 2 * sh);
            y += height;
            for (int i = 1; i < controls.length - 1; i++) {
                final int prevSh = sh;
                sh = shadowSizeForShadow(getChildBorder(controls[i]));
                sashes[i - 1].setBounds(area.x, y - prevSh, area.width, sashwidth + prevSh + sh);
                y += sashwidth;
                height = (int) (ratios[i] * area.height / total);
                controls[i].setBounds(area.x + sh, y + sh, area.width - 2 * sh, height - 2 * sh);
                y += height;
            }
            if (controls.length > 1) {
                final int prevSh = sh;
                sh = shadowSizeForShadow(getChildBorder(controls[controls.length - 1]));
                sashes[sashes.length - 1].setBounds(area.x, y - prevSh, area.width, sashwidth + prevSh + sh);
                y += sashwidth;
                height = area.height - y;
                controls[controls.length - 1].setBounds(area.x + sh, y + sh, area.width - 2 * sh, height - 2 * sh);
            }

        }
    }

    private void onPaint(final Event e) {
        if (maxControl != null && !maxControl.isDisposed()) {
            for (final Control control : controls) {
                if (control == maxControl) {
                    drawBorderAround(control, e.gc);
                }
            }
        } else {
            for (final Control control : controls) {
                drawBorderAround(control, e.gc);
            }
        }
    }

    private void drawBorderAround(final Control c, final GC gc) {
        final int sh = getChildBorder(c);
        if (sh == SWT.SHADOW_NONE) {
            return;
        }

        final Display disp = getDisplay();
        final Color shadow = disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
        final Color highlight = disp.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
        if (shadow == null || highlight == null) {
            return;
        }
        final Rectangle r = c.getBounds();

        switch (sh) {
        case SWT.SHADOW_IN:
            drawBevelRect(gc, r.x - 1, r.y - 1, r.width + 1, r.height + 1, shadow, highlight);
            break;

        case SWT.SHADOW_OUT:
            drawBevelRect(gc, r.x - 1, r.y - 1, r.width + 1, r.height + 1, highlight, shadow);
            break;

        case SWT.SHADOW_ETCHED_IN:
            drawBevelRect(gc, r.x - 1, r.y - 1, r.width + 1, r.height + 1, highlight, shadow);
            drawBevelRect(gc, r.x - 2, r.y - 2, r.width + 3, r.height + 3, shadow, highlight);
            break;

        case SWT.SHADOW_ETCHED_OUT:
            drawBevelRect(gc, r.x - 1, r.y - 1, r.width + 1, r.height + 1, shadow, highlight);
            drawBevelRect(gc, r.x - 2, r.y - 2, r.width + 3, r.height + 3, highlight, shadow);
            break;
        }
    }

    private static void drawBevelRect(final GC gc, final int x, final int y, final int w, final int h, final Color topleft, final Color bottomright) {
        gc.setForeground(bottomright);
        gc.drawLine(x + w, y, x + w, y + h);
        gc.drawLine(x, y + h, x + w, y + h);

        gc.setForeground(topleft);
        gc.drawLine(x, y, x + w - 1, y);
        gc.drawLine(x, y, x, y + h - 1);
    }

    private void onDragSash(final Event event) {
        final Control sash = (Control) event.widget;
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
        final int sh1 = shadowSizeForShadow(getChildBorder(c1));
        final int sh2 = shadowSizeForShadow(getChildBorder(c2));

        if (event.detail == SWT.DRAG) {
            final Rectangle area = getClientArea();
            if (orientation == SWT.HORIZONTAL) {
                event.x = Math.min(Math.max(dragMinimum - sh1, event.x), area.width - dragMinimum - sashWidth - sh1);
            } else {
                event.y = Math.min(Math.max(dragMinimum - sh1, event.y), area.height - dragMinimum - sashWidth - sh1);
            }
            if (!liveUpdate) {
                return;
            }
        }

        Rectangle b1 = c1.getBounds();
        b1 = new Rectangle(b1.x - sh1, b1.y - sh1, b1.width + 2 * sh1, b1.height + 2 * sh1);
        Rectangle b2 = c2.getBounds();
        b2 = new Rectangle(b2.x - sh2, b2.y - sh2, b2.width + 2 * sh2, b2.height + 2 * sh2);

        final Rectangle sashBounds = sash.getBounds();
        final Rectangle area = getClientArea();

        if (orientation == SWT.HORIZONTAL) {
            final int shift = event.x - sashBounds.x;
            b1.width += shift;
            b2.x += shift;
            b2.width -= shift;

            final Long c1new = (((long) b1.width << 16) + area.width - 1) / area.width;

            if (c1new.equals(c1.getData(LAYOUT_RATIO))) {
                return;
            }

            c1.setData(LAYOUT_RATIO, c1new);
            c2.setData(LAYOUT_RATIO, Long.valueOf((((long) b2.width << 16) + area.width - 1) / area.width));
        } else {
            final int shift = event.y - sashBounds.y;
            b1.height += shift;
            b2.y += shift;
            b2.height -= shift;

            final Long c1new = (((long) b1.height << 16) + area.height - 1) / area.height;

            if (c1new.equals(c1.getData(LAYOUT_RATIO))) {
                return;
            }

            c1.setData(LAYOUT_RATIO, c1new);
            c2.setData(LAYOUT_RATIO, Long.valueOf((((long) b2.height << 16) + area.height - 1) / area.height));
        }

        c1.setBounds(b1.x + sh1, b1.y + sh1, b1.width - 2 * sh1, b1.height - 2 * sh1);
        sash.setBounds(event.x, event.y, event.width, event.height);
        c2.setBounds(b2.x + sh2, b2.y + sh2, b2.width - 2 * sh2, b2.height - 2 * sh2);
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
        if (this.orientation == orientation) {
            return;
        }
        if (orientation != SWT.HORIZONTAL && orientation != SWT.VERTICAL) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.orientation = orientation;

        for (int i = 0; i < sashes.length; i++) {
            sashes[i].dispose();
            sashes[i] = createSash();
        }
        layout();
        redraw();
        weightsChanged();
    }

    @Override
    public void setBackground(final Color color) {
        super.setBackground(color);
        background = color;
        for (final Control element : sashes) {
            element.setBackground(background);
        }
    }

    @Override
    public void setForeground(final Color color) {
        super.setForeground(color);
        foreground = color;
        for (final Control element : sashes) {
            element.setForeground(foreground);
        }
    }

    @Override
    public void setLayout(final Layout layout) {
        checkWidget();
    }

    /**
     * Specify the control that should take up the entire client area of the SashForm. If one control
     * has been maximized, and this method is called with a different control, the previous control will
     * be minimized and the new control will be maximized.. if the value of control is null, the
     * SashForm will minimize all controls and return to the default layout where all controls are laid
     * out separated by sashes.
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
                layout();
                redraw();
                weightsChanged();
                for (final Control element : sashes) {
                    element.setVisible(true);
                }
            }
            return;
        }

        for (final Control element : sashes) {
            element.setVisible(false);
        }
        maxControl = control;
        layout();
        redraw();
        weightsChanged();
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
            cArray[i].setData(LAYOUT_RATIO, Long.valueOf((((long) weights[i] << 16) + total - 1) / total));
        }

        layout();
        redraw();
        weightsChanged();
    }

    private Control createSash() {
        final int sashStyle = orientation == SWT.HORIZONTAL ? SWT.VERTICAL : SWT.HORIZONTAL;

        Control c;
        /*
         * if(liveUpdate) c = new SashCanvas(this, sashStyle); else
         */ c = new Sash(this, sashStyle);
        final Control sash = c;

        sash.setBackground(background);
        sash.setForeground(foreground);
        sash.addListener(SWT.Selection, sashListener);

        sash.addListener(SWT.Resize, event -> sash.redraw());

        sash.addListener(SWT.Paint, event -> {
            final GC gc = new GC(sash);

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
            final int sh1 = getChildBorder(c1);
            final int sh2 = getChildBorder(c2);

            final boolean vertical = orientation == SWT.VERTICAL;
            final Display disp = getDisplay();
            gc.setLineWidth(1);
            final Color shadow = disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
            final Color highlight = disp.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
            final Point p = sash.getSize();

            switch (sh1) {
            case SWT.SHADOW_IN:
                gc.setForeground(highlight);
                if (vertical) {
                    gc.drawLine(0, 0, p.x - 1, 0);
                } else {
                    gc.drawLine(0, 0, 0, p.y - 1);
                }
                break;

            case SWT.SHADOW_OUT:
                gc.setForeground(shadow);
                if (vertical) {
                    gc.drawLine(0, 0, p.x - 1, 0);
                } else {
                    gc.drawLine(0, 0, 0, p.y - 1);
                }
                break;

            case SWT.SHADOW_ETCHED_IN:
                gc.setForeground(highlight);
                if (vertical) {
                    gc.drawLine(0, 1, p.x - 1, 1);
                    gc.drawPoint(p.x - 1, 0);
                    gc.setForeground(shadow);
                    gc.drawLine(0, 0, p.x - 2, 0);
                } else {
                    gc.drawLine(1, 0, 1, p.y - 1);
                    gc.drawPoint(0, p.y - 1);
                    gc.setForeground(shadow);
                    gc.drawLine(0, 0, 0, p.y - 2);
                }
                break;

            case SWT.SHADOW_ETCHED_OUT:
                gc.setForeground(shadow);
                if (vertical) {
                    gc.drawLine(0, 1, p.x - 1, 1);
                    gc.drawPoint(p.x - 1, 0);
                    gc.setForeground(highlight);
                    gc.drawLine(0, 0, p.x - 2, 0);
                } else {
                    gc.drawLine(1, 0, 1, p.y - 1);
                    gc.drawPoint(0, p.y - 1);
                    gc.setForeground(highlight);
                    gc.drawLine(0, 0, 0, p.y - 2);
                }
                break;
            }

            switch (sh2) {
            case SWT.SHADOW_IN:
                gc.setForeground(highlight);
                if (vertical) {
                    gc.drawPoint(p.x - 1, p.y - 1);
                    gc.setForeground(shadow);
                    gc.drawLine(0, p.y - 1, p.x - 2, p.y - 1);
                } else {
                    gc.drawPoint(p.x - 1, p.y - 1);
                    gc.setForeground(shadow);
                    gc.drawLine(p.x - 1, 0, p.x - 1, p.y - 2);
                }
                break;

            case SWT.SHADOW_OUT:
                gc.setForeground(shadow);
                if (vertical) {
                    gc.drawPoint(p.x - 1, p.y - 1);
                    gc.setForeground(highlight);
                    gc.drawLine(0, p.y - 1, p.x - 2, p.y - 1);
                } else {
                    gc.drawPoint(p.x - 1, p.y - 1);
                    gc.setForeground(highlight);
                    gc.drawLine(p.x - 1, 0, p.x - 1, p.y - 2);
                }
                break;

            case SWT.SHADOW_ETCHED_IN:
                gc.setForeground(highlight);
                if (vertical) {
                    gc.drawLine(p.x - 1, p.y - 2, p.x - 1, p.y - 1);
                    gc.drawLine(1, p.y - 1, p.x - 3, p.y - 1);
                    gc.setForeground(shadow);
                    gc.drawLine(0, p.y - 2, p.x - 2, p.y - 2);
                    gc.drawPoint(0, p.y - 1);
                    gc.drawPoint(p.x - 2, p.y - 1);
                } else {
                    gc.drawLine(p.x - 2, p.y - 1, p.x - 1, p.y - 1);
                    gc.drawLine(p.x - 1, 1, p.x - 1, p.y - 3);
                    gc.setForeground(shadow);
                    gc.drawLine(p.x - 2, 0, p.x - 2, p.y - 2);
                    gc.drawPoint(p.x - 1, 0);
                    gc.drawPoint(p.x - 1, p.y - 2);
                }
                break;

            case SWT.SHADOW_ETCHED_OUT:
                gc.setForeground(shadow);
                if (vertical) {
                    gc.drawLine(p.x - 1, p.y - 2, p.x - 1, p.y - 1);
                    gc.drawLine(1, p.y - 1, p.x - 3, p.y - 1);
                    gc.setForeground(highlight);
                    gc.drawLine(0, p.y - 2, p.x - 2, p.y - 2);
                    gc.drawPoint(0, p.y - 1);
                    gc.drawPoint(p.x - 2, p.y - 1);
                } else {
                    gc.drawLine(p.x - 2, p.y - 1, p.x - 1, p.y - 1);
                    gc.drawLine(p.x - 1, 1, p.x - 1, p.y - 3);
                    gc.setForeground(highlight);
                    gc.drawLine(p.x - 2, 0, p.x - 2, p.y - 2);
                    gc.drawPoint(p.x - 1, 0);
                    gc.drawPoint(p.x - 1, p.y - 2);
                }
                break;
            }

            gc.dispose();
        });

        return sash;
    }

    /**
     * Set the border for a child control.
     * 
     * @param child  The child control for which to set the border.
     * @param shadow One of the SWT shadow constants SHADOW_IN, SHADOW_OUT, SHADOW_ETCHED_IN,
     *               SHADOW_ETCHED_OUT, SHADOW_NONE.
     */

    public void setChildBorder(final Control child, final int shadow) {
        checkWidget();
        if (child == null) {
            return;
        }
        if (shadow == SWT.SHADOW_NONE) {
            child.setData(CHILD_SHADOW, null);
        } else {
            child.setData(CHILD_SHADOW, Integer.valueOf(shadow));
        }
        layout();
        redraw();
        // weightsChanged();
    }

    /**
     * Get the border for a child control.
     * 
     * @param child The child control for which to get the border.
     * @return One of the SWT shadow constants SHADOW_IN, SHADOW_OUT, SHADOW_ETCHED_IN,
     *         SHADOW_ETCHED_OUT, SHADOW_NONE.
     */

    public int getChildBorder(final Control child) {
        checkWidget();
        if (child == null) {
            return SWT.SHADOW_NONE;
        }
        final Object o = child.getData(CHILD_SHADOW);
        if (o == null) {
            return SWT.SHADOW_NONE;
        } else {
            return (Integer) o;
        }
    }

    private int shadowSizeForShadow(final int shadow) {
        return switch (shadow) {
        case SWT.SHADOW_IN, SWT.SHADOW_OUT -> 1;
        case SWT.SHADOW_ETCHED_IN, SWT.SHADOW_ETCHED_OUT -> 2;
        default -> 0;
        };
    }
}
