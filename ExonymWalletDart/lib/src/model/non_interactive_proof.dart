import 'dart:collection';
import 'dart:convert';

class NonInteractiveProofRequest {

  NonInteractiveProofRequest(){}

  HashSet<String> issuerUids = HashSet();
  HashSet<String> pseudonyms = HashSet();
  Map<String, dynamic>? metadata;

  HashSet<String> getIssuerUids() {
    return issuerUids;
  }

  void setIssuerUids(HashSet<String> issuerUids) {
    this.issuerUids = issuerUids;
  }

  HashSet<String> getPseudonyms() {
    return pseudonyms;
  }

  void setPseudonyms(HashSet<String> pseudonyms) {
    this.pseudonyms = pseudonyms;
  }

  Map<String, dynamic>? getMetadata() {
    return metadata;
  }

  void setMetadata(Map<String, dynamic> metadata) {
    this.metadata = metadata;
  }

  // Constructor for deserializing JSON
  NonInteractiveProofRequest.fromJson(String jsonString) {
    Map<String, dynamic> json =  jsonDecode(jsonString);
    if (json.containsKey('issuerUids')) {
      for (var issuerUid in json['issuerUids']) {
        issuerUids.add(issuerUid);
      }
    }
    if (json.containsKey('pseudonyms')) {
      for (var pseudonym in json['pseudonyms']) {
        pseudonyms.add(pseudonym);
      }
    }
    metadata = json['metadata'];
  }

  // Method for serializing to JSON
  String toJson() {
    return jsonEncode({
      'issuerUids': issuerUids.toList(),
      'pseudonyms': pseudonyms.toList(),
      'metadata': metadata,
    });
  }


}


