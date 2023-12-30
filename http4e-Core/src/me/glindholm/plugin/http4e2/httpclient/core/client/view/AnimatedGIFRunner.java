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
package me.glindholm.plugin.http4e2.httpclient.core.client.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.misc.CoreException;

/**
 * @author Atanas Roussev (http://nextinterfaces.com)
 */
class AnimatedGIFRunner implements Runnable {

    private final Control control;

    private final GC controlGC;
    private final Color shellBackground;
    private final ImageLoader imageLoader;
    private final ImageData[] imageDataArray;
    private final boolean useGIFBackground = false;
    private Image image;
    private boolean isStopped;
    private final Image backgroundImage;
    private final int positionX;
    private final int positionY;

    AnimatedGIFRunner(final Control control, final ImageLoader imageLoaderGIF, final Image backgroundImage) {

        this.control = control;
        controlGC = new GC(control);
        shellBackground = control.getBackground();
        imageLoader = imageLoaderGIF;
        imageDataArray = imageLoader.data;
        if (imageDataArray == null || imageDataArray.length < 2) {
            throw new RuntimeException("Illegal ImageLoader.");
        }
        isStopped = false;
        this.backgroundImage = backgroundImage;
        final Rectangle ctrR = control.getBounds();
        positionX = (ctrR.width - CoreConstants.IMAGES_HEIGHT) / 2;
        positionY = (ctrR.height - CoreConstants.IMAGES_HEIGHT) / 2;

        control.addPaintListener(e -> {
            final GC graphics = e.gc;
            if (!graphics.isDisposed()) {
                graphics.drawImage(backgroundImage, positionX, positionY);
                graphics.dispose();
            }
        });
    }

    @Override
    public void run() {
        if (imageDataArray.length <= 1) {
            ExceptionHandler.warn("Invalid imageDataArray length.");
            return;
        }

        /*
         * Create an off-screen image to draw on, and fill it with the shell background.
         */
        final Display display = control.getDisplay();
        final Image offScreenImage = new Image(display, imageLoader.logicalScreenWidth, imageLoader.logicalScreenHeight);
        final GC offGC = new GC(offScreenImage);
        offGC.setBackground(shellBackground);
        offGC.fillRectangle(0, 0, imageLoader.logicalScreenWidth, imageLoader.logicalScreenHeight);

        try {
            /*
             * Create the first image and draw it on the off-screen image.
             */
            int imageDataIndex = 0;
            ImageData imageData = imageDataArray[imageDataIndex];
            if (image != null && !image.isDisposed()) {
                image.dispose();
            }

            image = new Image(display, imageData);
            offGC.drawImage(image, 0, 0, imageData.width, imageData.height, imageData.x, imageData.y, imageData.width, imageData.height);
            /*
             * Now loop through the images, creating and drawing each one on the off-screen image before drawing
             * it on the shell.
             */
            int repeatCount = imageLoader.repeatCount;
            while (!isStopped && (imageLoader.repeatCount == 0 || repeatCount > 0)) {
                switch (imageData.disposalMethod) {
                case SWT.DM_FILL_BACKGROUND:
                    /*
                     * Fill with the background color before drawing.
                     */
                    Color bgColor = null;
                    if (useGIFBackground && imageLoader.backgroundPixel != -1) {
                        bgColor = new Color(display, imageData.palette.getRGB(imageLoader.backgroundPixel));
                    }
                    offGC.setBackground(bgColor != null ? bgColor : shellBackground);
                    offGC.fillRectangle(imageData.x, imageData.y, imageData.width, imageData.height);
                    if (bgColor != null) {
                        bgColor.dispose();
                    }
                    break;
                case SWT.DM_FILL_PREVIOUS:
                    /* Restore the previous image before drawing. */
                    offGC.drawImage(image, 0, 0, imageData.width, imageData.height, imageData.x, imageData.y, imageData.width, imageData.height);
                    break;
                }

                imageDataIndex = (imageDataIndex + 1) % imageDataArray.length;
                imageData = imageDataArray[imageDataIndex];
                image.dispose();
                image = new Image(display, imageData);
                offGC.drawImage(image, 0, 0, imageData.width, imageData.height, imageData.x, imageData.y, imageData.width, imageData.height);

                /* Draw the off-screen image to the shell. */
                controlGC.drawImage(offScreenImage, positionX, positionY);

                /*
                 * Sleep for the specified delay time (adding commonly-used slow-down fudge factors).
                 */
                try {
                    int ms = imageData.delayTime * 10;
                    if (ms < 20) {
                        ms += 30;
                    }
                    if (ms < 30) {
                        ms += 10;
                    }
                    Thread.sleep(ms);
                } catch (final InterruptedException e) {
                }

                /*
                 * If we have just drawn the last image, decrement the repeat count and start again.
                 */
                if (imageDataIndex == imageDataArray.length - 1) {
                    repeatCount--;
                }
            }

            // repaint background image
            controlGC.drawImage(backgroundImage, positionX, positionY);

        } catch (final SWTException e) {
            throw new CoreException(CoreException.GIF_ANIMATION_FAILURE, e);

        } finally {
            if (offScreenImage != null && !offScreenImage.isDisposed()) {
                offScreenImage.dispose();
            }
            if (offGC != null && !offGC.isDisposed()) {
                offGC.dispose();
            }
            if (image != null && !image.isDisposed()) {
                image.dispose();
            }
        }
    }

    public void stop() {
        isStopped = true;
    }

}
