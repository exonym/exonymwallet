import 'package:exonym/model/identity_container_repository.dart';
import 'package:flutter/foundation.dart' as foundation;
import 'package:exonymwallet/exonymwallet.dart';

import 'local_state.dart';


// Entry point for wallet features
// listIdentitites()
//  - listRulebooks()
class AppStateModel extends foundation.ChangeNotifier {

  final LocalFileSystemState _localFileSystemState = LocalFileSystemState();

  Future<bool> isDeviceSetup(){
    return _localFileSystemState.hasPersonContainer();

  }
  Future<bool> isSignedIn(){
    return _localFileSystemState.hasPersonContainer();

  }

  Future<bool> isSybilRegistered(){
    return _localFileSystemState.hasPersonContainer();

  }

  String? _barcodeScan;

  String? get barcodeScan => _barcodeScan;

  void setBarcodeScan(String scan){
    _barcodeScan = scan;
    notifyListeners();

  }

  // authentication
  Future<void> login(String plainTextPassword) async {
    if (plainTextPassword=="123qwe"){
      return;
    } else {
      throw Exception();
    }
  }

  final _idsInWallet = <String, IdentityContainer>{};

  // // master password to access passwords to unlock all identities
  // Future<List<String>> createIdAndAddToWallet(
  //     IdentityType type, String username, String password) async {
  //   // Enforce only one person ID for each device
  //   // Check for Sybil onboarding before accepting > 1 container
  //   // Ensure if there's one container, the first is person
  //   final directory = await getApplicationDocumentsDirectory();
  //   log("directory.path=${directory.path}");
  //   List<String> result = await wallet.setupWallet(username,
  //       password, directory.path);
  //
  //   // create local identity references.
  //   return result;
  //
  // }
  //
  // Future<String> sha256AsHex(String toHash){
  //   return wallet.sha256AsHex(toHash);
  //
  // }

  Map<String, IdentityContainer> get idsInWallet {
    return Map.from(_idsInWallet);
  }

  List<IdentityContainer> allIds() {
    return IdentityRepository.loadProducts(IdentityType.all);
  }

  IdentityType _selectedIdType = IdentityType.all;

  IdentityType get selectedIdType {
    return _selectedIdType;
  }

  void setCategory(IdentityType newType) {
    _selectedIdType = newType;
    notifyListeners();
  }

  IdentityContainer? _selectedId;

  IdentityContainer? get selectedId{
    return _selectedId;

  }

  void setIdentity(String username){
    _selectedId = idsInWallet[username];

  }


  @override
  void dispose() {
    // wallet.dispose();
    super.dispose();
  }

  String? username;
  String? plainTextPassword;
  bool confirmedRecovery = false;

  void setUsername(String username){
    this.username = username;
  }

  void setPassword(String password){
    plainTextPassword = password;
    // todo : move to confirmation of word vector
    _localFileSystemState.setupLocalFileSystem(username!, "todo");

  }

  void setConfirmedRecovery(bool confirmed){
    confirmedRecovery = confirmed;
  }

// int get totalCartQuantity {
//   return _productsInCart.values.fold(0, (accumulator, value) {
//     return accumulator + value;
//   });
// }

}

