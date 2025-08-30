package com.selalerer.edit.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class InputStreamEditor extends InputStream {

    private final BufferEditor bufferEditor;
    private final InputStream origin;
    private final byte[] inBuffer;
    private byte[] outBuffer;
    private boolean reachedEnd = false;
    private int outBufferLocation = 0;
    private boolean inBufferEmpty = true;

    public InputStreamEditor(BufferEditor bufferEditor, InputStream origin) {
        this.bufferEditor = bufferEditor;
        this.origin = origin;
        inBuffer = new byte[bufferEditor.getLongestWord()];
    }


    @Override
    public int read() throws IOException {
        if (outBuffer != null && outBuffer.length - outBufferLocation > 0) {
            ++outBufferLocation;
            return (int) outBuffer[outBufferLocation - 1];
        }
        outBuffer = null;
        if (reachedEnd) {
            return -1;
        }
        if (inBufferEmpty) {
            var readBytes = fillBuffer();
            if (readBytes == -1) {
                return -1;
            }
        } else {

            var nextByte = origin.read();
            if (nextByte == -1) {
                reachedEnd = true;
            } else {

            }
        }

        outBuffer = bufferEditor.edit(inBuffer);
        boolean edited = !Arrays.equals(inBuffer, outBuffer);

        var toReturn = (int)outBuffer[0];

        if (!edited) {
            outBuffer = null;
        } else {
            //inB
        }

        return (int) toReturn;
    }

    private int fillBuffer() throws IOException {
        var readBytes = origin.read(inBuffer);
        var totalReadBytes = readBytes;
        while (readBytes != -1 && totalReadBytes < inBuffer.length) {
            readBytes = origin.read(inBuffer, totalReadBytes, inBuffer.length - totalReadBytes);
            if (readBytes != -1) {
                totalReadBytes += readBytes;
            }
        }
        if (readBytes == -1) {
            reachedEnd = true;
        }
        return totalReadBytes;
    }

}
