import 'dart:convert';
import 'dart:ffi';
import 'dart:io';
import 'package:path/path.dart' as path;
import 'package:ffi/ffi.dart';
import 'generated/exonym_wallet_bindings.dart';
import 'package:exonymwallet/src/resource_management/closable.dart';
import 'model/global.dart' as global;

class ExonymWallet extends AbstractResource {


  final Pointer<graal_create_isolate_params_t> _params
    = calloc<graal_create_isolate_params_t>();

  final Pointer<Pointer<graal_isolate_t>> _isolate
    = calloc<Pointer<graal_isolate_t>>();

  final Pointer<Pointer<graal_isolatethread_t>> _thread
    = calloc<Pointer<graal_isolatethread_t>>();

  ExonymWalletLibrary? _lib;

  ExonymWallet(){
    var libraryPath = path.join(
        Directory.current.path, global.LIB_FOLDER, global.LIB_FOLDER_MACOS_X86, global.LIB_NAME);
    if (Platform.isIOS) {
      libraryPath = global.LIB_NAME;
      print("Platform=iOS : ensure $libraryPath is available - xcode~Targets~General~Frameworks, Libraries, and Embedded Content");
      print("If an error message follows, the lib was not found.  If you see the Exonym message - the library was found.");

    }
    var libPath;
    try {
      libPath = DynamicLibrary.open(libraryPath);
    } catch (e) {
      libraryPath = path.join(
          Directory.current.path, global.LIB_FOLDER, global.LIB_FOLDER_MACOS_M1, global.LIB_NAME);
      print(libPath);
      libPath = DynamicLibrary.open(libraryPath);

    }
    _lib = ExonymWalletLibrary(libPath);
    _lib!.graal_create_isolate(_params, _isolate, _thread);
    final int twelve = _lib!.hello_exonym(_getThread());
    if (twelve==12){
      print("© 2023 - Exonym - Network Authentication Solutions");

    } else {
      print("An error occured - libexonymwallet did not connect - check the .dylib is present: $libraryPath");

    }
  }

  ping(){
    _lib!.hello_exonym(_getThread());
  }

  /**
   * Opens the system parameters for testing serialisation and availability.
   *
   * Used to test the serialisation process and ensure the availability
   * of system parameters, which are basic entry criteria for the system to work correctly.
   *
   * @return A Future that resolves to a String representing the serialised system parameters.
   */
  Future<String> openSystemParams() async {
    return global.fromCString(_lib!.open_system_params(_getThread()));
  }


  /**
   * Creates a new rulebook document based on the provided name and path.
   *
   * This function expects to find both a "<name>.rulebook" and a "<name>.description" file
   * in the root of the specified path directory. It generates a "rulebook.json" file
   * as a result.
   *
   * The generated "rulebook.json" file serves as an immutable rulebook document once a user
   * subscribes to it for the first time.
   *
   * @param name The name of the rulebook.
   * @param path The root path directory where the rulebook files are located.
   * @return A Future that resolves to a String representing the result of the operation.
   */
  Future<String> newRulebook(String name, String path) async {
    return global.fromCString(
        _lib!.new_rulebook(_getThread(),
            global.toCString(name),
            global.toCString(path)
        )
    );
  }

  /**
   * Creates an SFTP template document at the specified path.
   *
   * This function generates an SFTP template document, named "sftp-template.xml", at the provided path.
   * The purpose of this template is to provide a starting point for the user to complete the required
   * data before calling the `sftpAdd` function.
   *
   * @param path The root path directory where the SFTP template document will be created.
   * @return A Future that resolves to a String representing the result of the operation.
   *
   */
  Future<String> sftpTemplate(String path) async {
    return global.fromCString(
        _lib!.sftp_template(_getThread(),
            global.toCString(path)
        )
    );
  }


