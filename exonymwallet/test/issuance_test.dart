import 'dart:io';

import 'package:test/test.dart';
import 'package:path/path.dart' as path;

void main(){

  group("Issuance", () {

    setUpAll(() async {
      print("setUpAll");

    });

    test("test0", () async {
      print("test0");

    });

    tearDownAll(() {
      print("tear down");

    });
  });

}