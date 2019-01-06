package io.codebit.support.util.crypto;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class StandardPasswordEncoder {
	private final String algorithm;
	private final int saltIterations = 8;
	private final int iterations = 1024;
	private final byte[] secret;
	private final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	public StandardPasswordEncoder(String algorithm, CharSequence secret) {
		this.algorithm = algorithm;
		this.secret = utf8Encode(secret);
	}

	public String encode(CharSequence rawPassword) {
		byte[] salt = generateSalt();
		byte[] digest = this.digest(rawPassword, salt);
		return new String(this.hexEncode(digest));

	}

	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		byte[] digested = decode(encodedPassword);
		byte[] salt = subArray(digested, 0, saltIterations);
		return matches(digested, digest(rawPassword, salt));
	}

	private byte[] digest(CharSequence rawPassword, byte[] salt) {
		byte[] digest = digest(concatenate(salt, secret, utf8Encode(rawPassword)));
		return concatenate(salt, digest);
	}

	private byte[] decode(String encodedPassword) {
		return this.hexDecode(encodedPassword);

	}

	private boolean matches(byte[] expected, byte[] actual) {
		if (expected.length != actual.length) {
			return false;
		}

		int result = 0;
		for (int i = 0; i < expected.length; i++) {
			result |= expected[i] ^ actual[i];
		}
		return result == 0;
	}

	private byte[] generateSalt() {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[saltIterations];
		random.nextBytes(salt);
		return salt;
	}

	private byte[] digest(byte[] value) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("NoSuchAlgorithmException");
		}
		for (int i = 0; i < iterations; i++) {
			value = messageDigest.digest(value);
		}
		return value;
	}

	private boolean comparePasswords(byte[] originalHash, byte[] comparisonHash) {
		int diff = originalHash.length ^ comparisonHash.length;
		for (int i = 0; i < originalHash.length && i < comparisonHash.length; i++) {
			diff |= originalHash[i] ^ comparisonHash[i];
		}

		return diff == 0;
	}

	private static final Charset CHARSET = Charset.forName("UTF-8");

	private byte[] utf8Encode(CharSequence string) {
		try {
			ByteBuffer bytes = CHARSET.newEncoder().encode(CharBuffer.wrap(string));
			byte[] bytesCopy = new byte[bytes.limit()];
			System.arraycopy(bytes.array(), 0, bytesCopy, 0, bytes.limit());

			return bytesCopy;
		} catch (CharacterCodingException e) {
			throw new IllegalArgumentException("Encoding failed", e);
		}
	}

	private String utf8Decode(byte[] bytes) {
		try {
			return CHARSET.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
		} catch (CharacterCodingException e) {
			throw new IllegalArgumentException("Decoding failed", e);
		}
	}

	private char[] hexEncode(byte[] bytes) {
		final int nBytes = bytes.length;
		char[] result = new char[2 * nBytes];

		int j = 0;
		for (int i = 0; i < nBytes; i++) {
			// Char for top 4 bits
			result[j++] = HEX[(0xF0 & bytes[i]) >>> 4];
			// Bottom 4
			result[j++] = HEX[(0x0F & bytes[i])];
		}

		return result;
	}

	private byte[] hexDecode(CharSequence s) {
		int nChars = s.length();

		if (nChars % 2 != 0) {
			throw new IllegalArgumentException(
					"Hex-encoded string must have an even number of characters");
		}

		byte[] result = new byte[nChars / 2];

		for (int i = 0; i < nChars; i += 2) {
			int msb = Character.digit(s.charAt(i), 16);
			int lsb = Character.digit(s.charAt(i + 1), 16);

			if (msb < 0 || lsb < 0) {
				throw new IllegalArgumentException(
						"Detected a Non-hex character at " + (i + 1) + " or " + (i + 2) + " position");
			}
			result[i / 2] = (byte) ((msb << 4) | lsb);
		}
		return result;
	}

	private byte[] concatenate(byte[]... arrays) {
		int length = 0;
		for (byte[] array : arrays) {
			length += array.length;
		}
		byte[] newArray = new byte[length];
		int destPos = 0;
		for (byte[] array : arrays) {
			System.arraycopy(array, 0, newArray, destPos, array.length);
			destPos += array.length;
		}
		return newArray;
	}

	private byte[] subArray(byte[] array, int beginIndex, int endIndex) {
		int length = endIndex - beginIndex;
		byte[] subarray = new byte[length];
		System.arraycopy(array, beginIndex, subarray, 0, length);
		return subarray;
	}

}
