package com.selalerer.edit.buffer;

import com.selalerer.edit.DatumLocator;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BufferLocator implements DatumLocator<byte[], Integer, byte[], byte[]> {

    private final ConcurrentHashMap<ByteBuffer, Integer> matchers = new ConcurrentHashMap<>();
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
        var words = new ArrayList<ByteArrayOutputStream>();

        for (int curLocation = fromLocation; curLocation < in.length; ++curLocation) {

            addByteToWords(words, in[curLocation]);
            removeWordsThatAreTooLong(words);

            var matchingWord = getMatchingWord(words);
            if (matchingWord.isPresent()) {
                var location = curLocation - matchingWord.get().length + 1;
                return matchingWord
                        .map(w -> new Result<>(location, w, w));
            }
        }

        return Optional.empty();
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
