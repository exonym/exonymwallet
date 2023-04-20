// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'compose_options.dart';

// **************************************************************************
// CliGenerator
// **************************************************************************

ComposeOptions _$parseComposeOptionsResult(ArgResults result) => ComposeOptions(
      result['input'] as String?,
      result['output'] as String?,
    );

ArgParser _$populateComposeOptionsParser(ArgParser parser) => parser
  ..addOption(
    'input',
    abbr: 'i',
    help: 'The input csv Rulebook file to solidify.',
  )
  ..addOption(
    'output',
    abbr: 'o',
    help: 'The generated solidified Rulebook.',
  );

final _$parserForComposeOptions = _$populateComposeOptionsParser(ArgParser());

ComposeOptions parseComposeOptions(List<String> args) {
  final result = _$parserForComposeOptions.parse(args);
  return _$parseComposeOptionsResult(result);
}
