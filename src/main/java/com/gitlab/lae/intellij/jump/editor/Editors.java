package com.gitlab.lae.intellij.jump.editor;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.editor.Editor;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ESCAPE;
import static java.util.Collections.emptySet;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.intStream;
import static kotlin.text.StringsKt.indexOf;

public final class Editors {
    private Editors() {
    }

    public static Stream<EditorOffset> searchVisibleOffsets(
            Collection<? extends Editor> editors,
            String query,
            boolean ignoreCase
    ) {
        return editors.stream().flatMap(editor ->
                searchVisibleOffsets(editor, query, ignoreCase)
                        .mapToObj(offset -> EditorOffset.of(editor, offset)));
    }

    private static IntStream searchVisibleOffsets(
            Editor editor,
            String query,
            boolean ignoreCase
    ) {
        // TODO take folding into account
        Rectangle area = editor.getScrollingModel().getVisibleArea();
        int start = getAreaStartOffset(editor, area);
        int end = getAreaEndOffset(editor, area);
        return searchOffsets(editor, query, ignoreCase, start, end);
    }

    static IntStream searchOffsets(
            Editor editor,
            String query,
            boolean ignoreCase,
            int start,
            int end
    ) {
        PrimitiveIterator.OfInt it = new PrimitiveIterator.OfInt() {

            int currentStart = start - 1;
            CharSequence chars = editor.getDocument()
                    .getImmutableCharSequence()
                    .subSequence(0, end);

            int next = -1;
            boolean done = false;

            @Override
            public boolean hasNext() {
                if (next < 0) computeNext();
                return next >= 0;
            }

            @Override
            public int nextInt() {
                if (next < 0) computeNext();
                if (next < 0) throw new NoSuchElementException();
                int result = next;
                next = -1;
                return result;
            }

            private void computeNext() {
                if (done) return;
                currentStart = indexOf(chars, query, currentStart + 1, ignoreCase);
                next = currentStart;
                if (next < 0) {
                    done = true;
                }
            }
        };
        return intStream(spliteratorUnknownSize(it, 0), false);
    }

    private static int getAreaStartOffset(Editor editor, Rectangle area) {
        return editor.logicalPositionToOffset(editor.xyToLogicalPosition(
                new Point(area.x, area.y)));
    }

    private static int getAreaEndOffset(Editor editor, Rectangle area) {
        return editor.logicalPositionToOffset(editor.xyToLogicalPosition(
                new Point(area.x + area.width, area.y + area.height)));
    }

    public static Set<KeyStroke> getSingleStrokeEditorEscapeKeys(ActionManager actionManager) {
        AnAction action = actionManager.getActionOrStub(ACTION_EDITOR_ESCAPE);
        if (action == null) {
            return emptySet();
        }
        return Arrays.stream(action.getShortcutSet().getShortcuts())
                .map(it -> it instanceof KeyboardShortcut &&
                        ((KeyboardShortcut) it).getSecondKeyStroke() == null ?
                        ((KeyboardShortcut) it).getFirstKeyStroke() : null)
                .filter(Objects::nonNull)
                .collect(toSet());
    }
}
