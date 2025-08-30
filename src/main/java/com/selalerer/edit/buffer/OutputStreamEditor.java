package com.selalerer.edit.buffer;

import java.io.IOException;
import java.io.OutputStream;

/***
 * An output stream that wraps a destination output stream and edits the data given before
 * passing it along to the underlying destination output stream.
 */
public class OutputStreamEditor extends OutputStream {

    private final BufferEditor.EditorSession sesion;

    /***
     * @param editor An editor configured with all the editing that data has to undergo.
     * @param destination The output stream to write the edited data into.
     */
    public OutputStreamEditor(BufferEditor editor, OutputStream destination) {
        this.sesion = editor.createEditorSession(destination);
    }

    /***
     * Add another byte to the input to edit.
     * @param i The byte
     */
    @Override
    public void write(int i) throws IOException {
        sesion.addByte((byte) i);
    }

    /***
     * Close the stream, flushing any buffered unwritten data into the destination output stream.
     */
    @Override
    public void close() {
        sesion.close();
    }
}
