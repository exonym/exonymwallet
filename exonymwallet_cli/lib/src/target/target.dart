import 'package:exonymwallet_cli/src/context/console_decoration.dart' as line;

import '../context/context_tracker.dart';
import '../context/menus.dart';

class TargetCommands {

  execute(List<String> commandAndOptions) async {
    try {

      var selected = ChoiceAndOptions<TargetMenu>(
          commandAndOptions.removeAt(0), TargetMenu.values);

      selected.options = commandAndOptions;

      if (selected.choice == TargetMenu.testnet){
        _targetNetwork(selected.options);

      } else if (selected.choice == TargetMenu.lead){

        var parts = ChoiceAndOptions<TargetMenuAction>(
            selected.options.removeAt(0), TargetMenuAction.values);
        print("Choice = ${selected.options}");

        if (parts.choice == TargetMenuAction.add){
          _addLead(selected.options);

        } else if (parts.choice == TargetMenuAction.remove){
          _removeLead(selected.options);

        }
      } else {
        throw Exception("Unexpected");

      }
    } catch (e) {
      print(e);
      _printTargetUsage(e);

    }
  }

  Future<void> _targetNetwork(List<String> options) async {
    try {
      bool targetTestNet = options[0].parseBool();
      cliContext.testNetwork = targetTestNet;
      cliContext.containerPassword = null;
      cliContext.containerName = null;

    } catch (e) {
      print(e);
      _printTargetUsage(e);

    }
  }

  Future<void> _addLead(List<String> options) async {
    if (!options.isEmpty){
      String leadURL = options.first;
      line.success("add lead $leadURL");

    } else {
      throw "A lead should be specified by URL ending .../lead";

    }
  }

  Future<void> _removeLead(List<String> options) async {
    String leadUID = options.first;
    line.success("remove lead " + leadUID);
  }

}

void _printTargetUsage(Object e) {
  line.warn('''
No command 'target $e': valid sub-commands are:
  testnet [true|false]    :   set the cli context to test net or main net 
  lead [add | remove]   :   add or remove a lead by URL
  ''');
}