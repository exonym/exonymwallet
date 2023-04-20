import 'package:flutter/cupertino.dart';

import '../styles.dart';

Route slowTransitionToRoute(Widget page) {
  return PageRouteBuilder(
      fullscreenDialog: true,
      pageBuilder: (context, animation, secondaryAnimation) => page,
      transitionDuration: const Duration(milliseconds: 1000),
      transitionsBuilder: (context, animation, secondaryAnimation, child) {
        const begin = Offset(0.0, 1.0);
        const end = Offset.zero;
        const curve = Curves.ease;

        var tween = Tween(begin: begin, end: end)
            .chain(CurveTween(curve: curve));

        return SlideTransition(
          position: animation.drive(tween),
          child: child,
        );
      }
  );
}

class DefaultCupertinoTextField extends StatelessWidget {

  const DefaultCupertinoTextField({
    super.key,
    this.obscureText = false,
    this.autofocus = false,
    required this.controller,
    required this.label,

  });

  final TextEditingController? controller;
  final bool obscureText;
  final bool autofocus;
  final String label;

  @override
  Widget build(BuildContext context) {
    return CupertinoTextField(
      style: Styles.cupertinoInputText,
      decoration: Styles.cupertinoInput,
      controller: controller,
      obscureText: obscureText,
      prefix: CupertinoLabel(label: label),
      autofocus: autofocus,
    );
  }
}

class CupertinoLabel extends StatelessWidget {

  const CupertinoLabel({
    super.key,
    required this.label
  });

  final String label;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(left: 10.0),
      child: Text("$label:", style: Styles.itemSubtitle),
    );
  }
}


void showNotificationAlertDialog(BuildContext context, String title, String message) {
  showCupertinoModalPopup<void>(
    context: context,
    builder: (BuildContext context) => CupertinoAlertDialog(
      title: Text(title),
      content: Text(message),
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

void showCancellableAlertDialog(BuildContext context, String title, String message, Function fn) {
  showCupertinoModalPopup<void>(
    context: context,
    builder: (BuildContext context) => CupertinoAlertDialog(
      title: Text(title),
      content: Text(message),
      actions: <CupertinoDialogAction>[
        CupertinoDialogAction(
          isDefaultAction: false,
          onPressed: () {
            Navigator.pop(context);
          },
          child: const Text('Cancel'),
        ),
        CupertinoDialogAction(
          isDefaultAction: true,
          onPressed: () {
            Navigator.pop(context);
            fn();
          },
          child: const Text('OK'),
        ),
      ],
    ),
  );
}
