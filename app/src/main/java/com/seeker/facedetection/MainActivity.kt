package com.seeker.facedetection


import  android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.google.android.material.snackbar.Snackbar
import com.seeker.facedetection.Camera.CameraSourcePreview
import com.seeker.facedetection.Camera.GraphicOverlay
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private var mCameraSource: CameraSource? = null
    private var mGraphicOverlay: GraphicOverlay? = null
    private var customFaceDetector: CustomFaceDetector? =null
    private var mPreview: CameraSourcePreview? = null
    private var mSwitchCam: ImageView? = null
    private var faceResult_Tv: TextView? = null

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_main)
        mPreview = findViewById<View>(R.id.preview) as CameraSourcePreview
        mGraphicOverlay = findViewById<View>(R.id.faceOverlay) as GraphicOverlay
        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(CameraSource.CAMERA_FACING_FRONT)
        } else {
            requestCameraPermission()
        }

        mSwitchCam = findViewById(R.id.switchCam)
        mSwitchCam?.setOnClickListener {
             switchCamera()
        }

        faceResult_Tv = findViewById(R.id.faceResult_Tv)

    }

    private fun switchCamera() {
        /*Switching camera needs releasing and recreating and starting CameraSource*/
        if(mCameraSource?.getCameraFacing()==CameraSource.CAMERA_FACING_FRONT){
            if (mCameraSource != null) {
                mCameraSource?.release();
            }
            createCameraSource(CameraSource.CAMERA_FACING_BACK);
        }
        else{
            if (mCameraSource != null) {
                mCameraSource?.release();
            }
            createCameraSource(CameraSource.CAMERA_FACING_FRONT);
        }

        startCameraSource()
    }

    /*Camera permission above lollipop*/
    private fun requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission")
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }

        val thisActivity = this
        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(
                thisActivity, permissions,
                RC_HANDLE_CAMERA_PERM
            )
        }

        Snackbar.make(
            mGraphicOverlay!!, R.string.permission_camera_rationale,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.ok, listener)
            .show()
    }

    private fun createCameraSource(cameraFacing:Int) {
        val context = applicationContext
        val detector = FaceDetector.Builder(context)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setMode(FaceDetector.ACCURATE_MODE)
            .setProminentFaceOnly(true)
            .build()

        /*Create customFaceDetector to keep track of  Bitmaps*/
        customFaceDetector =
            CustomFaceDetector(detector)
        customFaceDetector!!.setProcessor(
            MultiProcessor.Builder(
                GraphicFaceTrackerFactory(this, mGraphicOverlay!!, mPreview!!,object :faceUpdateListener{
                    override fun onFaceUpdate(face: Face?) {
                        val smilingProbability = String.format("%.2f", face!!.isSmilingProbability)
                        val leftEyeOpenProbability = String.format("%.2f",  face.isLeftEyeOpenProbability)
                        val rightEyeOpenProbability =  String.format("%.2f", face.isRightEyeOpenProbability)


                        runOnUiThread {
                            Handler().post(Runnable {
                                faceResult_Tv!! .setText(
                                        "User Smiling Probability      : "+smilingProbability+"\n"+
                                        "Left Eye Open Probability   : "+leftEyeOpenProbability+"\n"+
                                        "Right Eye Open Probability : "+rightEyeOpenProbability)
                            })

                        }
                    }

                })
            )
                .build()
        )


        if (!detector.isOperational) {
            Log.w(TAG, "Face detector dependencies are not yet available.")
        }
        mCameraSource = CameraSource.Builder(context,  customFaceDetector)
            .setRequestedPreviewSize(640, 480) //Camera preview resolution
            .setFacing(cameraFacing)
            .setRequestedFps(10.0f) //Frame per second : Keep low to avoid performance disaster (If  NW call included)
            .build()
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        mPreview!!.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mCameraSource != null) {
            mCameraSource!!.release()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode)
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source")
            createCameraSource(CameraSource.CAMERA_FACING_FRONT)
            return
        }

        Log.e(
            TAG, "Permission not granted: results len = " + grantResults.size +
                    " Result code = " + if (grantResults.size > 0) grantResults[0] else "(empty)"
        )

        val listener = DialogInterface.OnClickListener { dialog, id -> finish() }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Face Tracker sample")
            .setMessage(R.string.no_camera_permission)
            .setPositiveButton(R.string.ok, listener)
            .show()
    }


    private fun startCameraSource() {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            applicationContext
        )
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }

        if (mCameraSource != null) {
            try {
                mGraphicOverlay?.let {
                    mPreview!!.start(mCameraSource!!, it)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                mCameraSource!!.release()
                mCameraSource = null
            }

        }
    }


    companion object {
        private val TAG = "FaceDetector "
        private val RC_HANDLE_GMS = 9001
        private val RC_HANDLE_CAMERA_PERM = 2
    }
}
