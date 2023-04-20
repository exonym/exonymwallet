import 'package:exonymwallet_cli/src/context/console_decoration.dart' as line;

import '../context/context_tracker.dart';
import '../context/menus.dart';

class RulebookCommands{

  execute(List<String> commandAndOptions) async {
    try {

      var selected = ChoiceAndOptions<RulebookMenu>(
          commandAndOptions.removeAt(0), RulebookMenu.values);

      selected.options = commandAndOptions;

      if (selected.choice == RulebookMenu.create) {
        await _create(selected.options);

      } else if (selected.choice == RulebookMenu.extend) {
        throw "NOT_IMPLEMENTED";


      }
    } catch (e) {
      print(e);
      _printTargetUsage(e);

    }
  }

  Future<void> _create(List<String> options) async{
    if (options.isNotEmpty){
      String name = options[0];
      exonymWallet.newRulebook(name, cliContext.rootPath());

    } else {
      throw "NAME_REQUIRED";

    }
  }

}

void _printTargetUsage(Object e) {
  line.warn('''
No command 'rulebook $e': valid sub-commands are:
  create      :   create a rulebook from .rules and .description files 
  extend      :   extend an existing rulebook with a .rules file
  ''');
}