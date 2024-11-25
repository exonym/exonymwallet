# `exonymwallet`

> **This library is currently in the early stages of development.**

The `exonymwallet` bridging library provides a bridge between the 
`libexonymwallet` C-callable library and Dart, allowing developers macOS
to easily access the features provided by `libexonymwallet` in their 
Dart applications. 

Developers can create and manage Exonym identity wallets, solidify rulebooks, and interact with Decentralized Rulebooks directly from their Dart code. By using the 
`exonymwallet` lib, developers can harness the power of 
Exonym and Decentralized Rulebooks in their Dart applications and 
create decentralized, rulebook applications with ease.

## Supported Platforms
macOS (Apple Silicon, x86)

### Coming soon
- iOS  (build from source if you want it now)
- Windows

# Installation

```yaml
dependencies:
  exonymwallet: ^0.1.0
```

Now run

```
dart test
```

> N.B. if you've already run the tests, you need to delete the `identities` folder.

## Next Steps in Development

This library will be enhanced soon by introducing the use of Dart objects instead of working with string representations. This improvement will offer more flexibility and convenience when utilizing the library's functionalities.

Additionally, we plan to incorporate exception handling to provide more meaningful error messages and make it easier to manage and handle exceptions when interacting with the library.

We appreciate your patience and understanding as we continue to evolve and enhance this library. Your feedback and contributions are highly valued and will help shape its future development.

## License
__&copy; Copyright 2023 Exonym GmbH__

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

1. The software is not modified to interfere with its consumption of the service provided by Exonym GmbH to issue portable credentials, initially named "Sybil" and initially available via the website at https://exonym.io/.
2. The above copyright notice and this permission notice is included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
