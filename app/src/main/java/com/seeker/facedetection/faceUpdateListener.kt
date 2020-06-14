package com.seeker.facedetection

import com.google.android.gms.vision.face.Face


interface faceUpdateListener {
    fun  onFaceUpdate(face: Face?)

}