  /**
   * Adds an encrypted SFTP template document to the user's Open Wallet.
   *
   * This function reads the SFTP template document created using the `sftpTemplate()` function
   * and encrypts it into the user's Open Wallet. It requires the user's Open Wallet to be already open.
   * If the wallet is not open, the function will reject the request.
   *
   * The function takes three parameters:
   * - `username`: The username associated with the SFTP connection.
   * - `passwordAsSha256Hex`: The password for the SFTP connection, represented as a SHA-256 hashed hexadecimal string.
   * - `path`: The path of the SFTP template document that needs to be added to the user's Open Wallet.
   *
   * N.B The template file can and should be delete it afterwards.
   *
   * The contents of the SFTP credential cannot be retrieved.  It can only be used.
   *
   * @param username The username for the SFTP connection.
   * @param passwordAsSha256Hex The password for the SFTP connection, represented as a SHA-256 hashed hexadecimal string.
   * @param path The path of the SFTP template document to be added to the user's Open Wallet.
   * @return A Future that resolves to a String representing the result of the operation.
   * The result indicates whether the addition of the SFTP template was successful or not.
   * If the wallet is not open, the request will be rejected.
   */
  Future<String> sftpAdd(String username, String passwordAsSha256Hex, String path) async {
    return global.fromCString(
        _lib!.sftp_add(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(path)
        )
    );
  }

  /**
   * Uploads XML or JSON files to a specified location using SFTP credentials.
   *
   * This function uses SFTP credentials to write XML or JSON files to the specified location.
   * It requires the following parameters:
   * - `username`: The username associated with wallet.
   * - `passwordAsSha256Hex`: The password for the wallet, represented as a SHA-256 hashed hexadecimal string.
   * - `sftpCredentialUID`: The unique identifier for the SFTP credential to be used.
   * - `fileName`: The name of the file to be uploaded.
   * - `token`: The token required for authentication.
   * - `remotePath`: The path where the file will be uploaded on the remote server.
   * - `path`: The local path of the file to be uploaded.
   *
   * @param username The username associated with the wallet
   * @param passwordAsSha256Hex The password for the wallet, represented as a SHA-256 hashed hexadecimal string.
   * @param sftpCredentialUID The unique identifier for the SFTP credential to be used.
   * @param fileName The name of the file to be uploaded.
   * @param token The authentication token required for the upload process.
   * @param remotePath The path on the remote server where the file will be uploaded.
   * @param path The local path of the file to be uploaded.
   * @return A Future that resolves to a String representing the result of the operation.
   *         The result indicates whether the file upload was successful or not.
   */
  Future<String> sftpPut(String username, String passwordAsSha256Hex,
        String sftpCredentialUID,
        String fileName,
        String token,
        String remotePath,
        String path) async {
    return global.fromCString(
        _lib!.sftp_put(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(sftpCredentialUID),
            global.toCString(fileName),
            global.toCString(token),
            global.toCString(remotePath),
            global.toCString(path)
        )
    );
  }

  /**
   * Removes an SFTP credential from the wallet based on its URN reference.
   *
   * This function removes an SFTP credential from the wallet by referencing its URN (Uniform Resource Name).
   * The following parameters are required:
   * - `username`: The username associated with the wallet.
   * - `passwordAsSha256Hex`: The password for the wallet, represented as a SHA-256 hashed hexadecimal string.
   * - `name`: The URN of the SFTP credential to be removed.
   * - `path`: The path to the wallet where the SFTP credential is stored.
   *
   * @param username The username associated with the wallet
   * @param passwordAsSha256Hex The password for the wallet, represented as a SHA-256 hashed hexadecimal string.
   * @param name The URN (Uniform Resource Name) of the SFTP credential to be removed.
   * @param path The path to the wallet where the SFTP credential is stored.
   * @return A Future that resolves to a String representing the result of the operation.
   *         The result indicates whether the removal of the SFTP credential was successful or not.
   */
  Future<String> sftpRemove(String username, String passwordAsSha256Hex, String name, String path) async {
    return global.fromCString(
        _lib!.sftp_remove(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(name),
            global.toCString(path)
        )
    );
  }

  /**
   * Adds a Source to the trustworthy source list for the test network.
   *
   * This function adds a Source referenced by the provided URL to the trustworthy source list
   * specifically for the test network. It ensures that only valid and trusted sources are included.
   *
   * The function takes one parameter:
   * - `url`: The URL referencing the Source to be added.
   *
   * Please note that this function rejects invalid sources, including those that reference invalid rulebooks.
   *
   * @param url The URL referencing the Source to be added to the trustworthy source list.
   * @return A Future that resolves to a String representing the result of the operation.
   *         The result indicates whether the addition of the Source to the trustworthy source list was successful or not.
   */
  Future<String> sourceListTest(String url) async {
    return global.fromCString(
        _lib!.source_list_test(_getThread(),
            global.toCString(url)
        )
    );
  }

