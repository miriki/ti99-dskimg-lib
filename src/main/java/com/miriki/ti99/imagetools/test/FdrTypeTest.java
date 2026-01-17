package com.miriki.ti99.imagetools.test;

import com.miriki.ti99.imagetools.domain.*;
import com.miriki.ti99.imagetools.domain.io.FileTypeIO;
import com.miriki.ti99.imagetools.fs.FileWriter;

import java.util.Arrays;

public class FdrTypeTest {

	public static void main(String[] args) {

	    test("PROGRAM", 0,    0x00, 0x01);
	    test("DIS/FIX", 80,   0x00, 0x00);
	    test("DIS/VAR", 80,   0x80, 0x80);
	    test("INT/FIX", 80,   0x02, 0x02);
	    test("INT/VAR", 80,   0x82, 0x82);
	}

	private static void test(String label, int recordLength, int fiadFlags, int expectedStatus) {

	    System.out.println("=== TEST: " + label + " ===");

	    byte[] content = new byte[recordLength > 0 ? recordLength * 10 : 2000];
	    Arrays.fill(content, (byte) 0xAA);

	    Ti99File file = new Ti99File(label, label, recordLength, content);
	    file.setFlags(fiadFlags);

	    FileDescriptorRecord fdr = FileWriter.buildFdr(file, Arrays.asList(5,6,7), null);

	    int status = fdr.getFileStatus();
	    String decoded = FileTypeIO.decode(status);

	    System.out.println("Expected Status: " + String.format("0x%02X", expectedStatus));
	    System.out.println("Actual Status:   " + String.format("0x%02X", status));
	    System.out.println("Decoded Type:    " + decoded);
	    System.out.println("Matches?         " + (status == expectedStatus));
	    System.out.println();
	}
}
