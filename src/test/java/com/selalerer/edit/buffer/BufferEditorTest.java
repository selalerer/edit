package com.selalerer.edit.buffer;

import com.selalerer.edit.ConfigureTradeLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ConfigureTradeLog.class)
public class BufferEditorTest {

    @Test
    public void testNull() {

        var testSubject = new BufferEditor();

        var result = testSubject.edit(null);

        assertNull(result);
    }

    @Test
    public void noEdit() {

        var testSubject = new BufferEditor();

        var input = new byte[2048];

        Arrays.fill(input, (byte) 55);
        byte[] inputCopy = input.clone();

        var result = testSubject.edit(input);

        assertArrayEquals(inputCopy, result);
        assertArrayEquals(inputCopy, input);
    }

    @Test
    public void sameSizeReplacement() {

        var testSubject = new BufferEditor();

        var input = new byte[2048];
        var matcher1 = new byte[4];
        var replacement1 = new byte[4];
        var matcher2 = new byte[4];

        Arrays.fill(input, (byte) 55);
        Arrays.fill(input, 500, 504, (byte) 111);
        Arrays.fill(input, 1000, 1004, (byte) 88);

        Arrays.fill(matcher1, (byte) 88);
        Arrays.fill(replacement1, (byte) 99);
        Arrays.fill(matcher2, (byte) 111);

        testSubject.addReplacement(matcher1, replacement1);
        testSubject.addReplacement(matcher2, replacement1);

        var result = testSubject.edit(input);

        var expected = new byte[2048];
        Arrays.fill(expected, (byte) 55);
        Arrays.fill(expected, 500, 504, (byte) 99);
        Arrays.fill(expected, 1000, 1004, (byte) 99);

        assertArrayEquals(expected, result);
    }

    @Test
    public void biggerReplacements() {

        var testSubject = new BufferEditor();

        var input = new byte[2048];
        var matcher1 = new byte[4];
        var replacement1 = new byte[14];
        var matcher2 = new byte[4];

        Arrays.fill(input, (byte) 55);
        Arrays.fill(input, 500, 504, (byte) 111);
        Arrays.fill(input, 1000, 1004, (byte) 88);

        Arrays.fill(matcher1, (byte) 88);
        Arrays.fill(replacement1, (byte) 99);
        Arrays.fill(matcher2, (byte) 111);

        testSubject.addReplacement(matcher1, replacement1);
        testSubject.addReplacement(matcher2, replacement1);

        var result = testSubject.edit(input);

        var expected = new byte[2048 + 10 + 10];
        Arrays.fill(expected, (byte) 55);
        Arrays.fill(expected, 500, 514, (byte) 99);
        Arrays.fill(expected, 1000 + 10, 1000 + 10 + 14, (byte) 99);

        assertArrayEquals(expected, result);
    }

}