  /**
   * Generates a report of all credentials stored in the referenced wallet.
   *
   * This function produces a report that includes information about all the credentials present
   * in the specified wallet. It requires the following parameters:
   *
   * The generated report provides a comprehensive overview of the credentials stored in the wallet,
   * including relevant details and metadata associated with each credential.
   *
   * @param username The username associated with the wallet.
   * @param passwordAsSha256Hex The password for the wallet, represented as a SHA-256 hashed hexadecimal string.
   * @param path The path to the working directory
   *
   * @return A Future that resolves to a String representing the generated report of the credentials in the wallet.
   */
  Future<String> walletReport(String username, String passwordAsSha256Hex, String path) async {
    return global.fromCString(
        _lib!.wallet_report(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(path)
        )
    );
  }

  /**
   * Generates an authentication report to determine the user's ability to authenticate a specific request.
   *
   * This function creates an authentication report that determines whether or not the user can authenticate
   * the requested authentication. It provides detailed information about the sources or advocates that the user
   * can potentially subscribe to in order to fulfill the authentication request.
   *
   * @param username The username associated with the user's wallet.
   * @param passwordAsSha256Hex The password for the wallet, represented as a SHA-256 hashed hexadecimal string.
   * @param request The authentication request that needs to be evaluated.
   * @param path The path to the root of the working directory sub-paths are computed.
   * @return A Future that resolves to a String representing the authentication report.
   *         The report contains information about the user's ability to authenticate the request
   *         and details the potential sources or advocates that can be subscribed to for successful authentication.
   */
  Future<String> authenticationReport(String username, String passwordAsSha256Hex,
      String request, String path) async {
    return global.fromCString(
        _lib!.authentication_report(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(request),
            global.toCString(path)
        )
    );
  }

  /**
   * Updates the local network map manually.
   *
   * This function updates the local network map, which is typically done automatically and triggered
   * when information is not found. However, if a manual update is desired, this function can be used to
   * force an update.
   *
   * Upon invocation, the function updates the network map and returns a result indicating the success
   * or failure of the update process.
   *
   * @param path The path to the root of the working directory sub-paths are computed.
   * @return A Future that resolves to a String representing the result of the update process.
   *         The result indicates whether the update was successful or not.
   */
  Future<String> spawnNetworkMap(String path) async {
    return global.fromCString(_lib!.spawn_network_map(_getThread(), global.toCString(path)));
  }

  /**
   * Retrieves a detailed view of an actor and their interpretation of the rulebook.
   *
   * This function retrieves a detailed view of an actor and their interpretation of the rulebook.
   *
   * The function returns a result that represents the detailed view of the actor, including their interpretation
   * of the rulebook and any relevant information associated with the actor.
   *
   * This function is local reading only from the network map.
   *
   * @param uid The unique identifier of the actor.
   * @param path The path to the root of the working directory sub-paths are computed.
   * @return A Future that resolves to a String representing the detailed view of the actor and their interpretation
   *         of the rulebook.
   */
  Future<String> viewActor(String uid, String path) async {
    return global.fromCString(
        _lib!.view_actor(_getThread(),
            global.toCString(uid),
            global.toCString(path)
        )
    );
  }

  /**
   * Retrieves a list of actors associated with a specific UID.
   *
   * This function retrieves a list of actors who are associated with the provided UID.
   *
   * The function returns a result that represents the list of actors associated with the provided UID.
   * This can include sources associated with a rulebook identifier or advocates associated with a source, depending on the context.
   *
   * This function is local reading only from the network map.
   *
   * @param uid The unique identifier for which the associated actors need to be listed.
   * @param path The path to the root of the working directory sub-paths are computed.
   * @return A Future that resolves to a String representing the list of actors associated with the provided UID.
   */
  Future<String> listActors(String uid, String path) async {
    return global.fromCString(
        _lib!.list_actors(_getThread(),
            global.toCString(uid),
            global.toCString(path)
        )
    );
  }

