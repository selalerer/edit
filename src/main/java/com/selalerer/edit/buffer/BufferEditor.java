package com.selalerer.edit.buffer;

import com.selalerer.edit.Editor;
import com.selalerer.edit.LongestWordChangeListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***
 * A byte array editor.
 */
@Slf4j
public class BufferEditor implements Editor<byte[]>, LongestWordChangeListener {

    private final BufferLocator locator = new BufferLocator(this);
    private final BufferSubstitutionSupplier substitutionSupplier = new BufferSubstitutionSupplier();
    private final List<EditorSession> activeSessions = new ArrayList<>();

    /***
     * Add a desired replacement when editing the input buffer.
     * @param matcher The byte sequence to search and replace.
     * @param replacement The replacement to use.
     */
    public void addReplacement(byte[] matcher, byte[] replacement) {
        substitutionSupplier.addSubstitution(matcher, replacement);
        locator.addMatcher(matcher);
    }

    /***
     * Remove previously added replacement.
     */
    public void removeReplacement(byte[] matcher) {
        locator.removeMatcher(matcher);
        substitutionSupplier.removeSubstitution(matcher);
    }

    /***
     * @return The length of the longest searched byte sequence.
     */
    public int getLongestWord() {
        return locator.getLongestWord();
    }

    /***
     * Edit a buffer, replacing the searched bytes sequences with their replacements as defined by calls to
     * addReplacement() method.
     * @param in The input buffer.
     * @return The edited buffer.
     */
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

    /***
     * Starting an editing session. More friendly when reading input from a stream.
     * @param o The stream to write the edited result into.
     * @return The new session.
     */
    public EditorSession createEditorSession(OutputStream o) {
        var session = new EditorSession(o);
        synchronized (activeSessions) {
            activeSessions.add(session);
        }
        return session;
    }

    @Override
    public void longestWordUpdated(int previousLongestWord, int newLongestWord) {
        List<EditorSession> activeSessionsCopy;
        synchronized (activeSessions) {
            activeSessionsCopy = activeSessions.stream().toList();
        }
        for (var session : activeSessionsCopy) {
            session.longestWordUpdated(previousLongestWord, newLongestWord);
        }
    }

    /***
     * An editing session.
     */
    public class EditorSession implements AutoCloseable, LongestWordChangeListener {

        private final OutputStream o;
        private byte[] buffer;
        private int validBytes = 0;
        private final BufferLocator.LocatorSession locatorSession;

        /***
         * @param o The stream to write the edited data into.
         */
        public EditorSession(OutputStream o) {
            this.o = o;
            buffer = new byte[getLongestWord()];
            locatorSession = locator.startLocatorSession(0);
        }

        /***
         * Add another input byte.
         * @param b The next input byte.
         */
        @SneakyThrows
        public synchronized void addByte(byte b) {

            // No replacements
            if (buffer.length == 0) {
                o.write(b);
                return;
            }

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

        /***
         * Close the session, writing to the output any leftover bytes from the input that weren't
         * substituted.
         */
        @SneakyThrows
        public synchronized void close() {
            synchronized (activeSessions) {
                activeSessions.remove(this);
            }
            if  (validBytes > 0) {
                o.write(buffer, 0, validBytes);
                validBytes = 0;
            }
            o.close();
        }

        @Override
        public synchronized void longestWordUpdated(int previousLongestWord, int newLongestWord) {
            if (newLongestWord != buffer.length) {
                var newBuffer = new byte[newLongestWord];
                System.arraycopy(buffer, 0, newBuffer, 0, Math.min(buffer.length, newBuffer.length));
                if (validBytes > newBuffer.length) {
                    validBytes = newBuffer.length;
                }
                buffer = newBuffer;
            }
        }
    }
}
