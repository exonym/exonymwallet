import 'dart:convert';
import 'dart:ffi';
import 'dart:io';
import 'package:path/path.dart' as path;
import 'package:ffi/ffi.dart';
import 'generated/exonym_wallet_bindings.dart';
import 'package:exonymwallet/src/resource_management/closable.dart';
import 'model/global.dart' as global;

class ExonymWallet extends AbstractResource {


  final Pointer<graal_create_isolate_params_t> _params
    = calloc<graal_create_isolate_params_t>();

  final Pointer<Pointer<graal_isolate_t>> _isolate
    = calloc<Pointer<graal_isolate_t>>();

  final Pointer<Pointer<graal_isolatethread_t>> _thread
    = calloc<Pointer<graal_isolatethread_t>>();

  ExonymWalletLibrary? _lib;

  ExonymWallet(){
    var libraryPath = path.join(
        Directory.current.path, global.LIB_FOLDER, global.LIB_NAME);
    if (Platform.isIOS) {
      libraryPath = global.LIB_NAME;
      print("Platform=iOS : ensure $libraryPath is available - xcode~Targets~General~Frameworks, Libraries, and Embedded Content");
      print("A message should follow - stating that the library was linked successfully");

    }
    final libPath = DynamicLibrary.open(libraryPath);
    _lib = ExonymWalletLibrary(libPath);
    _lib!.graal_create_isolate(_params, _isolate, _thread);
    final int twelve = _lib!.hello_exonym(_getThread());
    if (twelve==12){
      print("© 2023 - Exonym - Network Authentication Solutions");

    } else {
      print("An error occured - libexonymwallet did not connect - check the .dylib is present: $libraryPath");

    }
  }

  ping(){
    _lib!.hello_exonym(_getThread());
  }

  Future<String> openSystemParams() async {
    return global.fromCString(_lib!.open_system_params(_getThread()));
  }

  Future<String> newRulebook(String ruleFile, String descriptionFile) async {
    return global.fromCString(
        _lib!.new_rulebook(_getThread(),
            global.toCString(ruleFile),
            global.toCString(descriptionFile)
        )
    );
  }

  Future<String> sftpTemplate(String path) async {
    return global.fromCString(
        _lib!.sftp_template(_getThread(),
            global.toCString(path)
        )
    );
  }

  Future<String> sftpAdd(String username, String passwordAsSha256Hex, String path) async {
    return global.fromCString(
        _lib!.sftp_add(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(path)
        )
    );
  }

  Future<String> sftpRemove(String username, String passwordAsSha256Hex, String name, String path) async {
    return global.fromCString(
        _lib!.sftp_remove(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(name),
            global.toCString(path)
        )
    );
  }

  Future<String> walletReport(String username, String passwordAsSha256Hex, String path) async {
    return global.fromCString(
        _lib!.wallet_report(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(path)
        )
    );
  }

  Future<String> authenticationReport(String username, String passwordAsSha256Hex,
      String request, String path) async {
    return global.fromCString(
        _lib!.authentication_report(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(request),
            global.toCString(path)
        )
    );
  }

  Future<String> spawnNetworkMap(String path) async {
    return global.fromCString(_lib!.spawn_network_map(_getThread(), global.toCString(path)));
  }

  Future<String> viewActor(String uid, String path) async {
    return global.fromCString(
        _lib!.view_actor(_getThread(),
            global.toCString(uid),
            global.toCString(path)
        )
    );
  }

  Future<String> listActors(String uid, String path) async {
    return global.fromCString(
        _lib!.list_actors(_getThread(),
            global.toCString(uid),
            global.toCString(path)
        )
    );
  }

  Future<String> listRulebooks(String path) async {
    return global.fromCString(
        _lib!.list_rulebooks(_getThread(),
            global.toCString(path)
        )
    );
  }

  Future<String> proofForRulebookSSO(String username, String passwordAsSha256Hex,
      String ulinkChallenge, String path) async {
    return global.fromCString(
        _lib!.proof_for_rulebook_sso(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(ulinkChallenge),
            global.toCString(path)
        )
    );
  }

  Future<String> generateDelegationRequestForThirdParty(String username,
      String passwordAsSha256Hex, String ulinkChallenge, String name, String path) async {

    return global.fromCString(
        _lib!.generate_delegation_request_for_third_party(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(ulinkChallenge),
            global.toCString(name),
            global.toCString(path)
        )
    );
  }

  Future<String> fillDelegationRequest(String username, String passwordAsSha256Hex,
      String ulink, String path) async {

    return global.fromCString(
        _lib!.fill_delegation_request(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(ulink),
            global.toCString(path)
        )
    );
  }

  Future<String> verifyDelegationRequest(String username, String passwordAsSha256Hex,
      String requestLink, String proofLink, String path) async {
    return global.fromCString(
        _lib!.verify_delegation_request(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(requestLink),
            global.toCString(proofLink),
            global.toCString(path)
        )
    );
  }

  Future<String> nonInteractiveProofRequest(String username, String passwordAsSha256Hex,
      String nonInteractiveProofRequest, String path) async {

    return global.fromCString(
        _lib!.non_interactive_proof(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(nonInteractiveProofRequest),
            global.toCString(path)
        )
    );
  }

  Future<String> onboardSybilTestnet(String username,
      String sybilClass,
      String passwordAsSha256Hex,
      String path) async {

    return global.fromCString(
        _lib!.onboard_sybil_testnet(_getThread(),
            global.toCString(username),
            global.toCString(sybilClass),
            global.toCString(passwordAsSha256Hex),
            global.toCString(path)
        )
    );
  }

  Future<String> onboardRulebookIssuancePolicy(String username,
      String issuancePolicy,
      String passwordAsSha256Hex,
      String path) async {

    return global.fromCString(
        _lib!.onboard_rulebook(_getThread(),
            global.toCString(username),
            global.toCString(issuancePolicy),
            global.toCString(passwordAsSha256Hex),
            global.toCString(path)
        )
    );
  }

  Future<String> onboardRulebookAdvocateUID(String username,
      String passwordAsSha256Hex,
      String advocateUid,
      String path) async {

    return global.fromCString(
        _lib!.onboard_rulebook_advocate_uid(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(advocateUid),
            global.toCString(path)

        )
    );
  }

  Future<String> sha256AsHex(String toHash) async {
    final r = _lib!.sha_256_as_hex(_getThread(), global.toCString(toHash));
    return global.fromCString(r);

  }

  Future<List<String>> setupWallet(String username, String plainTextPassword, String path) async {
      final r = _lib!.setup_wallet_path(_getThread(), global.toCString(username),
          global.toCString(plainTextPassword), global.toCString(path));
      String result =  global.fromCString(r);
      if (!result.startsWith("[")){
        throw Exception("$result ${global.INFORMAL_LIB_NAME}");

      } else {
        return List<String>.from(jsonDecode(result) as List);

      }
  }

  Future<void> authenticate(String username, String passwordAsHexString, String path) async {
    final r = _lib!.authenticate(_getThread(),
      global.toCString(username),
      global.toCString(passwordAsHexString),
      global.toCString(path),
    );
    String result = global.fromCString(r);
    if (result!="OPENED"){
      throw Exception("$result ${global.INFORMAL_LIB_NAME}");

    } else {
      return;

    }
  }

  Pointer<graal_isolatethread_t> _getThread(){
    return _thread.value;
  }

  @override
  void dispose() {
    _lib!.graal_tear_down_isolate(_getThread());
    print("© 2023 Exonym - Ciao for now.");

  }
}