  /**
   * Retrieves the identifiers of all active rulebooks.
   *
   * This top-level function retrieves the identifiers of all active rulebooks stored in the specified path.
   *
   * The function returns a result that represents the identifiers of all active rulebooks.
   * The result can be used to obtain information about the active rulebooks and perform further operations.
   *
   * @param path The path to the root of the working directory sub-paths are computed.
   * @return A Future that resolves to a String representing the identifiers of all active rulebooks.
   */
  Future<String> listRulebooks(String path) async {
    return global.fromCString(
        _lib!.list_rulebooks(_getThread(),
            global.toCString(path)
        )
    );
  }

  /**
   * Generates a proof token based on a universal link challenge for SSO (Single Sign-On) authentication.
   *
   * The function generates a proof token using the provided data and sends it to the domain that requires it
   * for the authentication process. It leverages SSO authentication mechanisms for seamless user authentication.
   * For more detailed information on SSO authentications, please refer to the documentation.
   *
   * @param username The username associated with the user's wallet
   * @param passwordAsSha256Hex The password for the user's wallet, represented as a SHA-256 hashed hexadecimal string.
   * @param ulinkChallenge The universal link challenge from the Verifier
   * @param path The path to the root of the working directory sub-paths are computed.
   * @return A Future that resolves to a String representing the generated proof token for SSO authentication.
   */
  Future<String> proofForRulebookSSO(String username, String passwordAsSha256Hex,
      String ulinkChallenge, String path) async {
    return global.fromCString(
        _lib!.proof_for_rulebook_sso(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(ulinkChallenge),
            global.toCString(path)
        )
    );
  }

  /**
   * Generates a delegation request for authenticating a third-party under the user's credentials.
   *
   * The function generates a delegation request based on the provided data, enabling the user to authenticate
   * the third-party under their own credentials. This delegation process is commonly used in SSO (Single Sign-On)
   * authentication scenarios. For more detailed information, please refer to the documentation on SSO authentications.
   *
   * @param username The username associated with the user's wallet.
   * @param passwordAsSha256Hex The password for the user's wallet, represented as a SHA-256 hashed hexadecimal string.
   * @param ulinkChallenge The universal link challenge containing data about the required authentication.
   * @param name The name or identifier of the third-party for whom the delegation request is generated; this manages the request locally.
   * @param path The path of the working directory
   * @return A Future that resolves to a String representing the generated delegation request for authenticating
   *         the third-party under the user's credentials.
   */
  Future<String> generateDelegationRequestForThirdParty(String username,
      String passwordAsSha256Hex, String ulinkChallenge, String name, String path) async {

    return global.fromCString(
        _lib!.generate_delegation_request_for_third_party(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(ulinkChallenge),
            global.toCString(name),
            global.toCString(path)
        )
    );
  }

  /**
   * Fulfills a previously generated delegation request by the third-party.
   *
   * The function fulfills the delegation request by providing the required authentication details, allowing
   * the third-party to proceed with the authentication process. This step is performed by the third-party
   * to complete the delegation flow. For more information, please refer to the documentation on SSO authentications.
   *
   * @param username The username associated with the user's wallet.
   * @param passwordAsSha256Hex The password for the user's wallet, represented as a SHA-256 hashed hexadecimal string.
   * @param ulink The universal link corresponding to the delegation request.
   * @param path The path to the working directory
   * @return A Future that resolves to a String representing the result of fulfilling the delegation request.
   *         The result indicates whether the fulfillment was successful or not.
   *         If successful, a universal link is produced that can be provided to the verifier.
   */
  Future<String> fillDelegationRequest(String username, String passwordAsSha256Hex,
      String ulink, String path) async {

    return global.fromCString(
        _lib!.fill_delegation_request(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(ulink),
            global.toCString(path)
        )
    );
  }

  /**
   * Verifies a delegation request and provides an endonym to facilitate delegated access.
   *
   * Upon successful verification, the function returns an `endonym` that can be provided to the service as proof
   * of delegated access. This step completes the delegation process. For detailed information on the delegation flow,
   * please refer to the documentation on SSO authentications.
   *
   * @param username The username associated with the user's wallet.
   * @param passwordAsSha256Hex The password for the user's wallet, represented as a SHA-256 hashed hexadecimal string.
   * @param requestLink The link representing the delegation request.
   * @param proofLink The link containing the proof token.
   * @param path The path to the working directory
   * @return A Future that resolves to a String representing the endonym, if the delegation request is successfully verified.
   *         The endonym can be provided to the service to facilitate delegated access.
   */
  Future<String> verifyDelegationRequest(String username, String passwordAsSha256Hex,
      String requestLink, String proofLink, String path) async {
    return global.fromCString(
        _lib!.verify_delegation_request(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(requestLink),
            global.toCString(proofLink),
            global.toCString(path)
        )
    );
  }

