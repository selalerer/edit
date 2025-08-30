package com.selalerer.edit;

import com.selalerer.edit.buffer.BufferEditor;
import lombok.Getter;

public class StringEditor implements Editor<String> {

    @Getter
    private final BufferEditor bufferEditor = new BufferEditor();

    public void addReplacement(String toReplace, String replacement) {
        bufferEditor.addReplacement(toReplace.getBytes(), replacement.getBytes());
    }

    @Override
    public String edit(String in) {
        return new String(bufferEditor.edit(in.getBytes()));
    }
}
