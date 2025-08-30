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
        if (in == null) {
            return null;
        }

        var output = new ByteArrayOutputStream(in.length);

        try (var session = createEditorSession(output);) {

            for (var b : in) {
                session.addByte(b);
            }
        }

        return output.toByteArray();
    }

    public EditorSession createEditorSession(OutputStream o) {
        return new EditorSession(o);
    }

    public class EditorSession implements AutoCloseable {

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

            // No replacements
            if (buffer.length == 0) {
                o.write(b);
                return;
            }

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

            // Remove the replaced data.
            validBytes -= result.datum().length;

            // Can write the buffer until the point of replacement.
            if (validBytes > 0) {
                o.write(buffer, 0, validBytes);
                validBytes = 0;
            }

            // The entire replacement can be written out.
            o.write(replacement);
        }

        @SneakyThrows
        public void close() {
            if  (validBytes > 0) {
                o.write(buffer, 0, validBytes);
                validBytes = 0;
            }
            o.close();
        }
    }
}
