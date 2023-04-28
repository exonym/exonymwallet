package io.exonym.lib.wallet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.helpers.AbstractCouchDbObject;
import io.exonym.lib.lite.FulfillmentReport;
import io.exonym.lib.lite.NonInteractiveProofRequest;
import io.exonym.lib.lite.SFTPLogonData;
import io.exonym.lib.lite.WalletReport;
import io.exonym.lib.pojo.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;

public class GraalVMReflectionConfig {

    private String name;
    private boolean allDeclaredFields = true;
    private boolean queryAllDeclaredMethods = true;
    private boolean queryAllDeclaredConstructors = true;

    private ArrayList<GvmRcMethod> methods = new ArrayList<>();


    public GraalVMReflectionConfig() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAllDeclaredFields() {
        return allDeclaredFields;
    }

    public void setAllDeclaredFields(boolean allDeclaredFields) {
        this.allDeclaredFields = allDeclaredFields;
    }

    public boolean isQueryAllDeclaredMethods() {
        return queryAllDeclaredMethods;
    }

    public void setQueryAllDeclaredMethods(boolean queryAllDeclaredMethods) {
        this.queryAllDeclaredMethods = queryAllDeclaredMethods;
    }

    public boolean isQueryAllDeclaredConstructors() {
        return queryAllDeclaredConstructors;
    }

    public void setQueryAllDeclaredConstructors(boolean queryAllDeclaredConstructors) {
        this.queryAllDeclaredConstructors = queryAllDeclaredConstructors;
    }

    public ArrayList<GvmRcMethod> getMethods() {
        return methods;
    }

    public void setMethods(ArrayList<GvmRcMethod> methods) {
        this.methods = methods;
    }

