package com.selalerer.edit;

import com.selalerer.edit.buffer.BufferEditor;
import lombok.Getter;

/***
 * An editor of strings.
 */
public class StringEditor implements Editor<String> {

    @Getter
    private final BufferEditor bufferEditor = new BufferEditor();

    /***
     * Add string to replace when editing data.
     * @param toReplace The string to replace.
     * @param replacement The replacement.
     */
    public void addReplacement(String toReplace, String replacement) {
        bufferEditor.addReplacement(toReplace.getBytes(), replacement.getBytes());
    }

    /***
     * Edit a given string as configured by calls to the addReplacement() method.
     * @param in The string to edit.
     * @return The edited string.
     */
    @Override
    public String edit(String in) {
        return new String(bufferEditor.edit(in.getBytes()));
    }
}
