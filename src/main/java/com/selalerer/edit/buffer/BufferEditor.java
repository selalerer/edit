package com.selalerer.edit.buffer;

import com.selalerer.edit.Editor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

@Slf4j
public class BufferEditor implements Editor<byte[]> {

    private final BufferLocator locator = new BufferLocator();
    private final BufferSubstitutionSupplier substitutionSupplier = new BufferSubstitutionSupplier();

    public void addReplacement(byte[] matcher, byte[] replacement) {
        locator.addMatcher(matcher);
        substitutionSupplier.addSubstitution(matcher, replacement);
    }

    public int getLongestWord() {
        return locator.getLongestWord();
    }

    @Override
    public byte[] edit(byte[] in) {
        log.trace("edit(): STARTED");

        if (in == null) {
            return null;
        }

        int inOffset = 0;

        var found = locator.findNext(in, inOffset);
        if (found.isEmpty()) {
            log.trace("Nothing found to replace in input buffer. Returning input buffer as is.");
            return in;
        }

        var resultBuilder = new ByteArrayOutputStream(in.length);

        while (found.isPresent()) {
            var foundResult = found.get();
            log.trace("Found matcher at offset {}", foundResult.location());

            if (foundResult.location() < inOffset) {
                throw new RuntimeException("Locator returned a location that is before the beginning of the search." +
                        " Location: " + foundResult.location() + ". Beginning of the search: " + inOffset);
            }

            int sizeUntilFound = foundResult.location() - inOffset;

            log.trace("Copying {} bytes from offset {}", sizeUntilFound, inOffset);
            resultBuilder.write(in, inOffset, sizeUntilFound);

            inOffset = foundResult.location() + foundResult.datum().length;
            log.trace("Moving to offset {}", inOffset);

            substitutionSupplier.getSubstitution(foundResult.matcher(), foundResult.datum())
                    .ifPresentOrElse(substitute -> {
                                log.trace("Writing substitution of size {}", substitute.length);
                                resultBuilder.writeBytes(substitute);
                            },
                            () -> {
                                throw new RuntimeException("No substitution found for datum. Offset: " +
                                        foundResult.location() + ". Datum: " + Arrays.toString(foundResult.datum()));
                            });

            if (inOffset >= in.length) {
                inOffset = in.length;
                break;
            }

            found = locator.findNext(in, inOffset);
        }

        var leftOverSize = in.length - inOffset;
        log.trace("Copying left over {} bytes from offset {}", leftOverSize, inOffset);
        resultBuilder.write(in, inOffset, leftOverSize);

        return resultBuilder.toByteArray();
    }

    public class EditorSession {

        private final OutputStream o;
        private int location = 0;
        private final byte[] buffer;
        private int validBytes = 0;
        private final BufferLocator.LocatorSession locatorSession;

        public EditorSession(OutputStream o) {
            this.o = o;
            buffer = new byte[getLongestWord()];
            locatorSession = locator.startLocatorSession(0);
        }

        @SneakyThrows
        public void addByte(byte b) {
            ++location;
            if (validBytes >= buffer.length) {
                // Can write out the oldest byte.
                o.write(buffer[0]);
                System.arraycopy(buffer, 1, buffer, 0, buffer.length - 1);
                buffer[buffer.length - 1] = b;
            } else {
                buffer[validBytes] = b;
                ++validBytes;
            }

            var resultOpt = locatorSession.addByte(b);
            if (resultOpt.isEmpty()) {
                return;
            }

            var result = resultOpt.get();

            var replacement = substitutionSupplier.getSubstitution(result.matcher(), result.datum())
                    .orElseThrow(() -> new RuntimeException("No substitution found for datum. Offset: " +
                            result.location() + ". Datum: " + Arrays.toString(result.datum())));

            // The entire replacement can be written out.
            o.write(replacement);

            // Now need to remove the replaced data.
            validBytes -= result.datum().length;
        }

        @SneakyThrows
        public void flush() {
            if  (validBytes > 0) {
                o.write(buffer, 0, validBytes);
            }
        }
    }
}
