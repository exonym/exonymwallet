import 'package:exonymwallet_cli/src/context/console_decoration.dart' as line;
import 'package:exonymwallet_cli/src/prove/delegate.dart';

import '../context/context_tracker.dart';
import '../context/menus.dart';

class ProveCommands{

  final Delegate delegateMenu = Delegate();

  execute(List<String> commandAndOptions) async {
    try {
      cliContext.rejectUnauthenticated();
      var selected = ChoiceAndOptions<ProveMenu>(
          commandAndOptions.removeAt(0), ProveMenu.values);

      selected.options = commandAndOptions;

      if (selected.choice == ProveMenu.report) {
        await _report(selected.options);

      } else if (selected.choice == ProveMenu.sso){
        await _sso(selected.options);

      } else if (selected.choice == ProveMenu.static){
        await _staticProof(selected.options);

      } else if (selected.choice == ProveMenu.delegate){
        delegateMenu.execute(commandAndOptions);

      } else {
        throw Exception("Unexpected");
      }
    } catch (e) {
      print(e);
      _printUsage(e);
    }
  }

  //
  _staticProof(List<String> options) async {
    final name = cliContext.containerName;
    final pwd = cliContext.containerPassword;
    final path = cliContext.rootPath();
    var r = await exonymWallet.nonInteractiveProofRequest(name!, pwd!,
        options[0], path);
    line.success(r);

  }

  _report(List<String> options) async {
    final name = cliContext.containerName;
    final pwd = cliContext.containerPassword;
    final path = cliContext.rootPath();
    var r = await exonymWallet.authenticationReport(name!, pwd!,
        options[0], path);
    line.success(r);

  }

  _sso(List<String> options) async {
    final name = cliContext.containerName;
    final pwd = cliContext.containerPassword;
    final path = cliContext.rootPath();
    var r = await exonymWallet.proofForRulebookSSO(name!, pwd!,
        options[0], path);
    line.success(r);

  }

}

void _printUsage(Object e) {
  line.warn('''
No command 'prove $e': valid sub-commands are:
  report     :   report on the provability of an authentication request. 
  sso        :   fill an SSO request. 
  static     :   generate an inspectable proof token and publish it.
  delegate   :   delegate service privilege via an endonym. 
  ''');
}