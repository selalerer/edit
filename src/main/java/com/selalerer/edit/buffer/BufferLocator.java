package com.selalerer.edit.buffer;

import com.selalerer.edit.DatumLocator;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BufferLocator implements DatumLocator<byte[], Integer, byte[], byte[]> {

    private final ConcurrentHashMap<ByteBuffer, Integer> matchers = new ConcurrentHashMap<>();
    @Getter
    private int longestWord = 0;

    public void addMatcher(byte[] matcher) {
        if (matcher.length == 0) {
            throw new RuntimeException("match length must not be zero");
        }
        matchers.put(ByteBuffer.wrap(matcher), 0);
        if (matcher.length > longestWord) {
            longestWord = matcher.length;
        }
    }

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

    public LocatorSession startLocatorSession(int fromLocation) {
        return new LocatorSession(fromLocation);
    }

    public class LocatorSession {
        private final List<ByteArrayOutputStream> words = new ArrayList<>();
        private int location;

        public LocatorSession(int startLocation) {
            location = startLocation;
        }

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
        words.removeIf(w -> w.size() > longestWord);
    }

    private Optional<byte[]> getMatchingWord(List<ByteArrayOutputStream> words) {
        return words.stream()
                .map(ByteArrayOutputStream::toByteArray)
                .filter(word -> matchers.containsKey(ByteBuffer.wrap(word)))
                .findFirst();
    }

}
