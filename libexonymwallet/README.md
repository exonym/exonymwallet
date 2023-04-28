# `libexonymwallet`

## About

`libexonymwallet` enables the creation and management of secure 
wallets that are essential for interacting with the Decentralized 
Rulebooks system. 

Decentralized Rulebooks are a distributed software system that facilitates 
multi-stakeholder governance, ensuring trust and accountability in online 
activities without the need for centralized control.

`libexonymwallet` makes it possible to create secure wallets that are 
capable of managing the complex transactions and activities that are 
inherent in the governance of online activities. The library offers a 
range of features that are essential for the effective management of 
wallets, including secure storage of private keys, SFTP credentials, 
the creation of new wallets, retrieval of advocate lists, and management 
of transactions.

`libexonymwallet` is used by our command-line utility and device App 
so that producers and consumers can interact with the Decentralized 
Rulebooks system seamlessly. This makes it possible for users to 
easily participate in the governance of online activities, without 
the need for specialized technical knowledge or experience. 

`libexonymwallet` is responsbile for making sure that the user's wallet 
is secure, and their transactions are private and tamper-proof, enabling 
them to participate in the Decentralized Rulebooks system with confidence.

# WalletAPI.class
The `WalletAPI.class` provides an essential component of the Rulebooks system by 
implementing a C-Callable interface through the use of __Gluon__ and __GraalVM__. This powerful 
virtual machine enables seamless interoperability between different programming 
languages and platforms.

In order to maintain the integrity and security of wallet data, especially considering 
the large volume of information required for Identity Mixer operations, the WalletAPI.class 
avoids returning structs. This design choice minimizes the risk of data corruption that 
could render wallets unusable. Instead, the WalletAPI.class focuses on providing a 
reliable and efficient interface for the Decentralized Rulebooks ecosystem, ensuring 
smooth interaction between various components and enabling deployment to iOS devices.

`libexonymwallet` serves a dual purpose as it is utilized as a jar file by services 
that aim to verify proofs generated within the Decentralized Rulebook system. This 
functionality ensures that the verification process is streamlined and easily accessible 
for a wide range of services. By incorporating `libexonymwallet` as a jar file, these 
services can efficiently validate proofs and maintain the security and trust within the 
Rulebooks ecosystem. 

`openSystemParams()` A test function to ensure that serialization is functioning correctly,  
that resources are being packaged properly, and they are available.

`newRulebook()`Takes a `.description` file and a `.rulebook` file, then solidifies a new rulebook.


`extendRulebook()`Takes a .rulebook file and generates valid identifiers for the rules within.

`walletReport()`
Takes the open wallet and produces a report on the wallet's status.

`spawnNetworkMap()`
Takes the root Trust Network document and computes a current valid network map.

`listRulebooks()`
Lists the identifiers of all the rulebooks in existence.

`viewActor()`
Produces a network map item for the identified actor from the network map.

`listActors()`
Produces a list of actors that are the sources for a rule, book, or advocates for a source.

`networkMap()`
Generates a network map based on the current state of the system.

`authSummaryForUniversalLink()`
Takes a universal link and produces a report based on the open wallet. The report will indicate whether the request can be fulfilled and highlight any rulebooks where the user cannot prove honesty.

`nonInteractiveProofRequest()`
Creates a non-interactive proof for publishing to a third-party location. Non-interactive proofs are always anonymous and therefore cannot contain pseudonyms.

`proofForRulebookSso()`
Produces a token for a single sign-on request and sends it to the /exonym endpoint. If the endpoint is unavailable, the function will fail. If available, the user should gain access where it has been requested. A single pseudonym must start with the domain name it will be sent to and be in HTTPS format.

`generateDelegationRequestForThirdParty()`
On receipt of a delegation request from a service, the user's wallet will call this function to generate a delegation request for the service.

`fillDelegationRequest(delegation_request)`
The third-party takes the delegation request and calls this function.

`verifyDelegationRequest(filled_delegation_request)`
The person who generated the delegation request takes the filled delegation request, verifies it, and produces an endonym.

`sftpTemplate()`
Produces an SFTP template in Excel format for the user to complete.

`addSftpTemplate()`
Once the template is complete, the user calls this function to add it to the open wallet.

`sftpRemove()` Removes the identified SFTP template.

`onboardSybilTestnet()` Executes the adoption protocol for the Sybil Test Net.

`onboardRulebook()` Executes the adoption protocol for a permissionless Advocate, this function works via the universal link and is designed for use with the smartphone app.

`onboardRulebookAdvocateUID(advocate_UID)`
Executes the adoption protocol for a permissioned advocate via the advocate UID.

`authenticate()`
Authenticates an existing wallet to open it.

`setupWallet()`
Sets up a new wallet with the given unique username and strong password.

`deleteWallet()`
Deletes the identified wallet. 

`generateResetProof()`
Generates a reset proof. Currently doesn't work, intended for a future release.

`sha256AsHex()`
Generates a hash as hex.

`openPassStore()`
Opens the PassStore in a set format.

`handleError()`
Handles all errors sent back.
_______

__&copy; 2023 Exonym GmbH__

This documentation is licensed under the Mozilla Public License, version 2.0 (the "License"); you may not use this file except in compliance with the License.

You may obtain a copy of the License at https://www.mozilla.org/en-US/MPL/2.0/.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