  /**
   * Creates a non-interactive proof request that can be written to a valid location.
   *
   * The function generates a non-interactive proof request based on the provided data. The proof request can be
   * written to a specified location for further processing or verification.
   *
   * @param username The username associated with the user's wallet.
   * @param passwordAsSha256Hex The password for the user's wallet, represented as a SHA-256 hashed hexadecimal string.
   * @param nonInteractiveProofRequest a json document that defines the request.
   * @param path The path to the working directory
   * @return A Future that resolves to a String representing the result of creating the non-interactive proof request.
   *         The result indicates whether the creation was successful or not.
   *         A successful result is a PresentationToken serialized to xml.
   */
  Future<String> nonInteractiveProofRequest(String username, String passwordAsSha256Hex,
      String nonInteractiveProofRequest, String path) async {

    return global.fromCString(
        _lib!.non_interactive_proof(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(nonInteractiveProofRequest),
            global.toCString(path)
        )
    );
  }

  /**
   * Executes test net onboarding for Sybil.
   *
   * The function executes the test net onboarding process, which typically involves setting up Sybil's account
   * and providing the necessary credentials for accessing the test network. This process is specific to the Sybil entity
   * and enables Sybil to participate in the test net environment.
   *
   * @param username The username associated with the wallet
   * @param sybilClass The test-class or category of Sybil being onboarded.
   * @param passwordAsSha256Hex The password for the wallet
   * @param path The path to the working directory
   * @return A Future that resolves to a String representing the result of the test net onboarding process for Sybil.
   *         The result indicates whether the onboarding was successful or not.
   */
  Future<String> onboardSybilTestnet(String username,
      String sybilClass,
      String passwordAsSha256Hex,
      String path) async {

    return global.fromCString(
        _lib!.onboard_sybil_testnet(_getThread(),
            global.toCString(username),
            global.toCString(sybilClass),
            global.toCString(passwordAsSha256Hex),
            global.toCString(path)
        )
    );
  }

  /**
   * Onboards the user to a rulebook using the issuance policy.
   *
   * The function executes the onboarding process for the specified rulebook, using the provided issuance policy
   * as a guide. This process typically involves completing specific steps or fulfilling requirements outlined in the policy.
   *
   * @param username The username associated with the user's wallet .
   * @param issuancePolicy The issuance policy defining the onboarding requirements for the rulebook.
   * @param passwordAsSha256Hex The password for the user's wallet, represented as a SHA-256 hashed hexadecimal string.
   * @param path The path to the location where the necessary onboarding data is stored.
   * @return A Future that resolves to a String representing the result of the onboarding process for the rulebook.
   *         The result indicates whether the onboarding was successful or not.
   */
  Future<String> onboardRulebookIssuancePolicy(String username,
      String issuancePolicy,
      String passwordAsSha256Hex,
      String path) async {

    return global.fromCString(
        _lib!.onboard_rulebook(_getThread(),
            global.toCString(username),
            global.toCString(issuancePolicy),
            global.toCString(passwordAsSha256Hex),
            global.toCString(path)
        )
    );
  }

  /**
   * Facilitates onboarding to an advocate using their UID.
   *
   * The function executes the onboarding process, allowing the user to onboard to the specified advocate.
   * This process typically involves establishing a connection with the advocate and completing any necessary steps
   * or requirements outlined by the advocate for successful onboarding.
   *
   * @param username The username associated with the user's wallet.
   * @param passwordAsSha256Hex The password for the user's wallet, represented as a SHA-256 hashed hexadecimal string.
   * @param advocateUid The unique identifier (UID) of the advocate to whom the user wants to onboard.
   * @param path The path to the working directory
   * @return A Future that resolves to a String representing the result of the onboarding process to the advocate.
   *         The result indicates whether the onboarding was successful or not.
   */
  Future<String> onboardRulebookAdvocateUID(String username,
      String passwordAsSha256Hex,
      String advocateUid,
      String path) async {

    return global.fromCString(
        _lib!.onboard_rulebook_advocate_uid(_getThread(),
            global.toCString(username),
            global.toCString(passwordAsSha256Hex),
            global.toCString(advocateUid),
            global.toCString(path)

        )
    );
  }

