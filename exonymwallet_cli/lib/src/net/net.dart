import 'package:exonymwallet_cli/src/context/console_decoration.dart' as line;

import '../context/context_tracker.dart';
import '../context/menus.dart';

class NetCommands{

  execute(List<String> commandAndOptions) async {
    try {

      var selected = ChoiceAndOptions<NetMenu>(
          commandAndOptions.removeAt(0), NetMenu.values);

      selected.options = commandAndOptions;

      if (selected.choice == NetMenu.spawn) {
        await _spawn();

      } else {
        String action = selected.options.length > 0 ? selected.options[0] : "";
        String subject = selected.options.length > 1 ? selected.options[1] : "";
        print("" + action + " " + subject);

        if (selected.choice == NetMenu.rulebooks){
          await _rulebooks(subject, action);
        } else if (selected.choice == NetMenu.leads){
          await _leadsOrModerators(subject, action);
        } else if (selected.choice == NetMenu.mods){
          await _leadsOrModerators(subject, action);
        } else if (selected.choice == NetMenu.add_lead){
          await _addSourceToSybilNode(action);
        } else {
          throw Exception("Unexpected");
        }
      }
    } catch (e) {
      print(e);
      _printTargetUsage(e);

    }
  }

  Future<void> _rulebooks(String subject, String action) async{
    if (action == "list"){
      if (subject==""){
        var path = await cliContext.rootPath();
        var r = await exonymWallet.listRulebooks(path);
        line.success(r);

      } else {
        var path = await cliContext.rootPath();
        var r = await exonymWallet.listActors(subject, path);
        line.success(r);

      }
    } else if (action == "view"){
      var path = await cliContext.rootPath();
      var r = await exonymWallet.viewActor(subject, path);
      line.success(r);

    } else {
      var path = await cliContext.rootPath();
      var r = await exonymWallet.listRulebooks(path);
      line.success(r);

    }
  }

  //
  Future<void> _leadsOrModerators(String subject, String action) async{
    if (action=="list"){
      var r = await exonymWallet.listActors(subject, cliContext.rootPath());
      line.success(r);

    } else if (action=="view"){
      var r = await exonymWallet.viewActor(subject, cliContext.rootPath());
      line.success(r);

    } else {
      throw "[leads || mods] [view || list]";

    }
  }

  Future<void> _spawn() async{
    exonymWallet.spawnNetworkMap(cliContext.rootPath());

  }

  Future<void> _addSourceToSybilNode(String url) async {
    try {
      cliContext.rejectUnauthenticated();
      if (cliContext.testNetwork){
            var r = await exonymWallet.leadListTest(url);
            line.success(r);

          } else {
            line.error("There is no production network - see https://exonym.io for more information");

          }
    } catch (e) {
      line.error("$e");

    }
  }
}

void _printTargetUsage(Object e) {
  line.warn('''
No command 'net $e': valid sub-commands are:
  spawn      : force the network map to spawn from scratch 
  rulebooks  : navigate rulebooks - sub-commands [list, view]
  leads      : navigate leads - sub-commands [list, view]
  mods       : navigate moderators  - sub-commands [list, view]
  add_lead   :  broadcasts a new source to be accepted onto the network - proof required for production and so an open wallet is necessary.
  ''');
}