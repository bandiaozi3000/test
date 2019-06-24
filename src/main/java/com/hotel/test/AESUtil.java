package com.hotel.test;

import org.apache.tomcat.util.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.Security;

/**
 * 
 * @Title: AESUtil.java
 * @Package com.bc.authorize.util
 * @Description: AES工具类
 * @author cy.wang
 * @date 2019年1月16日 下午6:53:04
 * @version V1.0
 *
 */
@Component
public class AESUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(AESUtil.class);

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * AES解密
	 *
	 * @param data
	 *            密文，被加密的数据
	 * @param key
	 *            秘钥
	 * @param iv
	 *            偏移量
	 * @param encodingFormat
	 *            解密后的结果需要进行的编码
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String data, String key, String iv, String encodingFormat){
		// 被加密的数据
		byte[] dataByte = Base64.decodeBase64(data);
		// 加密秘钥
		byte[] keyByte = Base64.decodeBase64(key);
		// 偏移量
		byte[] ivByte = Base64.decodeBase64(iv);
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding","BC");
			SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
			AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
			parameters.init(new IvParameterSpec(ivByte));
			cipher.init(Cipher.DECRYPT_MODE, spec, parameters);
			byte[] resultByte = cipher.doFinal(dataByte);
			if (null != resultByte && resultByte.length > 0) {
				String result = new String(resultByte, encodingFormat);
				return result;
			}
			return null;
		} catch (Exception e) {
			LOGGER.error("解密出现错误", e);
		}
		return null;
	}

}
