import 'dart:convert';
import 'dart:io';

import 'package:exonymwallet_cli/src/context/context_tracker.dart';
import 'package:exonymwallet_cli/src/context/menus.dart';
import 'package:exonymwallet_cli/src/context/console_decoration.dart' as line;
import 'package:path/path.dart' as path;

class SftpCommands {

  execute(List<String> commandAndOptions) async {

    try {
      var selected = ChoiceAndOptions<SftpMenu>(
          commandAndOptions.removeAt(0), SftpMenu.values);
      selected.options = commandAndOptions;

      if (selected.choice == SftpMenu.template) {
        await _template();

      } else if (selected.choice == SftpMenu.add) {
        await _add();

      } else if (selected.choice == SftpMenu.remove) {
        await _remove(selected.options);

      } else {
        throw Exception();

      }
    } catch (e) {
      print(e);
      _printWalletUsage(e);

    }
  }

  _template() async {
    String r = await exonymWallet.sftpTemplate(cliContext.rootPath());
    line.success(r);

  }

  _add() async {
    cliContext.rejectUnauthenticated();
    String r = await exonymWallet.sftpAdd(
        cliContext.containerName!,
        cliContext.containerPassword!,
        cliContext.rootPath());

    line.success(r);

  }

  _remove(List<String> options) async {
    cliContext.rejectUnauthenticated();
    if (options.isNotEmpty){
      String r = await exonymWallet.sftpRemove(
          cliContext.containerName!,
          cliContext.containerPassword!,
          options[0],
          cliContext.rootPath());

      line.success(r);

    } else {
      line.error("Usage: wallet sftp remove <name-of-sftp-credential>");

    }
  }
}

void _printWalletUsage(Object e) {
  line.warn('''
No command 'wallet sftp $e': valid sub-commands are:
  template         :   create a template to add new credentials
  add              :   add from the sftp-credential.xml file in cli/<test|main>/
  remove           :   remove credential by name
  ''');

}