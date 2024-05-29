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

export function initialize(options?: ReactNativeGoogleCodeScannerOptions) {
  if (Platform.OS !== 'android') {
    console.error(
      'react-native-google-code-scanner-android is only available on Android devices'
    );
    return;
  }
  return GoogleCodeScannerAndroid.initialize(options);
}

export function scan(
  onBarcodeAvailable: (barcode: string) => void,
  onError: (error: string) => void
) {
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
