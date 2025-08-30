package com.selalerer.edit.buffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/***
 * An input stream that wraps an origin input stream and edits the data read from the origin before returning it.
 */
public class InputStreamEditor extends InputStream {

    private final InputStream origin;
    private final BufferEditor.EditorSession session;
    private ByteArrayInputStream buffer;
    private final ByteArrayOutputStream sessionStream = new ByteArrayOutputStream();
    private boolean originEnded = false;

    /***
     * @param bufferEditor An editor that was configured and can be used to edit the origin input stream
     *                     supplied data.
     * @param origin The origin of the data to edit.
     */
    public InputStreamEditor(BufferEditor bufferEditor, InputStream origin) {
        this.origin = origin;
        this.session = bufferEditor.createEditorSession(sessionStream);
    }

    /***
     * Read the next edited byte.
     * @return The next edited byte.
     * @throws IOException if the origin stream throws an IOException.
     */
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
