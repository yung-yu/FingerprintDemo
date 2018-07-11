package com.andy.fingerprintdemo

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.support.annotation.RequiresApi
import android.util.Log

class MainActivity : AppCompatActivity() {
	val TAG = "test"
	lateinit var mKeyguardManager: KeyguardManager
	lateinit var mFingerprintManager: FingerprintManager
	lateinit var cancellationSignal: CancellationSignal
	lateinit var dialog :AlertDialog

	@RequiresApi(Build.VERSION_CODES.M)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		 mKeyguardManager = getSystemService(Activity.KEYGUARD_SERVICE) as KeyguardManager;
		 mFingerprintManager = getSystemService(Activity.FINGERPRINT_SERVICE) as FingerprintManager;//FingerprintManager.class

		if (!mKeyguardManager.isKeyguardSecure()){
			return;
		}

		if (checkSelfPermission(Manifest.permission.USE_FINGERPRINT)
				== PackageManager.PERMISSION_GRANTED) //In SDK 23, we need to check the permission before we call FingerprintManager API functionality.
		{
			if (!mFingerprintManager.isHardwareDetected()){
				//硬體裝置是否有 fingerprint reader
				return;
			}

			if (!mFingerprintManager.hasEnrolledFingerprints()){

				return;
			}
			startFingerprintListening()
		}
	}

	override fun onPause() {
		super.onPause()

	}

	@RequiresApi(Build.VERSION_CODES.M)
	private fun startFingerprintListening() {
		cancellationSignal = CancellationSignal()

		if (checkSelfPermission(Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED)
		//In SDK 23, we need to check the permission before we call FingerprintManager API functionality.
		{
			mFingerprintManager.authenticate(//callback 用來接收 authenticate 成功與否，有三個 callback method
					null, //crypto objects 的 wrapper class，可以透過它讓 authenticate 過程更為安全，但也可以不使用。
					cancellationSignal, //用來取消 authenticate 的 object
					0, //optional flags; should be 0
					mAuthenticationCallback, null) //optional 的參數，如果有使用，FingerprintManager 會透過它來傳遞訊息
			dialog = AlertDialog.Builder(this)
					.setTitle("指紋辨識")
					.setMessage("請開始指紋辨識")
					.create()
			dialog.setOnCancelListener {
				cancellationSignal.cancel()
			}
			dialog.show()
		}
	}


	var mAuthenticationCallback: FingerprintManager.AuthenticationCallback = object : FingerprintManager.AuthenticationCallback() {
		override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
			Log.e(TAG, "error $errorCode $errString")
			dialog.setMessage(errString)
		}

		override fun onAuthenticationFailed() {
			Log.e(TAG, "onAuthenticationFailed")
			dialog.setMessage("指紋辨識失敗")
		}

		@RequiresApi(Build.VERSION_CODES.M)
		override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
			Log.d(TAG, result.toString())
			result.cryptoObject
			dialog.setMessage("指紋辨識成功\n")
			dialog.cancel()
		}
	}

}
