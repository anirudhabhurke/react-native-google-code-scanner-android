import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-google-code-scanner-android' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const GoogleCodeScannerAndroid = NativeModules.GoogleCodeScannerAndroid
  ? NativeModules.GoogleCodeScannerAndroid
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

/**
 * Initializes the scanner.
 * This function should be called before using any other functions or components from the library.
 *
 * @param options - Optional configuration options for the scanner.
 * @param options.barcodeFormats - An array of barcode formats to scan. Default is [BarcodeFormat.FORMAT_ALL_FORMATS].
 * @param options.enableAutoZoom - Enable auto zoom. Default is false.
 * @param options.allowManualInput - Allow manual input. Default is false.
 */
export function initialize(options?: ReactNativeGoogleCodeScannerOptions) {
  if (Platform.OS !== 'android') {
    console.error(
      'react-native-google-code-scanner-android is only available on Android devices'
    );
    return;
  }
  return GoogleCodeScannerAndroid.initialize(options);
}

/**
 *
 * @param onBarcodeAvailable - gets the barcode string from the scanner
 * @param onError - gets the error message from the scanner.
 *
 * Note: You may get the following error message for the first time, while downloading the app outside of play store - "Waiting for the Barcode UI module to be downloaded."
 */
export function scan(onBarcodeAvailable: OnBarcodeAvailable, onError: OnError) {
  if (Platform.OS !== 'android') {
    console.error(
      'react-native-google-code-scanner-android is only available on Android devices'
    );
    return;
  }
  return GoogleCodeScannerAndroid.scan(onBarcodeAvailable, onError);
}

export interface ReactNativeGoogleCodeScannerOptions {
  barcodeFormats?: BarcodeFormat[];
  enableAutoZoom?: boolean;
  allowManualInput?: boolean;
}

export enum BarcodeFormat {
  FORMAT_UNKNOWN = -1,
  FORMAT_ALL_FORMATS = 0,
  FORMAT_CODE_128 = 1,
  FORMAT_CODE_39 = 2,
  FORMAT_CODE_93 = 4,
  FORMAT_CODABAR = 8,
  FORMAT_DATA_MATRIX = 16,
  FORMAT_EAN_13 = 32,
  FORMAT_EAN_8 = 64,
  FORMAT_ITF = 128,
  FORMAT_QR_CODE = 256,
  FORMAT_UPC_A = 512,
  FORMAT_UPC_E = 1024,
  FORMAT_PDF417 = 2048,
  FORMAT_AZTEC = 4096,
}

type OnBarcodeAvailable = (barcode: string) => void;

type OnError = (error: string) => void;
