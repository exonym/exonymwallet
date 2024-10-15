import 'dart:convert';
import 'dart:io';

import 'package:exonymwallet_cli/src/context/context_tracker.dart';
import 'package:exonymwallet_cli/src/context/menus.dart';
import 'package:exonymwallet_cli/src/context/console_decoration.dart' as line;
import 'package:exonymwallet_cli/src/wallet/sftp.dart';
import 'package:path/path.dart' as path;


class WalletCommands{

  late var rootPath;

  execute(List<String> commandAndOptions) async {
    rootPath = path.join(cliContext.rootPath());

    try {
      var selected = ChoiceAndOptions<WalletMenu>(
              commandAndOptions.removeAt(0), WalletMenu.values);
      selected.options = commandAndOptions;

      if (selected.choice == WalletMenu.get) {
        await _onboard(selected.options);
      } else if (selected.choice == WalletMenu.list) {
        await _list();
      } else if (selected.choice == WalletMenu.sftp) {
        await SftpCommands().execute(commandAndOptions);
      } else if (selected.choice == WalletMenu.create) {
        await _createContainer(selected.options);
      } else if (selected.choice == WalletMenu.open){
        await _openContainer(selected.options);
      } else if (selected.choice == WalletMenu.close){
        _closeContainer();
      } else {
        throw Exception();
      }
   } catch (e) {
      print("Message");
      print(e);
      _printWalletUsage(e);

    }
  }

  Future<void> _createContainer(List<String> options) async {
    if (options.isEmpty){
      return _printCreateWalletUsage();

    }
    final username = options[0];
    try{
      final password = _requestPassword("--$username--:Set Password> ");
      final passwordRepeat = _requestPassword("--$username--:Repeat Password> ");
      if (password==passwordRepeat){
          var recovery = await exonymWallet.setupWallet(
              username, password, rootPath);
          var pwd = await exonymWallet.sha256AsHex(password);

          _onCreateSuccessMessage(username, recovery);
          cliContext.containerName = username;
          cliContext.containerPassword = pwd;

      } else {
        line.error("Passwords did not match.  Please try again.");

      }
    } catch (e) {
      line.error("Error: $e");

    }
  }

  String _requestPassword(String prompt) {
    stdout.write(prompt);
    stdin.echoMode = false;
    final password = stdin.readLineSync(encoding: Utf8Codec());
    stdin.echoMode = true;
    print("");
    if (password==null || password.isEmpty){
      throw Exception("PASSWORD_EMPTY");

    } else {
      return password;

    }
  }

  void _closeContainer() {
    cliContext.containerPassword = null;
    cliContext.containerName = null;

  }

  Future<void> _openContainer(List<String> options) async {
    if (options.isEmpty){
      return _printOpenWalletUsage();

    }
    var username = options[0];
    var password = _requestPassword("--$username--Enter Password> ");

    try {
      final pwd = await exonymWallet.sha256AsHex(password);
      await exonymWallet.authenticate(username, pwd, rootPath);
      cliContext.containerName = username;
      cliContext.containerPassword = pwd;

    } catch (e) {
      print("\x1B[31m$e\x1B[0m");

    }
  }

  Future<void> _onboard(List<String> options) async {
    cliContext.rejectUnauthenticated();
    GlobalContext context = GlobalContext();
    if (context.testNetwork){
      await _onboardTestNet(options);

    } else {
      await _onboardMainNet(options);

    }
  }

  // rejected above
  Future<void> _onboardTestNet(List<String> options) async {
    if (options.isNotEmpty){
      if (options[0]=="sybil"){
        _onboardSybilTestNet(options);

      } else {
        _onboardAdvocateTestNet(options);

      }
    } else {
      line.error("[sybil 'person' || 'entity' || 'robot' || 'representative' || 'product'] || advocateUID ]");

    }
  }

  void _onboardSybilTestNet(List<String> options) async {
    if (options.length > 1){
      String result = await exonymWallet.onboardSybilTestnet(
          cliContext.containerName!,
          options[1],
          cliContext.containerPassword!,
          rootPath
      );
      line.success(result);

    } else {
      line.error("wallet get sybil <sybil-class>");

    }
  }

  // Works for universal links and Advocate IDs
  void _onboardAdvocateTestNet(List<String> options) async {
    if (options.isNotEmpty){
      if (options[0].startsWith("https://trust.exonym.io")){
        print(options[0]);
        exonymWallet.onboardRulebookIssuancePolicy(
            cliContext.containerName!,
            options[0],
            cliContext.containerPassword!,
            rootPath);

      } else {
        print("advocateUID");
        print(options[0]);
        String result = await exonymWallet.onboardRulebookModeratorUID(
            cliContext.containerName!,
            cliContext.containerPassword!,
            options[0],
            rootPath
        );
        line.success(result);

      }
    } else {
      line.error("<advocateUID> is needed");

    }
  }

  // auth rejected above
  Future<void> _onboardMainNet(List<String> options) async {
    line.success("main net onboarding");

  }



  Future<void> _list() async {
    cliContext.rejectUnauthenticated();
    String w = await exonymWallet.walletReport(
        cliContext.containerName!,
        cliContext.containerPassword!,
        rootPath);
    line.success(w);

  }

  Future<void> _sftp() async {
    cliContext.rejectUnauthenticated();
    line.success("sftp");

  }
}

String _computeTabs(String word){
  return (word.length > 7 ? "\t" : "\t\t");

}

void _onCreateSuccessMessage(String username, List<String> recovery) {
  line.success("New wallet created.");
  line.warn("---");
  line.warn("- Write down your recovery phrase and lock it away.");
  line.warn("- ");
  for (var i = 0; i < 8; i++){
    var word0 = "-  ${recovery[i]}";
    word0 += _computeTabs(word0);
    var word1 = recovery[i+8];
    word1 += _computeTabs(word1);
    var word2 = recovery[i+16];
    word2 += _computeTabs(word2);
    var word3 = recovery[i+24];
    word3 += _computeTabs(word3);
    line.warn("$word0$word1$word2$word3");

  }
  line.warn("- ");
  line.warn("---");
  line.success("The wallet is open.  see[exonym--$username-->] in the context prompt.");
  line.success("Now try:");
  print("  wallet close");
  print("  wallet open $username");

}


void _printCreateWalletUsage() {  line.warn('''
wallet create <container_name>
  ''');

}

void _printOpenWalletUsage() {  line.warn('''
wallet open <container_name>
''');

}


void _printWalletUsage(Object e) {
  line.warn('''
No command 'wallet $e': valid sub-commands are:
  create <container_name> :   create a new wallet,
  open                    :   open an existing wallet into the command-line context,
  close                   :   close the opened wallet,
  get                     :   accept a credential from a counterpart
  list                    :   list credentials in the open wallet
  sftp                    :   sftp operations
  ''');

}