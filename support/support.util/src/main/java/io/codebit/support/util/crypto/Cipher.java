package io.codebit.support.util.crypto;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.codebit.support.io.extensions.SerializableExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

//
//참고 http://dukeom.wordpress.com/2013/01/08/aes256-%EC%95%94%ED%98%B8%ED%99%94-java-%EC%83%98%ED%94%8C/
//
public class Cipher {
    Logger log = LoggerFactory.getLogger(Cipher.class.getCanonicalName());

    public enum Algorithm {
        // 대칭키
        // key Size: 128, 160, 190, 224, 256 bit
        AES_CBC_NoPadding("AES", "CBC", "NoPadding", 128),
        AES_CBC_PKCS5Padding("AES", "CBC", "PKCS5Padding", 128),
        AES_ECB_NoPadding("AES", "ECB", "NoPadding", 128),
        AES_ECB_PKCS5Padding("AES", "ECB", "PKCS5Padding", 128),
        // key Size:56 (내부적으로 64비트 필요)
        DES_CBC_NoPadding("DES", "CBC", "NoPadding", 56),
        DES_CBC_PKCS5Padding("DES", "CBC", "PKCS5Padding", 56),
        DES_ECB_NoPadding("DES", "ECB", "NoPadding", 56),
        DES_ECB_PKCS5Padding("DESede", "CBC", "NoPadding", 56),
        DESede_CBC_NoPadding("DESede", "CBC", "NoPadding", 168),
        DESede_CBC_PKCS5Padding("DESede", "CBC", "PKCS5Padding", 168),
        DESede_ECB_NoPadding("DESede", "ECB", "NoPadding", 168),
        DESede_ECB_PKCS5Padding("DESede", "ECB", "PKCS5Padding", 168),
        // 비대칭키
        RSA_ECB_PKCS1Padding("RSA", "ECB", "PKCS1Padding", 1024, 2048),
        RSA_ECB_OAEPWithSHA_1AndMGF1Padding("RSA", "ECB", "OAEPWithSHA-1AndMGF1Padding", 1024, 2048),
        RSA_ECB_OAEPWithSHA_256AndMGF1Padding("RSA", "ECB", "OAEPWithSHA-256AndMGF1Padding", 1024, 2048);

        private String algorithm;

        private String mode;

        private String padding;

        private int keySize;

        private String transformations;

        private Algorithm(String algorithm, String mode, String padding, int keySize) {
            this.algorithm = algorithm;
            this.mode = mode;
            this.padding = padding;
            this.keySize = keySize;
            this.transformations = String.format("%s/%s/%s", algorithm, mode, padding);
        }

        private Algorithm(String algorithm, String mode, String padding, int keySize, int size) {
            this.algorithm = algorithm;
            this.mode = mode;
            this.padding = padding;
            this.keySize = keySize;
            this.transformations = String.format("%s/%s/%s", algorithm, mode, padding);
        }

        public String getTransformation() {
            return transformations;
        }
    }

    private String key;
    private SecretKeySpec keySpec;
    private javax.crypto.Cipher cipher;
    private Algorithm algorithm;
    private IvParameterSpec ivParameterSpec;

    public Cipher(String key) {
        this(Algorithm.AES_CBC_PKCS5Padding, key, null);
    }

    public Cipher(String key, String iv) {
        this(Algorithm.AES_CBC_PKCS5Padding, key, iv);
    }

    public Cipher(Algorithm algorithm, String key) {
        this(algorithm, key, null);
    }

    public Cipher(Algorithm algorithm, String key, String iv) {
        this.algorithm = algorithm;
        this.key = key;
        if (iv == null)
            iv = key;
        ivParameterSpec = new IvParameterSpec(this.padding(iv));
    }

    private void init() {
        byte[] raw = this.padding(key);
        keySpec = new SecretKeySpec(raw, this.algorithm.algorithm);
        try {
            cipher = javax.crypto.Cipher.getInstance(this.algorithm.getTransformation());
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            log.error(e.getMessage(), e);
        }
    }

    public byte[] encrypt(byte[] bytes) {
        this.init();
        try {
            byte[] encrypted = null;
            synchronized (cipher) {
                cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
                encrypted = cipher.doFinal(bytes);
            }
            return encrypted;
        } catch (Exception e) {
            throw new RuntimeException("encryption Fail", e);
        }
    }

    public String encrypt(String str) {
        return this.encrypt(str, Charset.defaultCharset());
    }

    public String encrypt(String str, Charset charset) {
        Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(encrypt(str.getBytes(charset)));
    }

    public <T extends Serializable> byte[] encrypt(T object) {
        return this.encrypt(SerializableExtension.serialize(object));
    }

    public byte[] decrypt(byte[] bytes) {
        this.init();

        try {
            byte[] encrypted = null;
            synchronized (cipher) {
                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
                encrypted = cipher.doFinal(bytes);
            }
            return encrypted;
        } catch (Exception e) {
            throw new RuntimeException("encryption Fail", e);
        }
    }

    public <T extends Serializable> T decrypt(byte[] object, Class<T> clazz) {
        return SerializableExtension.deserialize(decrypt(object));
    }

    public String decrypt(String str) {
        return this.decrypt(str, Charset.defaultCharset());
    }

    public String decrypt(String str, Charset charset) {
        Decoder decoder = Base64.getDecoder();
        // String 을 byte[]로 재변환 후 암호 해제
        byte[] decrypted = this.decrypt(decoder.decode(str));
        return new String(decrypted, charset);
    }

    public static String generateRandomSecretKey(Algorithm algorithm)
            throws Exception {
        KeyGenerator KeyGen = KeyGenerator.getInstance(algorithm.algorithm);
        KeyGen.init(128);
        SecretKey key = KeyGen.generateKey();
        byte[] raw = key.getEncoded();
        return new String(raw, Charset.forName("UTF-8"));
    }

    private byte[] padding(String key) {
        int size = this.algorithm.keySize;
        size = size / 8;
        byte[] keys = key.getBytes();
        int len = keys.length;
        byte[] _keys = new byte[size];
        if (len >= size) {
            System.arraycopy(keys, 0, _keys, 0, size);
        } else {
            System.arraycopy(keys, 0, _keys, 0, len);
            for (int i = 0; i < (size - len); i++) {
                _keys[len + i] = keys[i % len];
            }
        }
        return _keys;
    }

	/*private <T extends Serializable> byte[] _serialize(T value)
    {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bos))
		{
			out.writeObject(value);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		return bos.toByteArray();
	}
	
	private <T extends Serializable> T _deserialize(byte[] object)
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(object);
		try (ObjectInputStream in = new ObjectInputStream(bis))
		{
			return (T) in.readObject();
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}*/

	
	
	/*private static String bytesToString(byte[] bytes)
	{
		byte[] b2 = new byte[bytes.length + 1];
		b2[0] = 1;
		System.arraycopy(bytes, 0, b2, 1, bytes.length);
		return new BigInteger(b2).toString(Character.MAX_RADIX);
	}

	private static byte[] stringToBytes(String str)
	{
		byte[] bytes = new BigInteger(str, Character.MAX_RADIX).toByteArray();
		return Arrays.copyOfRange(bytes, 1, bytes.length);
	}*/

}
