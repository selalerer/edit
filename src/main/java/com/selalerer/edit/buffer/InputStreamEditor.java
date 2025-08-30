package com.selalerer.edit.buffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamEditor extends InputStream {

    private final BufferEditor bufferEditor;
    private final InputStream origin;
    private final BufferEditor.EditorSession session;
    private ByteArrayInputStream buffer;
    private final ByteArrayOutputStream sessionStream = new ByteArrayOutputStream();
    private boolean originEnded = false;

    public InputStreamEditor(BufferEditor bufferEditor, InputStream origin) {
        this.bufferEditor = bufferEditor;
        this.origin = origin;
        this.session = bufferEditor.createEditorSession(sessionStream);
    }


    @Override
    public int read() throws IOException {

        if (buffer != null) {
            int result = buffer.read();
            if (result !=  -1) {
                return result;
            }
            buffer = null;
        }

        if (originEnded) {
            return -1;
        }

        while (sessionStream.size() <= 0) {
            var input = origin.read();
            if (input == -1) {
                originEnded = true;
                break;
            }

            session.addByte((byte) input);
        }

        if (originEnded) {
            session.close();
        }

        buffer = new ByteArrayInputStream(sessionStream.toByteArray());
        sessionStream.reset();

        return buffer.read();
    }

}
