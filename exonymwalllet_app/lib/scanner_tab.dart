import 'package:flutter/cupertino.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import 'package:provider/provider.dart';
import 'model/app_state_model.dart';

class ScannerTab extends StatefulWidget {
  const ScannerTab({super.key});

  @override
  State<ScannerTab> createState() {
    return _ScannerTabState();
  }
}

class _ScannerTabState extends State<ScannerTab> {

  @override
  Widget build(BuildContext context) {
    return Consumer<AppStateModel>(
      builder: (context, model, child) {
        return  CupertinoPageScaffold(
          navigationBar: const CupertinoNavigationBar(
            middle: Text("Scan QR to Authenticate"),
          ),
          child: _buildScanner(context, model),
        );
      },
    );
  }
}


Widget _buildScanner(BuildContext context, AppStateModel model){
  MobileScannerController controller = MobileScannerController(
    facing: CameraFacing.back,
    torchEnabled: false,
    detectionSpeed: DetectionSpeed.noDuplicates,

  );
  return MobileScanner(
    controller: controller,
    onDetect: (capture) {
      final List<Barcode> barcodes = capture.barcodes;
      for (final barcode in barcodes) {
        debugPrint('Barcode found! ${barcode.rawValue}');
      }
      controller.dispose();
    },
  );
}



