package com.selalerer.edit.buffer;

import com.selalerer.edit.DatumLocator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class BufferLocatorTest {

    @Test
    public void empty() {
        var testSubject = new BufferLocator(null);

        var inputBuffer = new byte[1024];

        Arrays.fill(inputBuffer, (byte) 3);

        var result = testSubject.findNext(inputBuffer, 0);

        assertEquals(Optional.empty(), result);
    }

    @Test
    public void notFound() {

        var testSubject = new BufferLocator(null);

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

        var testSubject = new BufferLocator(null);

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

        var testSubject = new BufferLocator(null);

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

        var testSubject = new BufferLocator(null);

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

    @Test
    public void longestWordListenerIsCalledWhenEnlarged() {
        var longestWordListenerCalled = new AtomicBoolean(false);
        var testSubject = new BufferLocator((a,b) -> longestWordListenerCalled.set(true));

        assertFalse(longestWordListenerCalled.get());
        testSubject.addMatcher(new byte[5]);
        assertTrue(longestWordListenerCalled.get());
        longestWordListenerCalled.set(false);

        testSubject.addMatcher(new byte[5]);
        assertFalse(longestWordListenerCalled.get());

        testSubject.addMatcher(new byte[6]);
        assertTrue(longestWordListenerCalled.get());
    }

    @Test
    public void removeMatchers() {
        var testSubject = new BufferLocator(null);

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

        testSubject.removeMatcher(matcher1);

        result = testSubject.findNext(inputBuffer, 157);

        assertEquals(Optional.empty(), result);
    }

    @Test
    public void longestWordListenerIsCalledWhenDiminish() {
        var longestWordListenerCalled = new AtomicBoolean(false);
        var newLongestWordValue = new AtomicInteger(0);

        var testSubject = new BufferLocator((a,b) -> {
            longestWordListenerCalled.set(true);
            newLongestWordValue.set(b);
        });

        assertFalse(longestWordListenerCalled.get());
        testSubject.addMatcher(new byte[5]);
        assertTrue(longestWordListenerCalled.get());
        assertEquals(5, newLongestWordValue.get());

        longestWordListenerCalled.set(false);
        newLongestWordValue.set(0);

        testSubject.addMatcher(new byte[5]);
        assertFalse(longestWordListenerCalled.get());
        assertEquals(0, newLongestWordValue.get());

        testSubject.addMatcher(new byte[4]);
        assertFalse(longestWordListenerCalled.get());
        assertEquals(0, newLongestWordValue.get());

        testSubject.removeMatcher(new byte[5]);
        assertTrue(longestWordListenerCalled.get());
        assertEquals(4, newLongestWordValue.get());
    }


    private void assertResult(DatumLocator.Result<Integer, byte[], byte[]> expected,
                              DatumLocator.Result<Integer, byte[], byte[]> actual) {

        assertEquals(expected.location(), actual.location(), "" + actual);
        assertArrayEquals(expected.matcher(), actual.matcher(), "" + actual);
        assertArrayEquals(expected.datum(), actual.datum(), "" + actual);
    }
}