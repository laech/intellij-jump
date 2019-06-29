package com.gitlab.lae.intellij.jump.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import static com.gitlab.lae.intellij.jump.editor.Editors.searchOffsets;
import static com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT;

public final class EditorsTest extends LightPlatformCodeInsightFixtureTestCase {

    public void testSearchOffsetsReturnsOffsetsMatchingQuery() {
        myFixture.configureByText(PLAIN_TEXT, "HelloWorld");
        Editor editor = myFixture.getEditor();
        assertOrderedEquals(
                new int[]{4, 6},
                searchOffsets(editor, "o", true, 0, 10).toArray());

    }

}
