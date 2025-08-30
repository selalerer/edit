package com.selalerer.edit.buffer;

import com.selalerer.edit.SubstitutionSupplier;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Supplies substitutes to be used when editing a buffer.
 */
public class BufferSubstitutionSupplier implements SubstitutionSupplier<byte[], byte[]> {

    private final ConcurrentHashMap<ByteBuffer, byte[]> substitutions = new ConcurrentHashMap<>();

    /***
     * Add a mapping between found byte sequence and its substitution.
     * @param matcher The byte sequence to substitute.
     * @param substitute The byte sequence that will be used as substitution.
     */
    public void addSubstitution(byte[] matcher, byte[] substitute) {
        substitutions.put(ByteBuffer.wrap(matcher), substitute);
    }

    /***
     * @param matcher The bytes sequence that was used to find the substituted bytes.
     * @param current The bytes that will be substituted.
     * @return The substitution bytes sequence.
     */
    @Override
    public Optional<byte[]> getSubstitution(byte[] matcher, byte[] current) {
        return Optional.ofNullable(substitutions.get(ByteBuffer.wrap(matcher)));
    }
}
