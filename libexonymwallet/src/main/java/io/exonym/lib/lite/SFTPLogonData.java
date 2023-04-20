package io.exonym.lib.lite;

import io.exonym.lib.pojo.Namespace;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;

@XmlRootElement(name="SFTPCredentials", namespace = Namespace.EX)
@XmlType(name="SFTPCredentials", namespace = Namespace.EX)
public class SFTPLogonData {

    private URI sftpUID;
    private  int port;
    private  String host;

    private String username;
    private String password;

    private String knownHost0;
    private String knownHost1;
    private String knownHost2;

    public SFTPLogonData() {
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsernameAndPassword(String username, String password) {
        if (username==null || password==null){
            throw new NullPointerException();

        }
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getKnownHost0() {
        return knownHost0;
    }

    public String getKnownHost1() {
        return knownHost1;
    }

    public String getKnownHost2() {
        return knownHost2;
    }

    public void setKnownHosts(String k0, String k1, String k2) {
        if (k0==null || k1==null || k2==null){
            throw new NullPointerException();

        }
        this.knownHost0 = k0;
        this.knownHost1 = k1;
        this.knownHost2 = k2;

    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return this.username + " " + this.password + " \\n\\t " + this.knownHost0;
    }

    public URI getSftpUID() {
        return sftpUID;
    }

    public void setSftpUID(URI sftpUID) {
        this.sftpUID = sftpUID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setKnownHost0(String knownHost0) {
        this.knownHost0 = knownHost0;
    }

    public void setKnownHost1(String knownHost1) {
        this.knownHost1 = knownHost1;
    }

    public void setKnownHost2(String knownHost2) {
        this.knownHost2 = knownHost2;
    }
}
