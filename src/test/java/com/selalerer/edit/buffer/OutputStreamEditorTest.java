package com.selalerer.edit.buffer;

import com.selalerer.edit.StringEditor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class OutputStreamEditorTest {

    @Test
    @SneakyThrows
    public void prettifyOutputStreamStory() {

        var badStory = """
                I once saw a bad man that
                likes to hurt people.
                """;

        var editor = new StringEditor();

        editor.addReplacement("bad", "good");
        editor.addReplacement("hurt", "help");

        var output = new ByteArrayOutputStream();
        try (var testSubject = new OutputStreamEditor(editor.getBufferEditor(), output);) {

            testSubject.write(badStory.getBytes());
        }

        var result = new String(output.toByteArray());

        var expectedResult = """
                I once saw a good man that
                likes to help people.
                """;

        assertEquals(expectedResult, result);
    }

}