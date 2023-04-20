package io.exonym.lib.actor;

import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;

public interface TokenVerifierInterface {

     void loadRevocationInformation(RevocationInformation ri);
     void loadRevocationAuthorityParameters(RevocationAuthorityParameters ri);
     void loadIssuerParameters(IssuerParameters ri);
     void loadInspectorParams(InspectorPublicKey ri);

}
