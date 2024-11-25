# react-native-google-code-scanner-android

React Native wrapper for [Google Code Scanner API](https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner) (Android only)

## Installation

```sh
npm install react-native-google-code-scanner-android
```

### Exclude platforms other than Android

```
// react-native.config.js
module.exports = {
  dependencies: {
    // ...
    'react-native-google-code-scanner-android': {
      platforms: {
        ios: null,
        // web: null,
        // windows: null,
        // macos: null,
      },
    },
  },
};
```

## Usage

```js
import {
  initialize,
  scan,
  BarcodeFormat,
  checkModuleAvailability,
  installModule,
  subscribeToInstallProgress,
} from 'react-native-google-code-scanner-android';

// ...
// initialize the api (needed before scan)
initialize({
  barcodeFormats: [BarcodeFormat.FORMAT_ALL_FORMATS],
  enableAutoZoom: true,
  allowManualInput: true,
});

// ...
// scan for codes
await scan(
  (barcode: string) => {
    // on code available
    setResult(barcode);
  },
  (error: string) => {
    // on receiving error
    console.error(error);
  }
);

// ...
// check if the module is available
const isAvailable = await checkModuleAvailability();
if (!isAvailable) {
  // install the module
  const isInstalled = await installModule();
}

// ...
// subscribe to install progress
subscribeToInstallProgress((progress) => {
  console.log(`Install progress: ${progress}%`);
});
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
