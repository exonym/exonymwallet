import 'package:exonymwallet/exonymwallet.dart';
import 'package:test/test.dart';

import 'test_utils.dart';

void main() {

  group('Exonym Create Wallet', () {

    var wallet = ExonymWallet();
    const username = "username";
    const path = "identities";

    setUpAll(() {
    }); //set up

    test("- weak password;", () async {
      await expectLater(wallet.setupWallet(username, "password",path), throwsA(isA<Exception>()));
    });

    test("+ strong password; container already exists", () async {
      await wallet.setupWallet(username, "34wdvdbf8e8r883427573",path);
      await expectLater(wallet.setupWallet(username, "34wdvdbf8e8r883427573", path), throwsA(isA<Exception>()));
    });

    test("+ - authentication;", () async {
      final u = "whodido";
      final password = "34wdvdbf8e8r883427573";
      var hash = await wallet.sha256AsHex(password);
      var badPassword = await wallet.sha256AsHex("password");
      print ("Using Password Hash: $hash for password: $password");
      await wallet.setupWallet(u, password, path);
      await wallet.authenticate(u, hash, path);
      await expectLater(wallet.authenticate(u, badPassword, path), throwsA(isA<Exception>()));
      await expectLater(wallet.authenticate("user_does_not_exist", badPassword, path), throwsA(isA<Exception>()));

    });

    tearDownAll(() async {
      wallet.dispose();
      await deleteContainer(username);
      await deleteContainer("whodido");

    });
  });
}




