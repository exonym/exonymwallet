import 'package:exonym/onboarding.dart';
import 'package:exonym/processes/authenticate.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'identity_tab.dart';
import 'model/app_state_model.dart';
import 'scanner_tab.dart';

class ExonymApp extends StatelessWidget {

  const ExonymApp({super.key});

  @override
  Widget build(BuildContext context) {

    SystemChrome.setPreferredOrientations(
        [DeviceOrientation.portraitUp, DeviceOrientation.portraitDown]);

    return const CupertinoApp(
      debugShowCheckedModeBanner: false,
      theme: CupertinoThemeData(brightness: Brightness.dark,
        // primaryColor: Colors.deepPurple,
        primaryContrastingColor: Colors.white,
        barBackgroundColor: Colors.black,
        scaffoldBackgroundColor: Colors.black,
        textTheme: CupertinoTextThemeData(
        primaryColor: Colors.white,
        // textStyle: TextStyle(),
        // actionTextStyle: TextStyle(),
        // tabLabelTextStyle: TextStyle(),
        // navTitleTextStyle: TextStyle(),
        // navLargeTitleTextStyle: TextStyle(),
        // navActionTextStyle: TextStyle(),
        // pickerTextStyle: TextStyle(),
        // dateTimePickerTextStyle: TextStyle(),

      ),

      ),
      home: ExonymLogonPage(),
    );
  }
}



class ExonymHomePage extends StatelessWidget {
  const ExonymHomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppStateModel>(
        builder: (context, model, child) {
          late ScannerTab? scanner;

        return CupertinoTabScaffold(
        tabBar: CupertinoTabBar(
          onTap: (tab) async {
            if (tab==2){
              scanner = const ScannerTab(); // activates scanner
            } else {
              scanner = null; // deactivates
            }
          },
          items: const <BottomNavigationBarItem>[
            BottomNavigationBarItem(
              icon: Icon(CupertinoIcons.rectangle_stack_person_crop_fill),
              label: 'ID',
            ),
            // BottomNavigationBarItem(
            //   icon: Icon(CupertinoIcons.rosette),
            //   label: 'Prove',
            // ),
            BottomNavigationBarItem(
              icon: Icon(CupertinoIcons.barcode_viewfinder),
              label: 'Scanner',
            ),
          ],
        ),
        tabBuilder: (context, index) {
          late final CupertinoTabView returnValue;
          switch (index) {
            case 0:
              returnValue = CupertinoTabView(builder: (context) {
                return const CupertinoPageScaffold(
                  child: IdentityTab(),
                );
              });
              break;
            // case 1:
            //   returnValue = CupertinoTabView(builder: (context) {
            //     return const CupertinoPageScaffold(
            //       child: ProofTab(),
            //     );
            //   });
            //   break;
            case 1:
              returnValue = CupertinoTabView(builder: (context) {
                return CupertinoPageScaffold(
                  child: scanner!,
                );
              });
              break;
          }
          return returnValue;
        },
        controller: CupertinoTabController(initialIndex: 0)
      );
    });
  }

}
