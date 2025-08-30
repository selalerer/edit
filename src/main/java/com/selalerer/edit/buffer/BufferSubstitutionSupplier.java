package com.selalerer.edit.buffer;

import com.selalerer.edit.SubstitutionSupplier;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BufferSubstitutionSupplier implements SubstitutionSupplier<byte[], byte[]> {

    private final ConcurrentHashMap<ByteBuffer, byte[]> substitutions = new ConcurrentHashMap<>();

    public void addSubstitution(byte[] matcher, byte[] substitute) {
        substitutions.put(ByteBuffer.wrap(matcher), substitute);
    }

    @Override
    public Optional<byte[]> getSubstitution(byte[] matcher, byte[] current) {
        return Optional.ofNullable(substitutions.get(ByteBuffer.wrap(matcher)));
    }
}
