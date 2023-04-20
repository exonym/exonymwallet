enum IdentityType {
  person,
  representative,
  robot,
  entity,
  product,
  all,

}

class IdentityContainer {
  const IdentityContainer({
    required this.identityType,
    required this.username,
    required this.b64Salt,
    this.saltedPasswordAsHex,
  });

  final IdentityType identityType;
  final String username;
  final String b64Salt;
  final String? saltedPasswordAsHex;

}