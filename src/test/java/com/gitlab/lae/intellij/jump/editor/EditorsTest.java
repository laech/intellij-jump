package com.gitlab.lae.intellij.jump.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.FoldingModel;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import static com.gitlab.lae.intellij.jump.editor.Editors.searchOffsets;
import static com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT;

public final class EditorsTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testSearchOffsetsReturnsOffsetsMatchingQuery() {
        myFixture.configureByText(PLAIN_TEXT, "HelloWorld");
        Editor editor = myFixture.getEditor();
        assertOrderedEquals(
                searchOffsets(editor, "o", true, 0, 10).toArray(),
                new int[]{4, 6});
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
                new int[]{26, 28});
    }
}
