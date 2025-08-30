package com.selalerer.edit.buffer;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamEditor extends OutputStream {

    private final BufferEditor.EditorSession sesion;

    public OutputStreamEditor(BufferEditor editor, OutputStream destination) {
        this.sesion = editor.createEditorSession(destination);
    }

    @Override
    public void write(int i) throws IOException {
        sesion.addByte((byte) i);
    }

    @Override
    public void close() {
        sesion.close();
    }
}
