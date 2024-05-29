package com.googlecodescannerandroid

import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class GoogleCodeScannerAndroidModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  private var options: GmsBarcodeScannerOptions? = null

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

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun scan(onBarcodeAvailable: Callback, onError: Callback) {
    if (options == null) {
      throw IllegalStateException("Scanner not initialized")
    }
    val scanner = GmsBarcodeScanning.getClient(this.reactApplicationContext, options!!)
    scanner.startScan().addOnSuccessListener { barcode ->
      onBarcodeAvailable.invoke(barcode.rawValue)
    }.addOnFailureListener {
      onError.invoke(it.message ?: "Unknown error")
    }
  }

  companion object {
    const val NAME = "GoogleCodeScannerAndroid"
  }
}
