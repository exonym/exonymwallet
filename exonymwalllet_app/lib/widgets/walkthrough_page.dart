import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import '../styles.dart';

class WalkthroughPage extends StatefulWidget {
  const WalkthroughPage({
    super.key,
    required this.icon,
    required this.title,
    required this.input,
    this.messaging,
    required this.buttons,
    this.altButton,
    this.showBackButton = true
  });

  // define objects
  final IconData icon;
  final String title;
  final Widget input;
  final Widget? messaging;
  final Widget buttons;
  final Widget? altButton;
  final bool showBackButton;

  @override
  State<StatefulWidget> createState() {
    return _WalkthroughPageState();
  }
}

class _WalkthroughPageState extends State<WalkthroughPage>{

  @override
  Widget build(BuildContext context) {

    Widget menubar = _buildMenubar();

    return CupertinoPageScaffold(
      child: Container (
        color: Colors.black,
        padding:  const EdgeInsets.symmetric(vertical: 49.0, horizontal: 16.0),
        child : Center(
          child: Column(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children:  [
                menubar,
              Column(children: [
                Icon(widget.icon, size: 50.0,),
                Text(widget.title, style: Styles.itemHeader)
              ],),

              widget.input,
                widget.messaging !=null ? widget.messaging! : Styles.spacer,
                widget.buttons,
              ]
            ),
          ),
        ),
      );

  }

  Widget _buildMenubar() {
    if (widget.showBackButton && widget.altButton!=null){
      return Row(mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          CupertinoButton(onPressed: (){
            Navigator.pop(context);
          }, child: const Icon(CupertinoIcons.lessthan)),
          Styles.spacer,
          widget.altButton!,
        ],);

    } else if (widget.altButton!=null){
      return Row(mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Styles.spacer,
          Styles.spacer,
          widget.altButton!
        ],);

    } else if (widget.showBackButton){
      return Row(mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          CupertinoButton(onPressed: (){
            Navigator.pop(context);
          }, child: const Icon(CupertinoIcons.lessthan)),
          Styles.spacer,
          Styles.spacer,
        ],);

    } else {
      return Styles.spacer;

    }
  }
}