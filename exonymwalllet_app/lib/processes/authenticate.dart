import 'package:exonym/onboarding.dart';
import 'package:exonym/styles.dart';
import 'package:exonym/widgets/ui.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../model/app_state_model.dart';
import '../assets/assets.dart';
import '../app.dart';

class ExonymLogonPage extends StatefulWidget {
  const ExonymLogonPage({super.key});

  @override
  State<StatefulWidget> createState() {
    return _ExonymLogonPage();
  }
}

class _ExonymLogonPage extends State<ExonymLogonPage> {

  late Image logo;

  @override
  void initState() {
    super.initState();
    logo = Image.asset(
      Images.exonymLogoWithTextPath,
      height: 49.0,
    );
  }

  @override
  void didChangeDependencies() {
    precacheImage(logo.image, context);
    super.didChangeDependencies();

  }


  @override
  Widget build(BuildContext context) {
    final controller = TextEditingController();


    void showAlertDialog(BuildContext context) {
      showCupertinoModalPopup<void>(
        context: context,
        builder: (BuildContext context) => CupertinoAlertDialog(
          title: const Text('Password Incorrect'),
          content: const Text('Please try again.'),
          actions: <CupertinoDialogAction>[
            CupertinoDialogAction(
              /// This parameter indicates this action is the default,
              /// and turns the action's text to bold text.
              isDefaultAction: true,
              onPressed: () {
                Navigator.pop(context);
              },
              child: const Text('OK'),
            ),
          ],
        ),
      );
    }


    void showRecovery(BuildContext context){
      showCupertinoModalPopup<void>(
          context: context,
          builder: (BuildContext context) => CupertinoActionSheet(
            title: const Text("Recovery"),
            message: const Text("You will need the 32 word Recovery Phrase, provided on wallet set-upå"),
            actions: [
              CupertinoActionSheetAction(
                isDefaultAction: true,
                child: const Text("Recover"),
                onPressed: () {

                },
              ),
              CupertinoActionSheetAction(
                child: const Text("Lost Recovery Phrase"),
                onPressed: () {
                  // navigate to web page on lost identities

                },
              ),
            ],
          ));
    }

    return Consumer<AppStateModel>(builder: (context, model, child){

      model.isDeviceSetup()
          .then((setup){
            if (!setup) {
              Navigator.of(context).push(slowTransitionToRoute(const FoundationIdentity()));
            }
          });

      return CupertinoPageScaffold(
          resizeToAvoidBottomInset: true,
          navigationBar: CupertinoNavigationBar(
            trailing: CupertinoButton(
              child: const Icon(CupertinoIcons.gear_alt,
                color: Styles.dim,
              ),
              onPressed: () {
                showRecovery(context);
              },
            ),
          ),
          child: Container(
            padding: const EdgeInsets.symmetric(vertical: 24.0, horizontal: 16.0),
            alignment: Alignment.center,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Styles.spacer,
                logo,
                Styles.spacer,
                DefaultCupertinoTextField(
                  controller: controller,
                  label: "Password",
                  obscureText: true,
                ),
                CupertinoButton.filled(
                    child: const Icon(CupertinoIcons.lock_shield),
                    onPressed: () {
                      model.login(controller.value.text)
                          .then((r){
                            Navigator.push(context,
                                MaterialPageRoute(
                                    builder: (context) => const ExonymHomePage()));

                      }).catchError((e){
                        showAlertDialog(context);

                      });
                    }),
              ],
            ),
          )
      );
    });
  }
}