    public static GraalVMReflectionConfig init(Class<?> clazz){
        GraalVMReflectionConfig r = new GraalVMReflectionConfig();
        r.name = clazz.getName();
        Method[] methods = clazz.getDeclaredMethods();
        GvmRcMethod init = new GvmRcMethod();
        init.setName("<init>");

        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor c : constructors){
            if (c.getModifiers()==1){
                r.methods.add(buildExecutable(c, true));
            }
        }
        for (Method m : methods){
            if (m.getModifiers()==1) {
                GvmRcMethod method = buildExecutable(m, false);
                r.methods.add(method);

            }
        }
        return r;

    }

    private static GvmRcMethod buildExecutable(Executable m, boolean constructor){
        GvmRcMethod method = new GvmRcMethod();
        String name = (constructor ? "<init>": m.getName());
        method.setName(name);
        Parameter[] params = m.getParameters();
        for (Parameter param : params){
            method.getParameterTypes().add(param.getType().getName());
        }
        return method;
    }

    private final static Class<?>[] CLASSES = new Class[]{
            TrustNetwork.class,
            NodeInformation.class,
            NetworkParticipant.class,
            Rulebook.class,
            RulebookDescription.class,
            WalletReport.class,
            SFTPLogonData.class,
            SsoChallenge.class,
            AuthenticationWrapper.class,
            FulfillmentReport.class,
            NonInteractiveProofRequest.class,
            HashSet.class,
            JsonObject.class,
            JsonArray.class,
            eu.abc4trust.xml.SmartcardPinRequests.class,
            eu.abc4trust.xml.ABCEBoolean.class,
            eu.abc4trust.xml.NreUpdateMessage.class,
            eu.abc4trust.xml.CarriedOverAttribute.class,
            eu.abc4trust.xml.CandidatePresentationToken.class,
            eu.abc4trust.xml.CommittedAttribute.class,
            eu.abc4trust.xml.IssuancePolicyAndAttributes.class,
            AbstractCouchDbObject.class,
            eu.abc4trust.xml.CommittedKey.class,
            eu.abc4trust.xml.Attribute.class,
            eu.abc4trust.xml.PseudonymDescriptions.class,
            eu.abc4trust.xml.MechanismSpecification.class,
            eu.abc4trust.xml.InspectorDescriptions.class,
            eu.abc4trust.xml.VerifierParameters.class,
            eu.abc4trust.xml.CredentialDescriptions.class,
            eu.abc4trust.xml.StringParameter.class,
            eu.abc4trust.xml.InspectorPublicKeyTemplate.class,
            eu.abc4trust.xml.PresentationTokenWithCommitments.class,
            eu.abc4trust.xml.AttributeDescription.class,
            eu.abc4trust.xml.StandardPseudonym.class,
            eu.abc4trust.xml.VerifierDrivenRevocationInToken.class,
            eu.abc4trust.returnTypes.ui.RevocationAuthorityInUi.class,
            eu.abc4trust.xml.TestIssuanceMessage.class,
            eu.abc4trust.returnTypes.ui.InspectorInUi.class,
            eu.abc4trust.xml.PseudonymMetadata.class,
            eu.abc4trust.xml.RevocationMessage.class,
            eu.abc4trust.xml.PresentationTokenDescription.class,
            eu.abc4trust.xml.NonRevocationEvidenceUpdate.class,
            eu.abc4trust.xml.PresentationTokenDescriptionWithCommitments.class,
            NetworkParticipant.class,
            eu.abc4trust.xml.AttributeInToken.class,
            eu.abc4trust.xml.SmartcardSystemParameters.class,
            eu.abc4trust.xml.RevocationHandle.class,
            eu.abc4trust.xml.Signature.class,
            eu.abc4trust.returnTypes.UiIssuanceArguments.class,
            eu.abc4trust.xml.CandidatePresentationTokenList.class,
            eu.abc4trust.xml.InspectorChoiceList.class,
            eu.abc4trust.xml.Message.class,
            XKey.class,
            eu.abc4trust.xml.PseudonymChoiceList.class,
            eu.abc4trust.xml.PresentationToken.class,
            eu.abc4trust.xml.IssuanceAttributeList.class,
            eu.abc4trust.xml.AttributeInPolicy.class,
            eu.abc4trust.xml.PresentationPolicy.class,
            eu.abc4trust.xml.RevocationEvent.class,
            eu.abc4trust.xml.IdemixVerifierParameters.class,
            eu.abc4trust.xml.CredentialDescription.class,
            eu.abc4trust.xml.SystemParametersTemplate.class,
            eu.abc4trust.returnTypes.ui.PseudonymInUi.class,
            eu.abc4trust.xml.RevocationLogEntry.class,
            eu.abc4trust.returnTypes.SptdReturn.class,
            eu.abc4trust.xml.IssuerParametersInput.class,
            eu.abc4trust.xml.CredentialInPolicy.class,
            eu.abc4trust.xml.PresentationPolicyAlternatives.class,
            eu.abc4trust.xml.CredentialUidList.class,
            eu.abc4trust.xml.RevocationHistoryIdmx.class,
            eu.abc4trust.returnTypes.ui.TokenCandidatePerPolicy.class,
            eu.abc4trust.returnTypes.UiPresentationArguments.class,
            KeyContainer.class,
            eu.abc4trust.xml.IssuanceLogEntry.class,
            eu.abc4trust.xml.Error.class,
            eu.abc4trust.xml.CredentialInTokenWithCommitments.class,
            eu.abc4trust.xml.CryptoParams.class,
            eu.abc4trust.returnTypes.SitdReturn.class,
            eu.abc4trust.xml.PseudonymWithMetadata.class,
            eu.abc4trust.xml.IssuerPublicKeyTemplate.class,
            eu.abc4trust.xml.RevocationReferences.class,
            eu.abc4trust.xml.InspectorDescription.class,
            eu.abc4trust.returnTypes.UiPresentationReturn.class,
            eu.abc4trust.xml.AttributeInLogEntry.class,
            eu.abc4trust.xml.AttributeList.class,
            eu.abc4trust.xml.VerificationCall.class,
            eu.abc4trust.xml.Credential.class,
            eu.abc4trust.xml.PolicyDescriptionsEntry.class,
            eu.abc4trust.xml.PublicKey.class,
            eu.abc4trust.xml.PseudonymDescriptionsEntry.class,
            eu.abc4trust.xml.SecretKey.class,
            eu.abc4trust.xml.CredentialTemplate.class,
            eu.abc4trust.xml.SystemParameters.class,
            eu.abc4trust.xml.RevocationState.class,
            eu.abc4trust.xml.VerifierIdentity.class,
            eu.abc4trust.xml.TestApplicationData.class,
            eu.abc4trust.xml.ValueInZkProof.class,
            eu.abc4trust.xml.Pseudonym.class,
            eu.abc4trust.xml.CredentialSpecificationAndSystemParameters.class,
            eu.abc4trust.xml.IssuanceProtocolMetadata.class,
            eu.abc4trust.xml.IssuanceTokenAndIssuancePolicy.class,
            eu.abc4trust.xml.InspectorPublicKey.class,
            eu.abc4trust.xml.CredentialSpecification.class,
            eu.abc4trust.returnTypes.UiManageCredentialData.class,
            eu.abc4trust.xml.AttributePredicate.class,
            eu.abc4trust.xml.SignatureToken.class,
            eu.abc4trust.xml.TestSystemParameters.class,
            eu.abc4trust.xml.IssuanceTokenDescription.class,
            eu.abc4trust.xml.SelectPresentationTokenDescription.class,
            eu.abc4trust.returnTypes.SptdArguments.class,
            eu.abc4trust.returnTypes.ui.RevealedFact.class,
            eu.abc4trust.xml.IssuerParameters.class,
            eu.abc4trust.xml.PolicyDescription.class,
            eu.abc4trust.returnTypes.UiIssuanceReturn.class,
            eu.abc4trust.xml.PolicyDescriptions.class,
            eu.abc4trust.xml.ZkProof.class,
            eu.abc4trust.xml.ValueWithHashInZkProof.class,
            eu.abc4trust.xml.AttributePartition.class,
            eu.abc4trust.xml.IssuanceToken.class,
            eu.abc4trust.xml.Parameter.class,
            eu.abc4trust.xml.SecretDescription.class,
            eu.abc4trust.xml.CandidateIssuanceTokenList.class,
            eu.abc4trust.xml.RevocationInformation.class,
            eu.abc4trust.xml.AbstractPseudonym.class,
            eu.abc4trust.xml.ModuleInZkProof.class,
            eu.abc4trust.xml.PseudonymInPolicy.class,
            eu.abc4trust.xml.IssuanceExtraMessage.class,
            eu.abc4trust.xml.IssuanceMessage.class,
            eu.abc4trust.xml.Metadata.class,
            eu.abc4trust.xml.AttributeDescriptions.class,
            eu.abc4trust.xml.PresentationPolicyAlternativesAndPresentationToken.class,
            eu.abc4trust.xml.PrivateKey.class,
            eu.abc4trust.xml.IntegerParameter.class,
            eu.abc4trust.returnTypes.IssuanceReturn.class,
            eu.abc4trust.xml.RevocationAuthorityPublicKeyTemplate.class,
            eu.abc4trust.xml.FriendlyDescription.class,
            eu.abc4trust.returnTypes.ui.CredentialInUi.class,
            eu.abc4trust.xml.RevocationAuthorityParameters.class,
            eu.abc4trust.xml.PseudonymInToken.class,
            eu.abc4trust.xml.TestReference.class,
            eu.abc4trust.returnTypes.ui.UiCommonArguments.class,
            eu.abc4trust.xml.Reference.class,
            eu.abc4trust.xml.CredentialInToken.class,
            eu.abc4trust.xml.UnknownAttributes.class,
            eu.abc4trust.xml.NonRevocationEvidence.class,
            eu.abc4trust.xml.SelectIssuanceTokenDescription.class,
            eu.abc4trust.returnTypes.ui.InspectableAttribute.class,
            eu.abc4trust.returnTypes.ui.CredentialSpecInUi.class,
            eu.abc4trust.xml.Secret.class,
            eu.abc4trust.xml.CandidateIssuanceToken.class,
            eu.abc4trust.xml.PseudonymDescription.class,
            eu.abc4trust.returnTypes.ui.IssuerInUi.class,
            eu.abc4trust.returnTypes.ui.TokenCandidate.class,
            eu.abc4trust.xml.VerifierParametersTemplate.class,
            eu.abc4trust.xml.CredentialDescriptionsEntry.class,
            eu.abc4trust.xml.IssuanceMessageAndBoolean.class,
            eu.abc4trust.returnTypes.SitdArguments.class,
            eu.abc4trust.xml.PseudonymDescriptionValue.class,
            eu.abc4trust.xml.RevocationHistory.class,
            eu.abc4trust.returnTypes.ui.RevealedAttributeValue.class,
            eu.abc4trust.xml.URISet.class,
            eu.abc4trust.xml.KeyPair.class,
            eu.abc4trust.xml.BigIntegerParameter.class,
            eu.abc4trust.xml.VerifierDrivenRevocationInPolicy.class,
            eu.abc4trust.xml.InspectorDescriptionsEntry.class,
            eu.abc4trust.xml.TestCryptoParams.class,
            eu.abc4trust.xml.ApplicationData.class,
            eu.abc4trust.xml.IssuancePolicy.class,
            eu.abc4trust.returnTypes.ui.PseudonymListCandidate.class
    };

    public String toJson() throws Exception {
        return JaxbHelper.serializeToJson(this, GraalVMReflectionConfig.class);
    }



    public static void parseGraalWarnings() {
        Path path = Path.of("resource", "reflect", "not_found.log");
        HashSet<String> classes = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(path)){
            String line = null;
            while((line=reader.readLine())!=null){
                if (line.length() > 0) {
                    String[] parts = line.split("\\$");
                    if (parts.length>1){
                        String[] isolate = parts[0].split(" ");
                        if (isolate.length > 1){
                            classes.add(isolate[isolate.length-1]);

                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
        for (String clazz : classes){
            System.out.println(clazz +".class,");
        }

    }


    public static void createReflectionConfig(Class<?>[] classes) throws Exception {
        Path path = Path.of("resource", "reflect", "reflection-config.json");
        Files.createDirectories(path.getParent());
        Files.deleteIfExists(path);
        Files.createFile(path);

        try (BufferedWriter writer = Files.newBufferedWriter(path)){
            for (Class<?> clazz : classes){
                GraalVMReflectionConfig c = GraalVMReflectionConfig.init(clazz);
                writer.append(c.toJson() + ",");

            }
            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static void main(String[] args) throws Exception {
           createReflectionConfig(CLASSES);
//        GraalVMReflectionConfig c = GraalVMReflectionConfig.init(JsonArray.class);
//        System.out.println(c.toJson());

    }
}
