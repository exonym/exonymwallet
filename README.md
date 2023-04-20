# Status
> 🛠️  : In development

Please see [docs.exonym.io](https://docs.exonym.io) for full-documentation.

_NB: we've not yet Snyk'd this repo!_

# Exonym Wallet Related Projects
Exonym and Decentralized Rulebooks together create a decentralized governance framework that 
enables trustworthy transactions without centralized control. Rulebooks are created for specific 
topics and can be joined by anyone interested. The Rulebook has a focused yet subjective Rulebook 
Document, which is extended and interpreted by Sources and Advocates. 

A Source references a set of baseline rules that are immutable and inherited by its chosen Advocates. 
When a consumer subscribes to a Rulebook, they agree to follow its rules when the Rulebook applies. 
This subscription model, combined with the ability for anyone to create and Advocate for a Rulebook, 
makes the system of rulebooks permissionless. Producers join the rulebook at an Advocate whose 
interpretation is closest to their own values and prove "current honesty" under it to Consumers.

Decentralized Rulebooks hold great potential for creating an Internet governance model that is 
transparent, accountable, and community-driven, and that can be scaled up and implemented cost-effectively. 

This project responsible for managing Exonym wallets that are designed to facilitate user accountability 
by rooting governance for a specific Internet activity in a public, immutable rulebook document.

Each Rulebook requires Producers to onboard to Exonym and Sybil, while Consumers are not required to onboard.

Exonym is an identity system that facilitates anonymity. Exonym's Sybil service resists 
unauthorized clone accounts and breaks the link between a user's real-world identity and 
their Exonym identity. Exonym, in combination with Sybil, is used to govern clone accounts, 
actively managing the governance of their activities and other capabilities. 

Rulebooks govern where they are applied and wanted by consumers. By working together, Exonym and 
Sybil, along with Rulebooks, allow for a new world of transactional trust where 
transactional utility is separate from transactional governance, and governance is owned by interested web users.

## Audience
Developers who want to extend Exonym Wallet features.

> __Warning:__ This development pipeline is necessarily complex.

## Pre-requisites
Superficial knowledge of: 
- [Exonym's Decentralised Rulebooks](https://docs.exonym.io).
- IBM Research's Identity Mixer.

## Background

Identity Mixer is written in Java, which does not run on iOS devices.  Decentralised 
cryptographic systems require the user to take ownership of their data.  This can either 
be done by each user having their own web space, or they can store the information on a 
device they have with them.  Given that user's don't do 'paying for software', a personal 
device really is the only option. 

Oracle developed GraalVM's `native-image`, which allows for compilation of Java into C-callable 
libraries and so the majority of this project has been structured around a pipeline that will 
deploy to iOS.  It isn't easy to kick GraalVM to target the `ios-arm64` architecture, but the 
good people at __GluonHQ__ have bespoke Graal JVMs and a Maven Plug-in that completes the 
deployment pipeline to the iOS App Store.

`native-image` needs to know what a program is going to do at compile time.  This means that 
reflections need to be configured in advance.  The most recent version of Identity Mixer `3.0.36` 
uses Google's `guice` dependency injection framework, which doesn't work with `native-image` 
because it does too much at runtime.  Google have another dependency injection framework 
called `Dagger2`, which requires a bit more set-up; but means that Identity Mixer will 
successfully compile to a native image with the correct reflection configuration file.  `idmx-graalvm` 
produces the `idmx-3.1.0` artifacts, which uses Dagger and is compatible with `3.0.36`.

While Dagger is faster than Guice when running on a JVM and a native image will run about 10 times 
faster than anything running on a JVM (it does on my machine anyway ;-)); 

___

One reason Exonym took so long to deliver (6 years full-time), was that my focus was on solving 
the issues of decentralised governance.  This was in the hope that when Hyperledger Indy was 
delivered it would have all the features that Identity Mixer had and we would just build a production 
version on Indy.  Exonym is a layer-2 system after all, so it wouldn't make any difference to us. 
Well, they _were_ going to do that... if you look at their code, there was a library called `libzmix`, 
but they mothballed it for whatever reason.  

In short, Identity Mixer has features that Exonym needs and that Indy does not have.  It is a much richer 
library than its counterparts, it was paid for by the EU taxpayer and it should be used more than it is.

Saying that; Identity Mixer may be feature rich, but standards also exist.  For this reason Exonym does 
not offer bespoke Attribute Based Credentials.  If you want ABCs, there are plenty of projects out there 
that follow W3C standards that you _should be using instead_.

I am certain that one day Identity Mixer will be replaced; it probably should be to increase verification 
times; but today, that doesn't matter.  What matters is that we figure out a way of making
the web safe to use without turning every successful company into a global government of some aspect
of our lives.

# Projects Overview

```
exonymwallet_cli    <<      exonymwallet;
exonymwallet_app    <<      exonymwallet;

exonymwallet        <<      libexonymwallet     <<      idmx-graalvm;
```

Each has their own README.md.

### idmx-graal
Identity Mixer with Dagger2 dependency injection. 

### libexonymwallet
Identity Mixer with `ExonymActor` support classes, compiled to a C-callable executable targeted at: `aarch-arm64` and `ios-arm64`

###  exonymwallet
A Dart package that wraps `libexonymwallet` and manages its threads.

###  exonymwallet_cli
Dart program that uses `package:exonymwallet` to implement the Exonym-CLI. 

###  exonymwallet_app
Flutter App that uses `package:exonymwallet` to implement the Exonym Wallet for iOS and Android.

# Installation
These instructions are for IntelliJ IDEA, but you can do the equivalent in your own IDE.

## Requirements

```
macos with xcode ^11
maven ^3 
graalvm-svm-java11-darwin-m1-gluon-22.1.0.1-Final
```
Must be Java11 (and not 17) but choose the relevant architecture.

Set the environment variables:
```
GRAALVM_HOME="your-path/graalvm-svm-java11-darwin-m1-gluon-22.1.0.1-Final/Contents/Home"
PATH="your-path/graalvm-svm-java11-darwin-m1-gluon-22.1.0.1-Final/Contents/Home/bin:$PATH"
JAVA_HOME="your-path/graalvm-svm-java11-darwin-m1-gluon-22.1.0.1-Final/Contents/Home"
```

## Install
From the root of the parent project.

```
cd idmx-graal
mvn install
```

Open `libexonymwallet` in IntelliJ 
- press `cmd+;` to launch the project properties.
- select `+` then `JAR~From modules with dependencies`
- select `libexonymwallet` from the Module combo.
- add `io.exonym.wallet.GraalVMProbeMain` as the main class.
- select `OK`
- check that the output directory is `...out/artifacts/libexonymwallet_jar`
- select `OK`
- from the main menu `Build~Build Artifacts...` then `Build` on the subsequent combo.

```
cd out/artifacts/libexonymwallet_jar

./create_meta_inf.sh libexonymwallet.jar
```

___
Open `/META-INF/native-image/reflect-config.json` and use the following regular expression to remove all instances that it finds.

```
    {\n.*Jaxb.*\n.*\n},
```

_If you don't do this step, you just get Warnings from GraalVM that can be ignored._
___

Run the shell script and then the gluonfx maven job.
```
./copy_to_libexonymwallet.sh
cd ../../../
mvn gluonfx:sharedlib
mvn gluonfx:sharedlib -Pios
```

On success, you can build and test the Dart wrapper.
```
cd ../exonymwallet
dart pub get
dart test
```

___
__FFIGEN__
If you need to rebuild the FFI class, edit the `config.yaml` to reference the correct architecture in the `libexonymwallet` folder and then run:
```
dart run ffigen --config config.yaml
```
You may also need to update the `ExonymWallet` class in `exonym_wallet.dart`.
___

> TODO: CLI and APP


_______

__&copy; 2023 Exonym GmbH__

This documentation is licensed under the Mozilla Public License, version 2.0 (the "License"); you may not use this file except in compliance with the License.

You may obtain a copy of the License at https://www.mozilla.org/en-US/MPL/2.0/.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
