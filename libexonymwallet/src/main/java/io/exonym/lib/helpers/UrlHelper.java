package io.exonym.lib.helpers;



import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class UrlHelper {


	private final static Logger logger = Logger.getLogger(UrlHelper.class.getName());
	public static final URI LAMBDA_LOCATION = URI.create("https://exonym.io/lambda.xml");
	public static final URI LAMBDA_FAILOVER_LOCATION = URI.create("https://spectra.plus/lambda.xml");
	private static byte[] comparison = "<?xml version=".getBytes();

	public static byte[] read(URL url) throws FileNotFoundException, IOException, UnknownHostException {
		logger.info("Trying to read:" + url);
		URLConnection connection = url.openConnection();
		logger.finest("2022-05-21 - inserted a 5 second timeout on read() - could potentially cause unexpected behaviour");
		connection.setConnectTimeout(5000);
		try (BufferedInputStream br = new BufferedInputStream(connection.getInputStream())){
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len;
			while((len = br.read(buffer)) != -1){
				bos.write(buffer, 0, len);

			}
			bos.close();
			return bos.toByteArray();

		} catch (UnknownHostException e) {
			throw e;

		} catch (FileNotFoundException e) {
			throw e; 
			
		} catch (Exception e) {
			throw e; 
			
		}
	}

	public static byte[] read(URL url0, URL url1) throws FileNotFoundException, IOException, UnknownHostException {
		try{
			return read(url0);

		} catch (Exception e){
			try {
				return read(url1);

			} catch (IOException ex) {
				ex.initCause(e);
				throw ex;

			}
		}
	}

	public static byte[] readXml(URL url) throws Exception {
		byte[] read = UrlHelper.read(url);
		if (isXml(read)){
			return read;

		} else {
			throw new SecurityException("Expected XML and XmlHelper.isXml() return false");

		}
	}

	public static boolean isXml(byte[] bytes){

		if (bytes==null || bytes.length < comparison.length){
			return false;

		}
		for (int i = 0; i < comparison.length; i++) {
			if (bytes[i]!=comparison[i]){
				return false;

			}
		}
		return true;

	}
}
