package com.selalerer.edit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringEditorTest {

    @Test
    public void prettifyStory() {

        var badStory = """
                I once saw a bad man that
                likes to hurt people.
                """;

        var testSubject = new StringEditor();

        testSubject.addReplacement("bad", "good");
        testSubject.addReplacement("hurt", "help");

        var result = testSubject.edit(badStory);

        var expectedResult = """
                I once saw a good man that
                likes to help people.
                """;

        assertEquals(expectedResult, result);
    }
}