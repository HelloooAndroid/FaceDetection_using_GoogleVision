package com.seeker.facedetection

import android.content.Context
import com.seeker.facedetection.Camera.GraphicOverlay
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.seeker.facedetection.Camera.CameraSourcePreview


class GraphicFaceTracker internal constructor(val context: Context,private val mOverlay: GraphicOverlay,val mPriview:CameraSourcePreview, var listner: faceUpdateListener) : Tracker<Face>() {
    private val mFaceGraphic: FaceGraphic

    init {
        mFaceGraphic = FaceGraphic(mOverlay)
    }

    override fun onNewItem(faceId: Int, item: Face?) {
        mFaceGraphic.setId(faceId)
    }

    override fun onUpdate(p0: Detector.Detections<Face>?, face: Face?) {
        mOverlay.add(mFaceGraphic)
        face?.let { mFaceGraphic.updateFace(it) }
        listner.onFaceUpdate(face)
    }




    override fun onMissing(p0: Detector.Detections<Face>?) {
        mOverlay.remove(mFaceGraphic)
    }

    override fun onDone() {
        mOverlay.remove(mFaceGraphic)
    }
}