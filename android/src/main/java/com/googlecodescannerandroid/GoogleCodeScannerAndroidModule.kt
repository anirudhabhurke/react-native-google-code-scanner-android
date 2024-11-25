package com.googlecodescannerandroid

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.android.gms.common.moduleinstall.*
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState
import android.util.Log

class GoogleCodeScannerAndroidModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    private val moduleInstallClient: ModuleInstallClient by lazy {
        ModuleInstall.getClient(reactContext)
    }

    private var installListener: InstallStatusListener? = null

    override fun getName(): String {
        return NAME
    }

    private var options: GmsBarcodeScannerOptions? = null

    private fun sendEvent(eventName: String, params: Any?) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun initialize(barcodeOptions: ReadableMap?) {
        val builder = GmsBarcodeScannerOptions.Builder()

        if (barcodeOptions != null) {
            var barcodeFormats = listOf<Int>()
            if (barcodeOptions.hasKey("barcodeFormats")) {
                barcodeFormats =
                    barcodeOptions.getArray("barcodeFormats")!!.toArrayList().map { (it as Double).toInt() }
            }
            var enableAutoZoom = false
            if (barcodeOptions.hasKey("enableAutoZoom")) {
                enableAutoZoom = barcodeOptions.getBoolean("enableAutoZoom")
            }
            var allowManualInput = false
            if (barcodeOptions.hasKey("allowManualInput")) {
                allowManualInput = barcodeOptions.getBoolean("allowManualInput")
            }
            if (barcodeFormats.isNotEmpty()) {
                val firstFormat = barcodeFormats[0]
                val restFormats = barcodeFormats.drop(1).map { it as Int }.toIntArray()
                builder.setBarcodeFormats(firstFormat, *restFormats)
            } else {
                builder.setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            }
            if (enableAutoZoom) {
                builder.enableAutoZoom()
            }
            if (allowManualInput) {
                builder.allowManualInput()
            }
        }
        builder.setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        options = builder.build()
    }

    @ReactMethod
    fun checkModuleAvailability(callback: Callback) {
        val optionalModuleApi = GmsBarcodeScanning.getClient(reactContext)
        
        moduleInstallClient.areModulesAvailable(optionalModuleApi)
            .addOnSuccessListener { response ->
                callback.invoke(null, response.areModulesAvailable())
            }
            .addOnFailureListener { e ->
                callback.invoke(e.message, null)
            }
    }

    @ReactMethod
    fun installModule(callback: Callback) {
        val optionalModuleApi = GmsBarcodeScanning.getClient(reactContext)
        
        val listener = object : InstallStatusListener {
            override fun onInstallStatusUpdated(update: ModuleInstallStatusUpdate) {
                update.progressInfo?.let { progressInfo ->
                    try {
                        val totalBytes = progressInfo.totalBytesToDownload
                        val downloadedBytes = progressInfo.bytesDownloaded
                        
                        // Avoid division by zero
                        val progress = if (totalBytes > 0) {
                            (downloadedBytes * 100 / totalBytes).toInt()
                        } else {
                            0
                        }
                        
                        // Create WritableMap for React Native event
                        val progressMap = Arguments.createMap().apply {
                            putInt("progress", progress)
                        }
                        
                        Log.d("GoogleCodeScannerAndroid", "Download progress: $downloadedBytes/$totalBytes ($progress%)")
                        sendEvent("moduleInstallProgress", progressMap)
                    } catch (e: Exception) {
                        Log.e("GoogleCodeScannerAndroid", "Error calculating progress", e)
                    }
                } ?: Log.d("GoogleCodeScannerAndroid", "No progress info available")

                if (isTerminateState(update.installState)) {
                    Log.d("GoogleCodeScannerAndroid", "Install state changed to: ${update.installState}")
                    moduleInstallClient.unregisterListener(this)
                }
            }
        }

        installListener = listener

        val request = ModuleInstallRequest.newBuilder()
            .addApi(optionalModuleApi)
            .setListener(listener)
            .build()

        moduleInstallClient.installModules(request)
            .addOnSuccessListener { response ->
                callback.invoke(null, response.areModulesAlreadyInstalled())
            }
            .addOnFailureListener { e ->
                callback.invoke(e.message, null)
            }
    }

    private fun isTerminateState(@InstallState state: Int): Boolean {
        return state == InstallState.STATE_CANCELED || 
               state == InstallState.STATE_COMPLETED || 
               state == InstallState.STATE_FAILED
    }

    // Example method
    // See https://reactnative.dev/docs/native-modules-android
    @ReactMethod
    fun scan(onBarcodeAvailable: Callback, onError: Callback) {
        if (options == null) {
            throw IllegalStateException("Scanner not initialized")
        }
        val scanner = GmsBarcodeScanning.getClient(this.reactApplicationContext, options!!)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                onBarcodeAvailable.invoke(barcode.rawValue)
            }.addOnCanceledListener {
                onError.invoke("canceled")
            }.addOnFailureListener {
                onError.invoke(it.message ?: "Unknown error")
            }
    }

    companion object {
        const val NAME = "GoogleCodeScannerAndroid"
    }
}
