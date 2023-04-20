package io.exonym.lib.standard;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;

public class QrCode {
	

	
	public static byte[] computeQrCodeAsPng(String stringToEncode, int pixels) throws Exception{
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = qrCodeWriter.encode(stringToEncode, BarcodeFormat.QR_CODE, pixels, pixels);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
		return os.toByteArray();
		
	}

	public static String computeQrCodeAsPngB64(String stringToEncode, int pixels) throws Exception {
		return Base64.encodeBase64String(computeQrCodeAsPng(stringToEncode, pixels));

	}

}