  /**
   * Computes the SHA-256 hash of a string and returns it as a hexadecimal string.
   *
   * This helper function computes the SHA-256 hash of the provided string and returns the result
   * as a hexadecimal string. It requires the following parameter:
   * - `toHash`: The string to be hashed.
   *
   * The function utilizes the SHA-256 hashing algorithm to calculate the hash value of the input string.
   * The resulting hash is then converted to a hexadecimal representation for easier handling and usage.
   *
   * @param toHash The string to be hashed.
   * @return A Future that resolves to a String representing the SHA-256 hash of the input string,
   *         presented as a hexadecimal string.
   */
  Future<String> sha256AsHex(String toHash) async {
    final r = _lib!.sha_256_as_hex(_getThread(), global.toCString(toHash));
    return global.fromCString(r);
  }

  /**
   * Sets up a new wallet with the specified username, plain-text password, and path.
   *
   * This function sets up a new wallet by creating a new instance with the provided username, plain-text password,
   * and path. It requires the following parameters:
   * - `username`: The username associated with the wallet.
   * - `plainTextPassword`: The plain-text password for the wallet.
   * - `path`: The path where the working directory
   *
   * The function returns a Future that resolves to a List of Strings representing the result of the wallet setup.
   * If the setup is successful, the List will contain information about the wallet's setup, such as its identifier.
   * If there is an error during setup, an Exception is thrown with a corresponding error message.
   *
   * @param username The username associated with the wallet.
   * @param plainTextPassword The plain-text password for the wallet.
   * @param path The path where the working directory
   * @return A Future that resolves to a List of Strings representing the recovery phrase for the wallet.
   * @throws Exception if there is an error during wallet setup.
   */
  Future<List<String>> setupWallet(String username, String plainTextPassword, String path) async {
    final r = _lib!.setup_wallet_path(
        _getThread(),
        global.toCString(username),
        global.toCString(plainTextPassword),
        global.toCString(path)
    );
    String result = global.fromCString(r);
    if (!result.startsWith("[")) {
      throw Exception("$result ${global.INFORMAL_LIB_NAME}");
    } else {
      return List<String>.from(jsonDecode(result) as List);
    }
  }

  /**
   * Authenticates the user by checking the correctness of the provided password.
   *
   * This helper function authenticates the user by checking whether the provided password is correct for the specified
   * username and wallet path.
   *
   * The function returns a Future with no return value. If the authentication is successful, the function completes
   * without throwing an exception. If the authentication fails, an Exception is thrown with a corresponding error message.
   *
   * @param username The username associated with the user's wallet.
   * @param passwordAsHexString The password for the user's wallet, represented as a hexadecimal string.
   * @param path The path to the working directory
   * @throws Exception if the authentication fails.
   */
  Future<void> authenticate(String username, String passwordAsHexString, String path) async {
    final r = _lib!.authenticate(_getThread(),
      global.toCString(username),
      global.toCString(passwordAsHexString),
      global.toCString(path),
    );
    String result = global.fromCString(r);
    if (result!="OPENED"){
      throw Exception("$result ${global.INFORMAL_LIB_NAME}");

    } else {
      return;

    }
  }


  /**
   * Retrieves the isolatethread pointer for GraalVM support.
   *
   * This function returns a `Pointer<graal_isolatethread_t>` that represents the isolatethread pointer
   * for GraalVM support. The isolatethread pointer allows interaction with GraalVM functionalities
   * within the codebase.
   *
   * @return A `Pointer<graal_isolatethread_t>` representing the isolatethread pointer for GraalVM support.
   */
  Pointer<graal_isolatethread_t> _getThread() {
    return _thread.value;
  }

  @override
  void dispose() {
    _lib!.graal_tear_down_isolate(_getThread());
    print("© 2023 Exonym - Ciao for now.");

  }
}