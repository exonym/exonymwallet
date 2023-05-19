package io.exonym.lib.lite;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import io.exonym.lib.exceptions.ProgrammingException;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.standard.WhiteList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;

public class SFTPClient extends ModelCommandProcessor implements AutoCloseable {
	

	private final static Logger logger = Logger.getLogger(SFTPClient.class.getName());
	private final SFTPLogonData credentials;

    private Session session = null;
    private Channel channel = null;
    private ChannelSftp sftp = null;
    private boolean active = true;

	private String rootPathFolder = "uploads";

    private Msg msg = new Msg() {
		private static final long serialVersionUID = 1L;
		
	};

	public SFTPClient(SFTPLogonData logonData) {
		super(1, "sftp-client-60s-timeout", 10000);
		this.credentials = logonData;

	}

	public void connect() throws Exception {
		String kn0, kn1, kn2, username, password;
		kn0 = credentials.getKnownHost0(); kn1 = credentials.getKnownHost1(); kn2 = credentials.getKnownHost2();

		username = credentials.getUsername();
		password = credentials.getPassword();

		if (kn0!=null || kn0.length()!=0) {

			if (kn0.startsWith(credentials.getHost())) {
				JSch j = new JSch();
				session = j.getSession(username, credentials.getHost(), credentials.getPort());
				logger.info("SFTP Session Created");
				String knownHosts = kn0 + "\n" + kn1 + "\n" + kn2;
				InputStream knownHostsInputStream = new ByteArrayInputStream(knownHosts.getBytes());
				j.setKnownHosts(knownHostsInputStream);
				
		        session.setPassword(password);
		        session.connect();
		        logger.info("SFTP Session Connected");

		        logger.info("Opening FTP Channel");
		        channel = session.openChannel("sftp");
		        channel.connect();
		        sftp = (ChannelSftp) channel;
		        logger.info("SFTP Channel Open "
						+ credentials.getUsername() + "@" + credentials.getHost());

				
			} else {
				throw new UxException("'ssh-keyscan <properties/sftp-host>' is the command you need.  See - exonym.io/help");
				
			}
		} else {
			throw new UxException("You must use ssh-keyscan to generate known_host keys");
			
		}
	}
	
	public void directory(String relativePath) throws SftpException {
		sftp.cd(relativePath);
		
	}
	
	public void put(String fileName, String content, boolean forceUploadFolderAsRoot) throws Exception {
		if (!exists(fileName)) {
			overwrite(fileName, content, forceUploadFolderAsRoot);
			
		} else {
			throw new ProgrammingException("File already exists " + fileName + " - call overwrite");
			
		}
	}

	public void delete(String fileName) throws Exception {
		try {
			sftp.rm(fileName);
			
		} catch (SftpException e) {
			throw e; 
			
		}
	}	
	
	public void deleteDirectory(String fileName) throws Exception {
		try {
			sftp.rmdir(fileName);
			
		} catch (SftpException e) {
			throw e;
			
		}
	}

