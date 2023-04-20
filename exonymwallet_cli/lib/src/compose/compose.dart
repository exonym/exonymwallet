
import '../../exonymwallet_cli.dart';
import 'dart:async';
import 'dart:io';
import 'dart:convert';

@Deprecated("kept for an example of cl args, but this is now 'rulebook'")
class Compose {

  late ComposeOptions options;

  Compose(List<String> options){
    try {
      this.options = parseComposeOptions(options);
      _validate();
      _execute();

    } catch (message) {
      print("Error: ${message}");
      _printUsage();

    }
  }

  void _validate() {
    if (options.input == null || options.output==null){
      throw "'compose' requires an input and an output file.";

    }
  }

  void _execute() {
    print("Reading from `${options.input}` and writing to `${options.output}`");
    var path = options.input!;
    new File(path)
        .openRead()
        .map(utf8.decode)
        .transform(new LineSplitter())
        .forEach((l) => print('line: $l'));

  }
}


void _printUsage(){
  print('''
Usage: compose -i <input_file> -o <output_file>

Arguments:
${parser.usage}
''');
}
