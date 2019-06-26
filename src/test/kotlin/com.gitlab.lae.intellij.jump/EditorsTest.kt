package com.gitlab.lae.intellij.jump

import com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class EditorsTest : LightPlatformCodeInsightFixtureTestCase() {

    fun `test searchOffsets returns offsets matching query`() {
        myFixture.configureByText(PLAIN_TEXT, "HelloWorld")
        val editor = myFixture.editor
        val actual = editor.searchOffsets('o', true, 0, 10).toList()
        val expected = listOf(4, 6)
        assertEquals(expected, actual)
    }
}
