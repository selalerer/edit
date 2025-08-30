package com.selalerer.edit.buffer;

import com.selalerer.edit.DatumLocator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BufferLocatorTest {

    @Test
    public void empty() {
        var testSubject = new BufferLocator();

        var inputBuffer = new byte[1024];

        Arrays.fill(inputBuffer, (byte) 3);

        var result = testSubject.findNext(inputBuffer, 0);

        assertEquals(Optional.empty(), result);
    }

    @Test
    public void notFound() {

        var testSubject = new BufferLocator();

        var inputBuffer = new byte[1024];
        var matcher1 = new byte[10];

        Arrays.fill(inputBuffer, (byte) 3);
        Arrays.fill(matcher1, (byte) 5);

        testSubject.addMatcher(matcher1);

        var result = testSubject.findNext(inputBuffer, 0);

        assertEquals(Optional.empty(), result);
    }

    @Test
    public void found() {

        var testSubject = new BufferLocator();

        var inputBuffer = new byte[1024];
        var matcher1 = new byte[10];

        Arrays.fill(inputBuffer, (byte) 3);
        Arrays.fill(matcher1, (byte) 5);
        Arrays.fill(inputBuffer, 300, 310, (byte) 5);

        testSubject.addMatcher(matcher1);

        var result = testSubject.findNext(inputBuffer, 0);

        assertNotEquals(Optional.empty(), result);

        var expected = new DatumLocator.Result<>(300, matcher1, matcher1);
        assertResult(expected, result.get());
    }

    @Test
    public void foundFirstThenSecondThenNone() {

        var testSubject = new BufferLocator();

        var inputBuffer = new byte[1024];
        var matcher1 = new byte[10];

        Arrays.fill(inputBuffer, (byte) 3);
        Arrays.fill(matcher1, (byte) 5);
        Arrays.fill(inputBuffer, 300, 310, (byte) 5);
        Arrays.fill(inputBuffer, 150, 160, (byte) 5);

        testSubject.addMatcher(matcher1);

        var result = testSubject.findNext(inputBuffer, 0);

        assertNotEquals(Optional.empty(), result);
        var expected = new DatumLocator.Result<>(150, matcher1, matcher1);
        assertResult(expected, result.get());

        result = testSubject.findNext(inputBuffer, 151);

        assertNotEquals(Optional.empty(), result);
        expected = new DatumLocator.Result<>(300, matcher1, matcher1);
        assertResult(expected, result.get());

        result = testSubject.findNext(inputBuffer, 301);

        assertEquals(Optional.empty(), result);
    }

    @Test
    public void findTwoMatchers() {

        var testSubject = new BufferLocator();

        var inputBuffer = new byte[1024];
        var matcher1 = new byte[10];
        var matcher2 = new byte[7];

        Arrays.fill(inputBuffer, (byte) 3);
        Arrays.fill(matcher1, (byte) 5);
        Arrays.fill(matcher2, (byte) 67);
        Arrays.fill(inputBuffer, 150, 160, (byte) 67);
        Arrays.fill(inputBuffer, 300, 310, (byte) 5);

        testSubject.addMatcher(matcher1);
        testSubject.addMatcher(matcher2);

        var result = testSubject.findNext(inputBuffer, 0);

        assertNotEquals(Optional.empty(), result);
        var expected = new DatumLocator.Result<>(150, matcher2, matcher2);
        assertResult(expected, result.get());

        result = testSubject.findNext(inputBuffer, 157);

        assertNotEquals(Optional.empty(), result);
        expected = new DatumLocator.Result<>(300, matcher1, matcher1);
        assertResult(expected, result.get());

        result = testSubject.findNext(inputBuffer, 301);

        assertEquals(Optional.empty(), result);
    }

    private void assertResult(DatumLocator.Result<Integer, byte[], byte[]> expected,
                              DatumLocator.Result<Integer, byte[], byte[]> actual) {

        assertEquals(expected.location(), actual.location());
        assertArrayEquals(expected.matcher(), actual.matcher());
        assertArrayEquals(expected.datum(), actual.datum());
    }
}