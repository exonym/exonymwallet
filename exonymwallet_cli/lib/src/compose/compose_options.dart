import 'package:build_cli_annotations/build_cli_annotations.dart';

part 'compose_options.g.dart';

ArgParser get parser => _$populateComposeOptionsParser(ArgParser(usageLineLength: 120));

@CliOptions()
class ComposeOptions{

  // @CliOption(
  //   name: 'source',
  //   abbr: 's',
  //   help: 'The source URL to add or remove.',
  // )
  // final String? sourceURL;
  //
  // @CliOption(
  //   name: 'honesty',
  //   abbr: 'h',
  //   help: 'The Advocate to use for the proof of honesty if more than one '
  //       'exists and is required.  The test net does not require a proof and'
  //       ' the UID is only required, if the wallet is honest under more than one.',
  // )
  // final String? advocateUID;


  @CliOption(
    name: 'input',
    abbr: 'i',
    help: 'The input csv Rulebook file to solidify.',
  )
  final String? input;

  @CliOption(
    name: 'output',
    abbr: 'o',
    help: 'The generated solidified Rulebook.',
  )
  final String? output;

  ComposeOptions(this.input, this.output);

}