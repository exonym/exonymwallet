import 'dart:convert';

import 'package:exonymwallet/exonymwallet.dart';
import 'package:exonymwallet/src/model/non_interactive_proof.dart';
import 'package:test/test.dart';

void main(){

  group("Non-Interactive-Proof-Requests", () {

    var wallet = ExonymWallet();
    const username = "mjh";
    const strongPwd = "34wdvdbf8e8r883427573";
    const path = "identities";
    const advocateUid = "urn:rulebook:exonym:trusted-sources:29a655983776d9cd7b4be696ed4cd773e63e6d640241e05c3a40b5d81f5d1f1c:c2c9d8b0:i";

    setUpAll(() async {
      print("Defining Wallet " + username);
      // await wallet.setupWallet(username, stronrgPwd, path);

    });

    test("test0", () async {
      print("test0");
      var hash = await wallet.sha256AsHex(strongPwd);
      // var r0 = await wallet.onboardSybilTestnet(username, "person", hash, path);
      // print(r0);
      // var r1 = await wallet.onboardRulebookAdvocateUID(username, hash, advocateUid, path);
      // print(r1);
      NonInteractiveProofRequest proof = NonInteractiveProofRequest();
      proof.issuerUids.add(advocateUid);
      proof.pseudonyms.add("urn:pseudonym:20231202");
      proof.metadata = {"url":"https://exonym.io"};

      wallet.nonInteractiveProofRequest(username, hash, proof.toJson(), path);


    });

    tearDownAll(() {
      print("You need to delete the wallet");


    });
  });

}