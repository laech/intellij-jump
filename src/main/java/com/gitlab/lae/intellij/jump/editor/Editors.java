package com.gitlab.lae.intellij.jump.editor;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ESCAPE;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
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
        return intStream(
                spliteratorUnknownSize(
                        new OffsetIterator(editor, query, ignoreCase, start, end),
                        0),
                false);
    }

    private static class OffsetIterator implements PrimitiveIterator.OfInt {

        private final NavigableMap<Integer, FoldRegion> regionsByStartOffset;
        private final CharSequence chars;
        private final String query;
        private final boolean ignoreCase;

        private int currentStart;
        private int next = -1;
        private boolean done = false;

        OffsetIterator(
                Editor editor,
                String query,
                boolean ignoreCase,
                int start,
                int end
        ) {
            this.currentStart = start - 1;
            this.chars = editor.getDocument()
                    .getImmutableCharSequence()
                    .subSequence(0, end);
            this.regionsByStartOffset = getFoldRegionByStartOffset(editor);
            this.query = requireNonNull(query);
            this.ignoreCase = ignoreCase;
        }

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
            boolean found = false;
            while (!done && !found) {
                currentStart = indexOf(chars, query, currentStart + 1, ignoreCase);
                next = currentStart;
                if (next < 0) {
                    done = true;
                } else {

                    Map.Entry<Integer, FoldRegion> entry =
                            regionsByStartOffset.floorEntry(next);

                    if (entry == null || entry.getValue().getEndOffset() <= next) {
                        found = true;
                    }
                }
            }
        }
    }

    private static NavigableMap<Integer, FoldRegion> getFoldRegionByStartOffset(Editor editor) {
        return stream(editor.getFoldingModel().getAllFoldRegions())
                .filter(o -> !o.isExpanded())
                .collect(toMap(
                        FoldRegion::getStartOffset,
                        identity(),
                        (a, b) -> a.getEndOffset() >= b.getEndOffset() ? a : b,
                        TreeMap::new));
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
        return stream(action.getShortcutSet().getShortcuts())
                .map(it -> it instanceof KeyboardShortcut &&
                        ((KeyboardShortcut) it).getSecondKeyStroke() == null ?
                        ((KeyboardShortcut) it).getFirstKeyStroke() : null)
                .filter(Objects::nonNull)
                .collect(toSet());
    }
}
