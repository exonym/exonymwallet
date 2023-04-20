import 'package:exonym/model/app_state_model.dart';
import 'package:exonym/widgets/ui.dart';
import 'package:exonym/widgets/walkthrough_page.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'styles.dart';

class FoundationIdentity extends StatefulWidget {
  const FoundationIdentity({super.key});

  @override
  State<StatefulWidget> createState() {
    return _FoundationIdentity();
  }
}

class _FoundationIdentity extends State<FoundationIdentity> {

  @override
  Widget build(BuildContext context) {

    final controller = TextEditingController();

    final form = DefaultCupertinoTextField(controller: controller, label: "Wallet name");

    const message = Align(
        alignment: Alignment.center,
        widthFactor: 0.5,
        child: Column(children: [
          Text("Your wallet name is private and cannot be changed.",
            style: Styles.itemSubtitle,
          ),
          Styles.spacer,
          Text("Tip: Use your name or initials."),
        ],)
    );

    return Consumer<AppStateModel>(builder: (context, model, child){
      return WalkthroughPage(
        icon: CupertinoIcons.person_alt_circle_fill,
        title: "Wallet Setup",
        input: form,
        messaging: message,
        buttons: CupertinoButton.filled(
            child: const Text("Confirm"),
            onPressed: () async {
              final username = controller.value.text;
              if (username.length > 2){
                model.setUsername(username);
                Navigator.push(context,
                    MaterialPageRoute<SetWalletPassword>(
                        builder: (context) => const SetWalletPassword()
                    )
                );
              } else {
                showNotificationAlertDialog(context, "Wallet Name Length Error",
                    "Wallet name must be 3 characters or longer");

              }
            }
        ),
        showBackButton: false,
        altButton: CupertinoButton(
          child: const Icon(CupertinoIcons.doc_person,
            color: Styles.dim,
          ),
          onPressed: () {
            showCancellableAlertDialog(context,
                "Import Wallet",
                "Do you want to import a wallet?", (){

            });
          },),
      );
    });
  }
}

class SetWalletPassword extends StatefulWidget {
  const SetWalletPassword({super.key});

  @override
  State<StatefulWidget> createState() {
    return _SetWalletPassword();
  }
}

class _SetWalletPassword extends State<SetWalletPassword> {
  
  final password = TextEditingController();
  final repeatPassword = TextEditingController();
  
  @override
  Widget build(BuildContext context) {
    return Consumer<AppStateModel>(builder: (builder, model, ctx){
      return WalkthroughPage(
        icon: CupertinoIcons.padlock_solid,
          title: "Set Password",
          input: Column(children: [
            DefaultCupertinoTextField(
                controller: password, label: "Password",
              obscureText: true,
            ),
            Styles.spacer,
            DefaultCupertinoTextField(
                controller: repeatPassword, label: "Repeat",
              obscureText: true,
            ),
          ],),
          messaging: const Align(
            alignment: Alignment.center,
            child: Text("Password must be at least seven characters to include one upper and one lower case letter.",
              textAlign: TextAlign.center,
            )
            ,),
          buttons: CupertinoButton.filled(
              child: const Text("Confirm"), 
              onPressed: (){
                String pwd = password.value.text;
                String rpwd = repeatPassword.value.text;
                if (pwd==rpwd){
                  model.setPassword(pwd);

                } else {
                  showNotificationAlertDialog(context,
                      "Please Try Again", "Passwords did not match.");
                  password.text = "";
                  repeatPassword.text = "";

                }
              })
      );
    });
  }
  
}