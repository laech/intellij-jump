package com.gitlab.lae.intellij.jump.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.FoldingModel;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import static com.gitlab.lae.intellij.jump.editor.Editors.searchOffsets;
import static com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT;
import static java.util.stream.Collectors.toList;

public final class EditorsTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testSearchOffsetsReturnsOffsetsMatchingQuery() {
        myFixture.configureByText(PLAIN_TEXT, "HelloWorld");
        Editor editor = myFixture.getEditor();
        assertOrderedEquals(
                searchOffsets(editor, "o", true, 0, 10).toArray(),
                EditorOffset.of(editor, 4),
                EditorOffset.of(editor, 6)
        );
    }

    public void testSearchOffsetsSkipFoldRegions() {
        myFixture.configureByText(
                PLAIN_TEXT, "" +
                        "HelloWorld\n" +
                        "HelloWorld\n" +
                        "HelloWorld\n" +
                        "HelloWorld");

        Editor editor = myFixture.getEditor();
        FoldingModel foldingModel = editor.getFoldingModel();
        foldingModel.runBatchFoldingOperation(() -> {
            FoldRegion r1 = foldingModel.addFoldRegion(0, 20, "...");
            FoldRegion r2 = foldingModel.addFoldRegion(30, 40, "...");
            assertNotNull(r1);
            assertNotNull(r2);
            r1.setExpanded(false);
            r2.setExpanded(false);
        });
        assertOrderedEquals(
                searchOffsets(editor, "o", true, 0, 40).toArray(),
                EditorOffset.of(editor, 26),
                EditorOffset.of(editor, 28)
        );
    }

    public void testSearchOffsetsSortsByDistanceFromCaret() {
        String text = "ABAB          A";
        myFixture.configureByText(PLAIN_TEXT, text);
        Editor editor = myFixture.getEditor();
        editor.getCaretModel().getPrimaryCaret().moveToOffset(4);
        assertOrderedEquals(
                searchOffsets(
                        editor,
                        "a",
                        true,
                        0,
                        text.length()
                ).collect(toList()),
                EditorOffset.of(editor, 2),
                EditorOffset.of(editor, 0),
                EditorOffset.of(editor, 14)
        );
    }
}
