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

      } else if (selected.choice == TargetMenu.source){

        var parts = ChoiceAndOptions<TargetMenuAction>(
            selected.options.removeAt(0), TargetMenuAction.values);
        print("Choice = ${selected.options}");

        if (parts.choice == TargetMenuAction.add){
          _addSource(selected.options);

        } else if (parts.choice == TargetMenuAction.remove){
          _removeSource(selected.options);

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

  Future<void> _addSource(List<String> options) async {
    if (!options.isEmpty){
      String sourceURL = options.first;
      line.success("add source $sourceURL");

    } else {
      throw "A source should be specified by URL ending .../x-source";

    }
  }

  Future<void> _removeSource(List<String> options) async {
    String sourceUID = options.first;
    line.success("remove source " + options.first);
  }

}

void _printTargetUsage(Object e) {
  line.warn('''
No command 'target $e': valid sub-commands are:
  testnet [true|false]    :   set the cli context to test net or main net 
  source [add | remove]   :   add or remove a source by URL
  ''');
}