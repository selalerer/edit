package com.selalerer.edit.buffer;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BufferSubstitutionSupplierTest {

    @Test
    public void testEmpty() {
        var testSubject = new BufferSubstitutionSupplier();

        var result = testSubject.getSubstitution(new byte[0], null);

        assertEquals(Optional.empty(), result);
    }

    @Test
    public void notFound() {
        var testSubject = new BufferSubstitutionSupplier();

        var b1 = new byte[5];
        var b2 = new byte[5];

        Arrays.fill(b1, (byte) 10);
        Arrays.fill(b2, (byte) 16);

        testSubject.addSubstitution(b1, b2);

        var result = testSubject.getSubstitution(b2, b2);

        assertEquals(Optional.empty(), result);
    }

    @Test
    public void found() {
        var testSubject = new BufferSubstitutionSupplier();

        var b1 = new byte[5];
        var b2 = new byte[5];

        Arrays.fill(b1, (byte) 10);
        Arrays.fill(b2, (byte) 16);

        testSubject.addSubstitution(b1, b2);

        var result = testSubject.getSubstitution(b1, b1);

        assertNotEquals(Optional.empty(), result);
        assertArrayEquals(b2, result.get());
    }

    @Test
    public void foundWithTwoOptions() {
        var testSubject = new BufferSubstitutionSupplier();

        var b1 = new byte[5];
        var b2 = new byte[5];
        var b3 = new byte[5];

        Arrays.fill(b1, (byte) 10);
        Arrays.fill(b2, (byte) 16);
        Arrays.fill(b3, (byte) 200);

        testSubject.addSubstitution(b3, b1);
        testSubject.addSubstitution(b1, b2);

        var result = testSubject.getSubstitution(b1, b1);

        assertNotEquals(Optional.empty(), result);
        assertArrayEquals(b2, result.get());
    }

}