package com.kilianbatzner.identicons;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import java.util.Random;

/**
 * Class to create circular kaleidoscopic identicons
 * Created by @therealkilian on 15.12.15.
 *
 * Copyright 2014 www.kilians.net <info@kilians.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class IdenticonFactory {

    private static Random random = new Random();

    /**
     * Takes a bitmap with transparent background, colors the foreground and the background and
     * creates a random kaleidoscope out of it. The kaleidoscope is created by cropping a 45° degree
     * slice at a random angle from the circular bitmap and rotating + flipping it to fill the
     * circle.
     *
     * @param original        circular bitmap with shapes drawn on a transparent background. The original
     *                        is not altered by this method.
     * @param foregroundColor color used to fill the shapes on the original with
     * @param backgroundColor color used to fill the circular background
     * @return resulting kaleidoscope
     */
    public static Bitmap createIdenticon(Bitmap original, int foregroundColor, int backgroundColor) {
        // get a slice of the circle as the source for our kaleidoscope
        Bitmap crop = getKaleidoscopeCrop(original, foregroundColor, backgroundColor);
        // transform the slice into a kaleidoscope
        return getKaleidoscope(crop);
    }

    /**
     * Generates the 45° crop from a random angle in the circular bitmap used to create the kaleidoscope.
     *
     * @param original        see {@link #createIdenticon(Bitmap, int, int)}
     * @param foregroundColor see {@link #createIdenticon(Bitmap, int, int)}
     * @param backgroundColor see {@link #createIdenticon(Bitmap, int, int)}
     * @return the resulting slice
     */
    private static Bitmap getKaleidoscopeCrop(Bitmap original, int foregroundColor, int backgroundColor) {
        // get the mask to crop the slice.
        Mask mask = getMask(original);

        Bitmap result = Bitmap.createBitmap(mask.bitmap.getWidth(), mask.bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);
        paint.setColor(foregroundColor);

        // tilt the mask back so that the start point is at (radius, 0)
        canvas.rotate(-mask.tiltAngle, original.getWidth() / 2, original.getHeight() / 2);

        // color the foreground of the bitmap
        canvas.drawBitmap(original, 0, 0, paint);
        canvas.drawColor(foregroundColor, PorterDuff.Mode.SRC_IN);

        // draw a circle in the background
        paint.setColor(backgroundColor);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        canvas.drawCircle(original.getWidth() / 2, original.getHeight() / 2, original.getWidth() / 2, paint);

        // crop the slice using the mask
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(mask.bitmap, 0, 0, paint);
        return result;
    }

    /**
     * Get the mask to crop the slice. The mask comes with a tilt angle, so that the slice is
     * chosen randomly from the circle, but can then be rotated back to 12 o'clock for the
     * subsequent transformations.
     *
     * @param original see {@link #createIdenticon(Bitmap, int, int)}
     * @return a mask containing a triangular bitmap to crop the slice and the angle by which the
     * slice was rotated from 12 o'clock
     */
    private static Mask getMask(Bitmap original) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        // take a random start point at the border of the bitmap and make sure the end is
        // 45 degrees away in reference to the circle's center
        Point start = getStartPoint(original.getWidth());
        Point center = new Point(original.getWidth() / 2, original.getWidth() / 2);
        Point end = getEndPoint(start, center);
        float tiltAngle = (float) getAngle(start, center);

        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(start.x, start.y);
        path.lineTo(end.x, end.y);
        path.lineTo(center.x, center.y);
        path.lineTo(start.x, start.y);
        path.close();

        Bitmap targetBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
        Canvas canvas = new Canvas(targetBitmap);
        canvas.drawPath(path, paint);

        return new Mask(targetBitmap, tiltAngle);
    }

    /**
     * Calculates a end-point based on start and center, so that both start and end lie on the
     * rectangle around the circular bitmap and the angle start-center-end is 45° wide.
     *
     * @param start  start of the slice
     * @param center center of the circular bitmap
     * @return end of the slice, which lies on the border of the bitmap
     */
    private static Point getEndPoint(Point start, Point center) {
        // calculate a point, so that the angle Start-Center-End will be 45° or 0.25*PI
        int diameter = center.x * 2;

        // get the point which is 90° away
        Point otherSide = new Point(0, 0);
        if (start.x == 0) {
            // start on the left
            otherSide.y = 0;
            otherSide.x = diameter - start.y;
        } else if (start.x == diameter) {
            // start on the right
            otherSide.y = diameter;
            otherSide.x = diameter - start.y;
        } else if (start.y == 0) {
            // start on the top
            otherSide.x = diameter;
            otherSide.y = start.x;
        } else {
            // start on the bottom
            otherSide.x = 0;
            otherSide.y = start.x;
        }

        // get the point in the middle of start and otherSide
        Point middle = new Point(otherSide.x + (start.x - otherSide.x) / 2, otherSide.y + (start.y - otherSide.y) / 2);

        // go from the center in the direction of middle until we reach one of the borders
        double dirX = middle.x - center.x;
        double dirY = middle.y - center.y;
        double x, y;

        if (Math.abs(dirX) > Math.abs(dirY)) {
            // we will hit the left / right border
            double steps = Math.abs(diameter / 2 / dirX);
            x = center.x + dirX * steps;
            y = center.y + dirY * steps;
        } else {
            // we will hit the upper / lower border
            double steps = Math.abs(diameter / 2 / dirX);
            x = center.x + dirX * steps;
            y = center.y + dirY * steps;
        }
        return new Point((int) x, (int) y);
    }

    /**
     * Get the angle between a point and 12 o'clock on a circle with center as its center
     * @return angle in [0,360]
     */
    private static double getAngle(Point point, Point center) {
        double topX = center.x;
        double topY = center.y - Math.sqrt(Math.abs(point.x - center.x) * Math.abs(point.x - center.x)
                + Math.abs(point.y - center.y) * Math.abs(point.y - center.y));
        return (2 * Math.atan2(point.y - topY, point.x - topX)) * 180 / Math.PI;
    }

    /**
     * Choose a random point on the border of a square with a certain diameter
     *
     * @param diameter width of the square
     * @return random point on the border
     */
    private static Point getStartPoint(int diameter) {
        // choose a random point on one of the four borders
        int side = random.nextInt(4);
        int x = 0;
        int y = 0;
        switch (side) {
            case 0: // choose from the left border
                x = 0;
                y = random.nextInt(diameter);
                break;
            case 1: // choose from the top border
                x = random.nextInt(diameter);
                y = 0;
                break;
            case 2: // choose from the right border
                x = diameter;
                y = random.nextInt(diameter);
                break;
            case 3: // choose from the bottom border
                x = random.nextInt(diameter);
                y = diameter;
                break;
        }

        return new Point(x, y);
    }

    /**
     * Generate a kaleidoscope out of a given slice of a circle. The slice is rotated 3 times and
     * then flipped horizontally. Each transformation is added to the bitmap.
     *
     * @param crop slice of circle as source for the kaleidoscope
     * @return resulting kaleidoscope
     */
    private static Bitmap getKaleidoscope(Bitmap crop) {
        Bitmap rotated = addRotation(crop, 90, 3);
        return addFlipped(rotated);
    }

    private static Bitmap addRotation(Bitmap original, int degrees, int count) {
        // draw the original
        Bitmap targetBitmap = Bitmap.createBitmap(original);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas canvas = new Canvas(targetBitmap);
        Matrix matrix = new Matrix();
        for (int i = 0; i < count; i++) {
            // rotate the original and draw it
            matrix.postRotate(degrees, original.getWidth() / 2, original.getHeight() / 2);
            canvas.drawBitmap(original, matrix, paint);
        }
        return targetBitmap;
    }

    private static Bitmap addFlipped(Bitmap original) {
        // draw the original
        Bitmap targetBitmap = Bitmap.createBitmap(original);
        Canvas canvas = new Canvas(targetBitmap);
        Matrix matrix = new Matrix();
        // flipping operation:
        matrix.preScale(-1.0f, 1.0f);
        matrix.postTranslate(original.getWidth(), 0);
        canvas.drawBitmap(original, matrix, new Paint());
        return targetBitmap;
    }

    private static class Mask {
        Bitmap bitmap;
        float tiltAngle;

        public Mask(Bitmap bitmap, float tiltAngle) {
            this.bitmap = bitmap;
            this.tiltAngle = tiltAngle;
        }
    }
}
