import 'package:exonymwallet/exonymwallet.dart';
import 'package:path/path.dart' as path;

final ExonymWallet exonymWallet = ExonymWallet();
final GlobalContext cliContext = GlobalContext();

class GlobalContext{

  static final GlobalContext _context = GlobalContext._internal();

  factory GlobalContext() {
    return _context;
  }

  GlobalContext._internal();

  IdentityContainer? container;
  String? containerName;
  bool testNetwork = true;
  String? containerPassword;
  String? targetAdvocate;
  String language = "en";

  bool isLoggedIn()  {
    return containerName!=null && containerPassword!=null;
  }

  void rejectUnauthenticated() {
    if (!isLoggedIn()){
      throw "wallet open <username>";
    }
  }

  @override
  String toString(){
    var result = "exonym";
    if (this.testNetwork==true){
      result = "$result(test-net)";
    }
    if (this.containerName!=null){
      result = "$result--$containerName--";
    }
    if (this.targetAdvocate!=null){
      result = "[$targetAdvocate]$result";
    }
    return "$result> ";
  }

  String rootPath() {
    var directory = path.current;
    return path.join(directory, "cli", (this.testNetwork ? "test-net" : "main-net"));

  }
}

extension BoolParsing on String {
  bool parseBool() {
    if (this.toLowerCase() == 'true') {
      return true;
    } else if (this.toLowerCase() == 'false') {
      return false;
    }
    throw 'Unexpected value: "$this"';
  }
}

