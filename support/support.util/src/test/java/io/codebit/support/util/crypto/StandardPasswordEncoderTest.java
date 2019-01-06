package io.codebit.support.util.crypto;

import io.codebit.support.util.crypto.StandardPasswordEncoder;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by probyoo on 2018. 8. 7..
 */
public class StandardPasswordEncoderTest {

	private StandardPasswordEncoder standardPasswordEncoder = new StandardPasswordEncoder("SHA-512", "");

	@Test
	public void matches() throws Exception {
		String oldPass = "cce0115502ab45d025a9c7ba120ec600577858ba81732e61a353b1ae7fc7c5c75645fe9d77515f3164423712d46788d67edf1386110b81bf30ea954cc87799125460600546835db3";
		String password = "test1234!";
		boolean matched = standardPasswordEncoder.matches(password, oldPass);
		System.out.println(matched);
		assertTrue(matched);
	}

}