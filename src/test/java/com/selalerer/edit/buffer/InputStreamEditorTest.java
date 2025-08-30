package com.selalerer.edit.buffer;

import com.selalerer.edit.StringEditor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class InputStreamEditorTest {

    @Test
    @SneakyThrows
    public void prettifyStreamStory() {

        var badStory = """
                I once saw a bad man that
                likes to hurt people.
                """;

        var editor = new StringEditor();

        editor.addReplacement("bad", "good");
        editor.addReplacement("hurt", "help");

        var testSubject = new InputStreamEditor(editor.getBufferEditor(), new ByteArrayInputStream(badStory.getBytes()));

        var result = new String(testSubject.readAllBytes());

        var expectedResult = """
                I once saw a good man that
                likes to help people.
                """;

        assertEquals(expectedResult, result);
    }

}