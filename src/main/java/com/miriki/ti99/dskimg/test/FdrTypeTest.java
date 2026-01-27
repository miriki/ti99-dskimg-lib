package com.miriki.ti99.dskimg.test;

import java.util.Arrays;

import com.miriki.ti99.dskimg.domain.*;
import com.miriki.ti99.dskimg.domain.enums.FileType;
import com.miriki.ti99.dskimg.domain.enums.RecordFormat;
import com.miriki.ti99.dskimg.domain.io.FileTypeIO;
import com.miriki.ti99.dskimg.fs.FileWriter;

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

	    FileDescriptorRecord fdr = FileWriter.buildFdrForTest(file, Arrays.asList(5,6,7));

	    int status = fdr.getFileStatus();
	    // String decoded = FileTypeIO.decode(status);
	    FileType decodedType = FileTypeIO.decode(status);
	    RecordFormat decodedFormat = FileTypeIO.decodeFormat(status);
	    String decoded = FileTypeIO.toUiLabel(decodedType, decodedFormat, recordLength);

	    System.out.println("Expected Status: " + String.format("0x%02X", expectedStatus));
	    System.out.println("Actual Status:   " + String.format("0x%02X", status));
	    System.out.println("Decoded Type:    " + decoded);
	    System.out.println("Matches?         " + (status == expectedStatus));
	    System.out.println();
	}
}
