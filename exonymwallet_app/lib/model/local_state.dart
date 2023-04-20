import 'dart:convert';
import 'dart:io';
import 'package:path/path.dart' as path;
import 'package:path_provider/path_provider.dart';

class LocalFileSystemState {

  late Directory _directory;
  late File metadataFile;

  LocalFileSystemState();

  Future<bool> hasPersonContainer() async {
    _directory = await getApplicationDocumentsDirectory();
    print(_directory.path);
    final metadataFilePath = path.join(_directory.path, "wallet_metadata.json");
    metadataFile = File(metadataFilePath);
    final ids = Directory(path.join(_directory.path, "identities"));
    return metadataFile.exists();

  }

  void identityContainersOnDevice() async {

  }

  Future<void> setupLocalFileSystem(String username, String b64salt) async {
    if (username.isEmpty){
      throw Exception("Username has not been set");

    }
    if (metadataFile.existsSync()){
      throw Exception("Cannot overwrite metadata");

    } else {
      _createMetadata(username, b64salt);

    }
  }

  Future<void> _createMetadata(String username, String b64salt) async {
    WalletMetadata walletMetadata = WalletMetadata(
        walletName: username,
        b64Salt: b64salt
    );
    metadataFile.writeAsString(jsonEncode(walletMetadata));
  }

  Future<WalletMetadata> _openMetadata() async {
    final content = await metadataFile.readAsString();
    return WalletMetadata.fromJson(jsonDecode(content));

  }
}

class WalletMetadata {
  WalletMetadata({
    required this.walletName,
    required this.b64Salt,
  });

  final String walletName;
  final String b64Salt;

  factory WalletMetadata.fromJson(Map<String, dynamic> json) {
    return WalletMetadata(
      walletName: json["walletName"],
      b64Salt: json["b64Salt"],
    );
  }

  Map<String, dynamic> toJson(){
    return {
      'walletName': walletName,
      'b64Salt':b64Salt,
    };
  }

}