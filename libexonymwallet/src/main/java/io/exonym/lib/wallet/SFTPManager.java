package io.exonym.lib.wallet;

import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.lite.SFTPClient;
import io.exonym.lib.lite.SFTPLogonData;
import io.exonym.lib.pojo.Namespace;
import io.exonym.lib.standard.PassStore;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

public class SFTPManager {

    public static final String CRED_FILE_NAME = "_sftp-credential.xml";
    private final PassStore store;
    private final Path  root;
    private final ExonymToolset exo;

    public SFTPManager(PassStore store, Path  root) throws Exception {
        this.store=store;
        this.root = root;
        this.exo = new ExonymToolset(store, root);;

    }

    public String add() throws Exception {
        SFTPLogonData credential = readCredentialFile(root);
        verifyCredential(credential);
        verifyAccess(credential);
        exo.getX().saveLocalResource(credential, store.getEncrypt());
        exo.getX().openResource(credential.getSftpUID(), store.getDecipher());
        return "SFTP_CREDENTIAL_TESTED_SAVED_AND_RECOVERED";

    }

    public String remove(String uidToDelete) throws Exception {
        if (!uidToDelete.startsWith(Namespace.URN_PREFIX_COLON)){
            uidToDelete = Namespace.URN_PREFIX_COLON + uidToDelete;
            if (!uidToDelete.endsWith(":sftp")){
                uidToDelete += ":sftp";

            }
        }
        exo.getX().deleteSftpCredential(uidToDelete);
        return "DELETED";

    }

    private final static Logger logger = Logger.getLogger(SFTPManager.class.getName());

    private void verifyAccess(SFTPLogonData credential) throws Exception {
        try {
            SFTPClient client = new SFTPClient(credential);
            client.connect();
            String f = "delete-me-" + UUID.randomUUID().toString().replaceAll("-", "") + ".xml";
            try {
                client.overwrite(f, "", false);

            } catch (Exception e) {
                client.overwrite(f, "test", true);

            }
            client.close();

        } catch (Exception e) {
            logger.info(e.getMessage());
            String stack = ExceptionUtils.getStackTrace(e);
            logger.info(stack);

            throw new UxException("SFTP_CREDENTIAL_INVALID", e,
                    "Check your username, password, or known host data",
                    "Authentication was refused by the server.",
                    "To generate the Known Host fingerprints try the following command:",
                    "ssh-keyscan " + credential.getHost(),
                    credential.getUsername(),
                    credential.getKnownHost0(),
                    credential.getKnownHost1(),
                    credential.getKnownHost2());

        }
    }

    public String put(String filename, String token, String sftpCredentialUID, String remotePath) throws Exception {
        SFTPLogonData sftpCred = exo.getX().openResource(URI.create(sftpCredentialUID), store.getDecipher());
        SFTPClient client = new SFTPClient(sftpCred);
        client.connect();
        try {
            client.put(remotePath + "/" + filename, token, false);

        } catch (Exception e) {
            client.put(remotePath + "/" + filename, token, true);

        }
        client.close();
        return"{'success':true}";

    }

    private void verifyCredential(SFTPLogonData credential) throws UxException {
        if (credential.getSftpUID()==null){
            throw new UxException(ErrorMessages.INVALID_UID, "sftpUID");

        } if (credential.getHost()==null){
            throw new UxException("NO_HOST");

        } if (credential.getPort()==0){
            throw new UxException("NO_PORT");

        } if (credential.getUsername()==null){
            throw new UxException("NO_USERNAME");

        } if (credential.getPassword()==null){
            throw new UxException("NO_PASSWORD");

        }
    }

    private SFTPLogonData readCredentialFile(Path root) throws UxException {
        root = root.resolve(CRED_FILE_NAME);
        if (Files.isRegularFile(root)){
            try (BufferedReader reader = Files.newBufferedReader(root)){
                StringBuilder builder = new StringBuilder();
                String line = null;
                while ((line = reader.readLine())!=null){
                    builder.append(line);

                }
                try {
                    return JaxbHelper.xmlToClass(builder.toString(), SFTPLogonData.class);

                } catch (Exception e) {
                    throw new UxException(ErrorMessages.FILE_NOT_REGULAR, e, "A valid XML file is required");

                }
            } catch (Exception e){
                throw new UxException(ErrorMessages.FILE_NOT_FOUND, e, "An error occurred");

            }
        } else {
            throw new UxException(ErrorMessages.FILE_NOT_FOUND,
                    "No such file as " + CRED_FILE_NAME, "Try creating a template first");
        }
    }


    public static String createTemplate(Path path) throws UxException {
        createDirectories(path);
        path = path.resolve(CRED_FILE_NAME);
        if (Files.exists(path)){
            throw new UxException("SFTP_TEMPLATE_ALREADY_EXISTS", "Error creating file");

        }
        try (BufferedWriter writer = Files.newBufferedWriter(path)){

            SFTPLogonData sftp = new SFTPLogonData();
            sftp.setSftpUID(URI.create("urn:rulebook:unique-name-in-the-target-wallet:sftp"));
            sftp.setPort(22);
            sftp.setHost("example.com");
            sftp.setKnownHosts("use_the_ssh_command", "to_generate", "these_for_security");
            sftp.setUsernameAndPassword("username", "password");
            String template = JaxbHelper.serializeToXml(sftp, SFTPLogonData.class);
            writer.write(template);
            writer.flush();
            return template;

        } catch (Exception e) {
            throw new UxException(ErrorMessages.WRITE_FILE_ERROR, "Error creating file");

        }
    }

    private static void createDirectories(Path path) throws UxException {
        try {
            if (!Files.isDirectory(path)){
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new UxException(ErrorMessages.WRITE_FILE_ERROR, "Error creating directories");

        }
    }

}
