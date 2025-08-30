package com.selalerer.edit.buffer;

import com.selalerer.edit.DatumLocator;
import com.selalerer.edit.Editor;
import com.selalerer.edit.SubstitutionSupplier;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class BufferEditor implements Editor<byte[]> {

    private final BufferLocator locator = new BufferLocator();
    private final BufferSubstitutionSupplier substitutionSupplier = new BufferSubstitutionSupplier();

    @Override
    public byte[] edit(byte[] in) {

        int inOffset = 0;

        var found = locator.findNext(in, inOffset);
        if (found.isEmpty()) {
            return in;
        }

        var resultBuilder = new ByteArrayOutputStream(in.length);

        while (found.isPresent()) {
            var foundResult = found.get();

            int sizeUntilFound = foundResult.location() - inOffset;

            resultBuilder.write(in, inOffset, sizeUntilFound);
            inOffset = foundResult.location() + foundResult.datum().length;

            substitutionSupplier.getSubstitution(foundResult.matcher(), foundResult.datum())
                    .ifPresentOrElse(substitute -> resultBuilder.writeBytes(foundResult.datum()),
                            () -> { throw new RuntimeException("No substitution found for datum. Offset: " +
                                    foundResult.location() + ". Datum: " + Arrays.toString(foundResult.datum())); });

            found = locator.findNext(in, inOffset);
        }

        resultBuilder.write(in, inOffset, in.length - inOffset);

        return resultBuilder.toByteArray();
    }
}
