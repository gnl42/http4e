package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Sash;

class UrbanSashFormLayout extends Layout {
    @Override
    protected Point computeSize(final Composite composite, final int wHint, final int hHint, final boolean flushCache) {
        System.out.println("UrbanSashFormLayout . computeSize() .. ");
        final UrbanSashForm sashForm = (UrbanSashForm) composite;
        final Control[] cArray = sashForm.getControls(true);
        int width = 0;
        int height = 0;
        if (cArray.length == 0) {
            if (wHint != SWT.DEFAULT) {
                width = wHint;
            }
            if (hHint != SWT.DEFAULT) {
                height = hHint;
            }
            return new Point(width, height);
        }
        // determine control sizes
        final boolean vertical = sashForm.getOrientation() == SWT.VERTICAL;
        int maxIndex = 0;
        int maxValue = 0;
        for (int i = 0; i < cArray.length; i++) {
            if (vertical) {
                final Point size = cArray[i].computeSize(wHint, SWT.DEFAULT, flushCache);
                if (size.y > maxValue) {
                    maxIndex = i;
                    maxValue = size.y;
                }
                width = Math.max(width, size.x);
            } else {
                final Point size = cArray[i].computeSize(SWT.DEFAULT, hHint, flushCache);
                if (size.x > maxValue) {
                    maxIndex = i;
                    maxValue = size.x;
                }
                height = Math.max(height, size.y);
            }
        }
        // get the ratios
        final long[] ratios = new long[cArray.length];
        long total = 0;
        for (int i = 0; i < cArray.length; i++) {
            Object data = cArray[i].getLayoutData();
            if (data != null && data instanceof SashFormData) {
                ratios[i] = ((SashFormData) data).weight;
            } else {
                data = new SashFormData();
                cArray[i].setLayoutData(data);
                ((SashFormData) data).weight = ratios[i] = ((200 << 16) + 999) / 1000;

            }
            total += ratios[i];
        }
        if (ratios[maxIndex] > 0) {
            final int sashwidth = sashForm.sashes.length > 0 ? sashForm.SASH_WIDTH + sashForm.sashes[0].getBorderWidth() * 2 : sashForm.SASH_WIDTH;
            if (vertical) {
                height += (int) (total * maxValue / ratios[maxIndex]) + (cArray.length - 1) * sashwidth;
            } else {
                width += (int) (total * maxValue / ratios[maxIndex]) + (cArray.length - 1) * sashwidth;
            }
        }
        width += sashForm.getBorderWidth() * 2;
        height += sashForm.getBorderWidth() * 2;
        if (wHint != SWT.DEFAULT) {
            width = wHint;
        }
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        }
        return new Point(width, height);
    }

    @Override
    protected boolean flushCache(final Control control) {
        return true;
    }

    @Override
    protected void layout(final Composite composite, final boolean flushCache) {
        final UrbanSashForm sashForm = (UrbanSashForm) composite;
        final Rectangle area = sashForm.getClientArea();
        if (area.width <= 1 || area.height <= 1) {
            return;
        }

        final Control[] newControls = sashForm.getControls(true);
        if (sashForm.controls.length == 0 && newControls.length == 0) {
            return;
        }
        sashForm.controls = newControls;

        final Control[] controls = sashForm.controls;

        if (sashForm.maxControl != null && !sashForm.maxControl.isDisposed()) {
            for (final Control control : controls) {
                if (control != sashForm.maxControl) {
                    control.setBounds(-200, -200, 0, 0);
                } else {
                    control.setBounds(area);
                }
            }
            return;
        }

        // keep just the right number of sashes
        if (sashForm.sashes.length < controls.length - 1) {
            final Sash[] newSashes = new Sash[controls.length - 1];
            System.arraycopy(sashForm.sashes, 0, newSashes, 0, sashForm.sashes.length);
            for (int i = sashForm.sashes.length; i < newSashes.length; i++) {
                newSashes[i] = new Sash(sashForm, sashForm.sashStyle);
                newSashes[i].setBackground(sashForm.background);
                newSashes[i].setForeground(sashForm.foreground);
                newSashes[i].addListener(SWT.Selection, sashForm.sashListener);
            }
            sashForm.sashes = newSashes;
        }
        if (sashForm.sashes.length > controls.length - 1) {
            if (controls.length == 0) {
                for (final Sash element : sashForm.sashes) {
                    element.dispose();
                }
                sashForm.sashes = new Sash[0];
            } else {
                final Sash[] newSashes = new Sash[controls.length - 1];
                System.arraycopy(sashForm.sashes, 0, newSashes, 0, newSashes.length);
                for (int i = controls.length - 1; i < sashForm.sashes.length; i++) {
                    sashForm.sashes[i].dispose();
                }
                sashForm.sashes = newSashes;
            }
        }
        if (controls.length == 0) {
            return;
        }
        final Sash[] sashes = sashForm.sashes;
        // get the ratios
        final long[] ratios = new long[controls.length];
        long total = 0;
        for (int i = 0; i < controls.length; i++) {
            Object data = controls[i].getLayoutData();
            if (data != null && data instanceof SashFormData) {
                ratios[i] = ((SashFormData) data).weight;
            } else {
                data = new SashFormData();
                controls[i].setLayoutData(data);
                ((SashFormData) data).weight = ratios[i] = ((200 << 16) + 999) / 1000;

            }
            total += ratios[i];
        }

        final int sashwidth = sashes.length > 0 ? sashForm.SASH_WIDTH + sashes[0].getBorderWidth() * 2 : sashForm.SASH_WIDTH;
        if (sashForm.getOrientation() == SWT.HORIZONTAL) {
            int width = (int) (ratios[0] * (area.width - sashes.length * sashwidth) / total);
            int x = area.x;
            controls[0].setBounds(x, area.y, width, area.height);
            x += width;
            for (int i = 1; i < controls.length - 1; i++) {
                sashes[i - 1].setBounds(x, area.y, sashwidth, area.height);
                x += sashwidth;
                width = (int) (ratios[i] * (area.width - sashes.length * sashwidth) / total);
                controls[i].setBounds(x, area.y, width, area.height);
                x += width;
            }
            if (controls.length > 1) {
                sashes[sashes.length - 1].setBounds(x, area.y, sashwidth, area.height);
                x += sashwidth;
                width = area.width - x;
                controls[controls.length - 1].setBounds(x, area.y, width, area.height);
            }
        } else {
            int height = (int) (ratios[0] * (area.height - sashes.length * sashwidth) / total);
            int y = area.y;
            controls[0].setBounds(area.x, y, area.width, height);
            y += height;
            for (int i = 1; i < controls.length - 1; i++) {
                sashes[i - 1].setBounds(area.x, y, area.width, sashwidth);
                y += sashwidth;
                height = (int) (ratios[i] * (area.height - sashes.length * sashwidth) / total);
                controls[i].setBounds(area.x, y, area.width, height);
                y += height;
            }
            if (controls.length > 1) {
                sashes[sashes.length - 1].setBounds(area.x, y, area.width, sashwidth);
                y += sashwidth;
                height = area.height - y;
                controls[controls.length - 1].setBounds(area.x, y, area.width, height);
            }

        }
    }
}