package com.seeker.facedetection

import android.content.Context
import com.seeker.facedetection.Camera.GraphicOverlay
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.seeker.facedetection.Camera.CameraSourcePreview


class GraphicFaceTrackerFactory(
    var context: Context,
    internal var overlay: GraphicOverlay,
    var mPriview: CameraSourcePreview,
    var listner: faceUpdateListener
) : MultiProcessor.Factory<Face> {
    override fun create(face: Face): Tracker<Face> {
        return GraphicFaceTracker(context,overlay,mPriview,listner)
    }
}