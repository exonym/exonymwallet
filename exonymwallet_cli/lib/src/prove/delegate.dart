
import 'package:exonymwallet_cli/src/context/console_decoration.dart' as line;

import '../context/context_tracker.dart';
import '../context/menus.dart';

class Delegate{

  execute(List<String> commandAndOptions) async {
    try {
      var selected = ChoiceAndOptions<ProveDelegateMenu>(
          commandAndOptions.removeAt(0), ProveDelegateMenu.values);

      selected.options = commandAndOptions;

      // print("Got something - ${selected.options}");
      if (selected.choice == ProveDelegateMenu.generate){
        await _generate(selected.options);

      } else if (selected.choice == ProveDelegateMenu.verify){
        await _verify(selected.options);

      } else if (selected.choice == ProveDelegateMenu.fill){
        await _fill(selected.options);

      }
    } catch (e) {
      print(e);
      _printUsage(e);

    }
  }

  _generate(List<String> options) async {
    final name = cliContext.containerName;
    final pwd = cliContext.containerPassword;
    final path = cliContext.rootPath();
    var r = await exonymWallet.generateDelegationRequestForThirdParty(name!, pwd!, options[0], options[1], path);
    line.success(r);

  }

  _verify(List<String> options) async {
    final name = cliContext.containerName;
    final pwd = cliContext.containerPassword;
    final path = cliContext.rootPath();
    var r = await exonymWallet.verifyDelegationRequest(name!, pwd!, options[0], options[1], path);
    line.success(r);


  }

  _fill(List<String> options) async {
    final name = cliContext.containerName;
    final pwd = cliContext.containerPassword;
    final path = cliContext.rootPath();
    var r = await exonymWallet.fillDelegationRequest(name!, pwd!, options[0], path);
    line.success(r);

  }
}

void _printUsage(Object e) {
  line.warn('''
No command 'prove delegate $e': valid sub-commands are:
  generate    :   generate a request for a third-party 
  fill        :   fill a generated request
  verify      :   verify a filled request 
  ''');
}