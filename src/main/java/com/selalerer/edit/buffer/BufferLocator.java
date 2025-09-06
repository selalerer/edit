package com.selalerer.edit.buffer;

import com.selalerer.edit.DatumLocator;
import com.selalerer.edit.LongestWordChangeListener;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.*;

/***
 * Locates sequences of bytes in an input buffer.
 */
@RequiredArgsConstructor
public class BufferLocator implements DatumLocator<byte[], Integer, byte[], byte[]> {

    private final HashSet<ByteBuffer> matchers = new HashSet<>();
    private final LongestWordChangeListener longestWordChangeListener;

    /***
     * Add sequence of bytes to search for.
     * @param matcher The searched bytes sequence.
     */
    public synchronized void addMatcher(byte[] matcher) {
        if (matcher.length == 0) {
            throw new RuntimeException("match length must not be zero");
        }
        matchersChange(() -> matchers.add(ByteBuffer.wrap(matcher)));
    }

    /**
     * Remove sequence of bytes to search for.
     * @param matcher The no longer searched bytes sequence.
     */
    public void removeMatcher(byte[] matcher) {
        matchersChange(() -> matchers.remove(ByteBuffer.wrap(matcher)));
    }

    private void matchersChange(Runnable change) {
        int longestWordBefore, longestWordAfter;

        synchronized (this) {
            longestWordBefore = getLongestWord();
            change.run();
            longestWordAfter = getLongestWord();
        }

        if (longestWordBefore != longestWordAfter && longestWordChangeListener != null) {
            longestWordChangeListener.longestWordUpdated(longestWordBefore, longestWordAfter);
        }
    }

    /**
     * @return The longest word that is currently searched for by this locator.
     */
    public synchronized int getLongestWord() {
        return matchers.stream()
                .mapToInt(k -> k.array().length)
                .reduce(0, Math::max);
    }

    /***
     * Finds the next searched bytes sequence.
     * @param in The buffer to search in.
     * @param fromLocation The location in the buffer to start the search from.
     * @return A result with the location and found bytes sequence or empty() if not found.
     */
    @Override
    public Optional<Result<Integer, byte[], byte[]>> findNext(byte[] in, Integer fromLocation) {

        var session = new LocatorSession(fromLocation);

        for (int curLocation = fromLocation; curLocation < in.length; ++curLocation) {

            var result = session.addByte(in[curLocation]);
            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }

    /***
     * Start a search session. More friendly to search in streams than the findNext() method.
     * @param fromLocation Should be 0 if the search starts from the beginning of the input or
     *                     the offset of the search start if the search is starting from another location.
     * @return The search session.
     */
    public LocatorSession startLocatorSession(int fromLocation) {
        return new LocatorSession(fromLocation);
    }

    /***
     * A search session.
     */
    public class LocatorSession {
        private final List<ByteArrayOutputStream> words = new ArrayList<>();
        private int location;

        /***
         * @param fromLocation Should be 0 if the search starts from the beginning of the input or
         *                     the offset of the search start if the search is starting from another location.
         */
        public LocatorSession(int fromLocation) {
            location = fromLocation;
        }

        /***
         * Add another byte to scanned input.
         * @param b A new byte from the input.
         * @return A result of a found byte sequence or empty() if not found yet.
         */
        public Optional<Result<Integer, byte[], byte[]>> addByte(byte b) {
            ++location;
            addByteToWords(words, b);
            removeWordsThatAreTooLong(words);
            return getMatchingWord(words)
                    .map(w -> new Result<>(location - w.length, w, w));
        }
    }

    private void addByteToWords(List<ByteArrayOutputStream> words, byte b) {
        // This byte starts a new word.
        words.add(new ByteArrayOutputStream());

        words.forEach(s -> s.write(b));
    }

    private void removeWordsThatAreTooLong(List<ByteArrayOutputStream> words) {
        var longestWord = getLongestWord();
        words.removeIf(w -> w.size() > longestWord);
    }

    private Optional<byte[]> getMatchingWord(List<ByteArrayOutputStream> words) {
        return words.stream()
                .map(ByteArrayOutputStream::toByteArray)
                .filter(word -> matchers.contains(ByteBuffer.wrap(word)))
                .findFirst();
    }

}
