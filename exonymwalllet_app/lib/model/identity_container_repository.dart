import 'package:exonymwallet/exonymwallet.dart';

class IdentityRepository {
  static const _allIdentities = <IdentityContainer>[
    IdentityContainer(
        identityType: IdentityType.person,
        username: 'sso',
        b64Salt: '123fsDDfdFfs'
    ),
  ];

  static List<IdentityContainer> loadProducts(IdentityType type) {
    if (type == IdentityType.all) {
      return _allIdentities;
    } else {
      throw Exception("Not implemented");
    }
  }
}