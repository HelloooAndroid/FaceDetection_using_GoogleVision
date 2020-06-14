package com.seeker.facedetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.io.ByteArrayOutputStream;

class CustomFaceDetector extends Detector<Face> {
    private Detector<Face> mDelegate;
    private Bitmap mCurrentBitmap;

    CustomFaceDetector(Detector<Face> delegate) {
        mDelegate = delegate;
    }

    public SparseArray<Face> detect(Frame frame) {
        YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, frame.getMetadata().getWidth(), frame.getMetadata().getHeight(), null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, frame.getMetadata().getWidth(), frame.getMetadata().getHeight()), 100, byteArrayOutputStream);
        byte[] jpegArray = byteArrayOutputStream.toByteArray();

        if (mCurrentBitmap != null) {
            mCurrentBitmap.recycle();
        }
        mCurrentBitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);

        return mDelegate.detect(frame);
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }

    public Bitmap getCurrentFrame() {
        return mCurrentBitmap;
    }


}