	/**
	 * Write the content to the specified path - The directory need not exist.
	 * 
	 * @param fileName
	 * @param content
	 * @throws Exception
	 */
	public void overwrite(String fileName, String content, boolean forceUploadsFolderAsRoot) throws Exception {
		// logger.info("SFTP Putting = " + fileName + "\ncontent= " + content);
		if (fileName.endsWith(".xml") || fileName.endsWith(".json") || fileName.endsWith(".html")) {
			this.createPath(fileName, forceUploadsFolderAsRoot);
			String pre = (forceUploadsFolderAsRoot ? "/" + this.rootPathFolder : "");

			fileName = (fileName.startsWith("/") ? pre + fileName : pre + "/" + fileName);
			
			try (ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes())){
				sftp.put(bis, fileName);
				this.getPipe().put(msg);
				
			} catch (SftpException e) {
				throw e; 
				
			}
		} else {
			throw new ProgrammingException("You should only be writing xml or json files here");
			
		}
	}

	public void overwrite(String fileName, byte[] content, boolean forceUploadsFolderAsRoot) throws Exception {
		this.createPath(fileName, forceUploadsFolderAsRoot);
		String pre = (forceUploadsFolderAsRoot ? "/" + this.rootPathFolder : "");

		fileName = (fileName.startsWith("/") ? pre + fileName : pre + "/" + fileName);

		try (ByteArrayInputStream bis = new ByteArrayInputStream(content)){
			sftp.put(bis, fileName);
			this.getPipe().put(msg);

		} catch (SftpException e) {
			throw e;

		}
	}
	
	public boolean exists(String file) {
		Vector<?> res = null;
	    try {
	        res = sftp.ls(file);
	        
	    } catch (SftpException e) {
	        if (e.id == 2) {
	            return false;
	            
	        }
	        logger.throwing("SFTPClient.class", "exists()", e);
	        
	    }
	    return res != null && !res.isEmpty();
		
	}
	
	public ArrayList<String> getFileNames(String pathFromRoot){
		ArrayList<String> r = new ArrayList<String>();
		try {
			@SuppressWarnings("unchecked")
			Vector<LsEntry> da = sftp.ls(pathFromRoot);
			for (LsEntry e : da) {
				String n = e.getFilename();
				if (!n.startsWith(".")){
					r.add(n);
					
				}
			}
		} catch (SftpException e) {
			e.printStackTrace();
			
		}
		return r; 
		
	}
	
	/**
	 * Provide path relative to root without indicating 
	 * root path at the beginning /
	 * @param path
	 * @throws SftpException
	 */
	public void createPath(String path, boolean forceUploadsFolderAsRoot) throws Exception {
		if (path!=null) {
			String[] folders = path.split("/");
			if (path.endsWith(".xml")) {
				String p = "";	
				for (int i = 0; i < (folders.length-1); i++) {
					p+= "/" + ((folders[i] !=null && folders[i].length()>0) ? folders[i] : "");
					
				}
				path = p;
				folders = path.split("/");
				
			}
			String root = (forceUploadsFolderAsRoot ? "/" + this.rootPathFolder + "/" : "/");
			sftp.cd(".");
			logger.fine("path to root: " + root);
			sftp.cd(root);
			for (String f : folders) {
				if (f.length()>0) {
					if (WhiteList.isMinLettersAllowsNumbersAndHyphens(f, 1)) {
						try {
							sftp.cd(f);
							logger.fine("pwd = " + sftp.pwd());
							
						} catch (SftpException e) {
							try {
								sftp.mkdir(f);
								sftp.cd(f);
								logger.fine("pwd = " + sftp.pwd());
								
							} catch (SftpException e1) {
								throw e1;
								
							}
						}
					} else {
						if (!f.contains(".")){
							throw new UxException("'" + f + "' is not a valid name");

						} else {
							logger.info("Skipping folder generation because the folder was called:" + f);

						}
					}
				}
			}
		} else {
			throw new NullPointerException();
			
		}
	}
	
	public boolean isActive() {
		return active;
	}

	public void disconnect() throws Exception {
		if (sftp!=null) {
			sftp.disconnect();
			
		}
		if (channel!=null) {
			channel.disconnect();
			
		}
		if (session!=null) {
			session.disconnect();
			
		} 
		super.close();
		
	}

	public ChannelSftp getSftp() {
		return sftp;

	}

	@Override
	public void close() throws Exception {
		this.disconnect();
		
	}
	
	@Override
	protected void periodOfInactivityProcesses() {
		try {
			this.active=false; 
			this.close();
			
		} catch (Exception e) {
			logger.info("Close");
			
		}
	}

	@Override
	protected void receivedMessage(Msg msg) {}

	public String getRootPathFolder() {
		return rootPathFolder;
	}

	public void setRootPathFolder(String rootPathFolder) {
		this.rootPathFolder = rootPathFolder;
	}
}
