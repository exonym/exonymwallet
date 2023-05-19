enum TopMenu {
  prove,
  net,
  wallet,
  rulebook,
  target,

}

enum WalletMenu {
  create,
  open,
  close,
  get,
  list,
  sftp,

}

enum SftpMenu {
  template,
  add,
  remove
}

enum ProveMenu {
  report,
  sso,
  static,
  delegate,

}

enum ProveDelegateMenu {
  generate,
  verify,
  fill

}

enum TargetMenu {
  testnet,
  source,

}

enum TargetMenuAction {
  add,
  remove,
}

//
enum NetMenu {
  spawn,
  rulebooks,
  sources,
  advocates,
  add_source

}

enum RulebookMenu {
  create,
  extend

}

class ChoiceAndOptions<T> {

  late T choice;
  late List<String> options;

  ChoiceAndOptions(String input, Iterable<T> values){
    var opts = input.split(" ");
    choice = _selectCommand<T>(opts.removeAt(0), values) as T;
    options = opts;

  }

  T _selectCommand<T>(String item, Iterable<T> values){
    return values.firstWhere(
          (value) => value.toString().split('.')[1] == item,
          orElse: () => throw "$item");

  }

}


void printGlobalUsage() {
  print('''
A command line utility for managing Exonym Wallet containers off-device.

Usage: exonym [<commands>] [<args>]

Top-level-commands:

  prove     :   Authentication commands for the open wallet.
  net       :   Navigate rulebooks, sources, and advocates.
  wallet    :   Wallet management.
  rulebook  :   Solidify and extend verifiable rulebooks based on rules and description files.
  target    :   Test net settings.

''');
}
