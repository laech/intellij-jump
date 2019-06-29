package com.gitlab.lae.intellij.jump.editor;

import com.google.auto.value.AutoValue;
import com.intellij.openapi.editor.Editor;

@AutoValue
public abstract class EditorOffset {
    EditorOffset() {
    }

    public abstract Editor editor();

    public abstract int offset();

    static EditorOffset of(Editor editor, int offset) {
        return new AutoValue_EditorOffset(editor, offset);
    }
}
