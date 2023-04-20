import 'dart:async';
import 'dart:core';

import 'dart:io';
import 'dart:isolate';

import 'package:console/console.dart';
import 'package:exonymwallet_cli/src/context/context_tracker.dart';
import 'package:exonymwallet_cli/src/context/menus.dart';
import 'package:exonymwallet_cli/src/net/net.dart';
import 'package:exonymwallet_cli/src/prove/prove.dart';
import 'package:exonymwallet_cli/src/rulebook/rulebook.dart';
import 'package:exonymwallet_cli/src/target/target.dart';
import 'package:exonymwallet_cli/src/wallet/wallet.dart';


class ExonymCLI{


  final GlobalContext context = GlobalContext();
  final WalletCommands wallet = WalletCommands();
  final RulebookCommands rulebook = RulebookCommands();
  final TargetCommands target = TargetCommands();
  final NetCommands net = NetCommands();
  final ProveCommands prove = ProveCommands();
  final ShellPrompt shell = ShellPrompt();

  Future<void> awaitInstructions() async {
    shell.message = cliContext.toString();
    exonymWallet.ping();

    shell.loop().listen((cmd) async {

      if (['stop', 'quit', 'exit'].contains(cmd.toLowerCase().trim())) {
        _terminateApplication();
        shell.stop();
        return;

      } else {
        await processCommands(cmd);

      }
      shell.message = cliContext.toString();

    });
  }

  processCommands(String cmd) async {
    try {
      ChoiceAndOptions command = ChoiceAndOptions<TopMenu>(cmd!, TopMenu.values);
      if (command.choice==TopMenu.prove){
        await prove.execute(command.options);

      } else if (command.choice==TopMenu.net){
        await net.execute(command.options);

      } else if (command.choice==TopMenu.rulebook){
        await rulebook.execute(command.options);

      } else if (command.choice==TopMenu.wallet){
        await wallet.execute(command.options);

      } else if (command.choice==TopMenu.target){
        await target.execute(command.options);

      } else {
        throw "No Command";

      }
    } catch (e) {
      print(e);
      printGlobalUsage();

    }
  }

  void _terminateApplication() {
    exonymWallet.dispose();
    exit(0);

  }
}

// @2.9
void main(List<String> args) async {
  ExonymCLI cli = ExonymCLI();
  cli.awaitInstructions();

}
