import * as React from 'react';

import { StyleSheet, View, Text, Button } from 'react-native';
import {
  BarcodeFormat,
  initialize,
  scan,
} from 'react-native-google-code-scanner-android';

export default function App() {
  const [result, setResult] = React.useState<string | undefined>();

  React.useEffect(() => {
    initialize({
      barcodeFormats: [BarcodeFormat.FORMAT_ALL_FORMATS],
      enableAutoZoom: true,
      allowManualInput: true,
    });
  }, []);

  const openScanner = async () => {
    try {
      await scan(
        (barcode) => {
          setResult(barcode);
        },
        (error) => {
          // you may get the following error for the first time
          // todo: handing the error
          // "Waiting for the Barcode UI module to be downloaded."
          console.error(error);
        }
      );
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <View style={styles.container}>
      <Button title="Open Scanner" onPress={openScanner} />
      <Text style={styles.result}>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'space-evenly',
  },
  result: {
    fontSize: 20,
  },
});
