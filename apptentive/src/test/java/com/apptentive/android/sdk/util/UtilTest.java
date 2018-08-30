package com.apptentive.android.sdk.util;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class UtilTest {

	@Test
	public void getEncryptedFilename() {
		File file = new File("/data/user/0/test.bin");
		File encryptedFile = Util.getEncryptedFilename(file);
		assertEquals(new File("/data/user/0/test.bin.encrypted"), encryptedFile);
		File decryptedFile = Util.getUnencryptedFilename(encryptedFile);
		assertEquals(file, decryptedFile);
	}
}