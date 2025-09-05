# edit
A java library for editing buffers.

Maven dependency:
```xml
        <dependency>
            <groupId>com.selalerer</groupId>
            <artifactId>edit</artifactId>
            <version>1.0.0</version>
        </dependency>
```

Edit strings:
```java
        var badStory = """
                I once saw a bad man that
                likes to hurt people.
                """;

        var editor = new StringEditor();

        editor.addReplacement("bad", "good");
        editor.addReplacement("hurt", "help");

        var result = testSubject.edit(badStory);
```

Also works with input streams:
```java
        var badStory = """
                I once saw a bad man that
                likes to hurt people.
                """;

        var editor = new StringEditor();

        editor.addReplacement("bad", "good");
        editor.addReplacement("hurt", "help");
        
        var stream = new InputStreamEditor(editor.getBufferEditor(), new ByteArrayInputStream(badStory.getBytes()));
        
        var result = new String(stream.readAllBytes());
```

Or output streams:
```java
        var badStory = """
                I once saw a bad man that
                likes to hurt people.
                """;

        var editor = new StringEditor();

        editor.addReplacement("bad", "good");
        editor.addReplacement("hurt", "help");
        
        var underlyingStream = new ByteArrayOutputStream();
        try (var stream = new OutputStreamEditor(editor.getBufferEditor(), underlyingStream);) {
            
            stream.write(badStory.getBytes());
        }
        
        var result = new String(underlyingStream.toByteArray());
```
