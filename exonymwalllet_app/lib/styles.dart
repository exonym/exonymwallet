// Copyright 2018 The Flutter team. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

abstract class Styles {

  static const SizedBox spacer = SizedBox(width: 32.0, height: 16.0);
  static const SizedBox iconSpacer = SizedBox(width: 56.0, height: 56.0);

  //
  // Forms
  //
  static BoxDecoration cupertinoInput = BoxDecoration(
      borderRadius: const BorderRadius.all(Radius.elliptical(5, 5)),
      border: Border.all(
        color: Colors.white,
      )
  );

  static const cupertinoInputText = TextStyle(fontSize: 25.0, height: 1.0);


  //
  // Text
  //
  static const TextStyle itemHeader = TextStyle(
    color: Colors.white,
    fontSize: 28,
    fontStyle: FontStyle.normal,
    fontWeight: FontWeight.bold,
  );

  static const TextStyle productRowTotal = TextStyle(
    color: Color.fromRGBO(0, 0, 0, 0.8),
    fontSize: 18,
    fontStyle: FontStyle.normal,
    fontWeight: FontWeight.bold,
  );

  static const TextStyle itemSubtitle = TextStyle(
    color: Color(0xFF8E8E93),
    fontSize: 16,
    fontWeight: FontWeight.w300,
  );

  static const TextStyle searchText = TextStyle(
    color: Color.fromRGBO(0, 0, 0, 1),
    fontSize: 14,
    fontStyle: FontStyle.normal,
    fontWeight: FontWeight.normal,
  );

  static const TextStyle deliveryTimeLabel = TextStyle(
    color: Color(0xFFC2C2C2),
    fontWeight: FontWeight.w300,
  );

  static const TextStyle deliveryTime = TextStyle(
    color: CupertinoColors.inactiveGray,
  );

  //
  // Colors
  //
  static const Color productRowDivider = Color(0xFFD9D9D9);

  static const Color scaffoldBackground = Color(0xfff0f0f0);

  static const Color searchBackground = Color(0xffe0e0e0);

  static const Color searchCursorColor = Color.fromRGBO(0, 122, 255, 1);

  static const Color searchIconColor = Color.fromRGBO(128, 128, 128, 1);

  static const Color dim = Colors.grey;
}