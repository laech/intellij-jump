package com.gitlab.lae.intellij.jump;

import com.gitlab.lae.intellij.jump.editor.EditorOffset;
import com.gitlab.lae.intellij.jump.tree.Tree;
import com.gitlab.lae.intellij.jump.tree.TreeLeaf;
import com.gitlab.lae.intellij.jump.tree.TreeNode;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.editor.Editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.gitlab.lae.intellij.jump.editor.Editors.getSingleStrokeEditorEscapeKeys;
import static com.gitlab.lae.intellij.jump.editor.Editors.searchVisibleOffsets;
import static com.gitlab.lae.intellij.jump.tree.Tree.of;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static javax.swing.KeyStroke.getKeyStrokeForEvent;

public final class Model {

    static final String markerChars = "fjdkslaowierqghpnvzxcmbtyu";

    private static final Map<Character, Integer> markerIndices = IntStream
            .range(0, markerChars.length()).boxed()
            .collect(toMap(markerChars::charAt, identity()));

    private TreeNode<EditorOffset> tree = TreeNode.empty();
    private final Set<KeyStroke> editorEscapeKeyStrokes = getSingleStrokeEditorEscapeKeys(ActionManager.getInstance());
    private final Map<Editor, KeyListener[]> existingKeyListeners = new HashMap<>();
    private final Map<Editor, Highlighter> editorHighlighters;

    private final KeyListener inputListener = new KeyAdapter() {

        @Override
        public void keyTyped(KeyEvent e) {
            super.keyTyped(e);
            Set<Editor> editors = editorHighlighters.keySet();
            String query = String.valueOf(e.getKeyChar());
            Stream<EditorOffset> offsets = searchVisibleOffsets(editors, query, true);
            tree = of(offsets, markerChars.length());
            editors.stream()
                    .map(Editor::getContentComponent)
                    .forEach(it -> {
                        it.removeKeyListener(this);
                        it.addKeyListener(jumpListener);
                    });
            editorHighlighters.values().forEach(it -> it.setTree(tree));
        }
    };

    private final KeyListener jumpListener = new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
            if (isCancel(e)) {
                detach();
                return;
            }

            // TODO no jump if there is no option
            // TODO jump directly if there is one option

            Integer index = markerIndices.get(e.getKeyChar());
            if (index == null) {
                return;
            }
            Tree<EditorOffset> node = tree.nodes().get(index);
            if (node instanceof TreeNode<?>) {
                tree = (TreeNode<EditorOffset>) node;
                editorHighlighters.values().forEach(it -> it.setTree(tree));
            } else {
                EditorOffset editorOffset = ((TreeLeaf<EditorOffset>) node).value();
                Editor editor = editorOffset.editor();
                int offset = editorOffset.offset();
                Window window = SwingUtilities.getWindowAncestor(editor.getContentComponent());
                if (window != null) {
                    window.toFront();
                }
                editor.getContentComponent().requestFocusInWindow();
                editor.getCaretModel().moveToOffset(offset);
                detach();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (isCancel(e)) {
                detach();
            }
        }

        private boolean isCancel(KeyEvent e) {
            return e.getKeyCode() == VK_ESCAPE ||
                    editorEscapeKeyStrokes.contains(getKeyStrokeForEvent(e));
        }
    };

    Model(Collection<Editor> editors) {
        editorHighlighters = editors.stream().collect(
                toMap(identity(), Highlighter::new));
    }

    void attach() {
        editorHighlighters.forEach((editor, highlighter) -> {
            JComponent contentComponent = editor.getContentComponent();
            KeyListener[] listeners = contentComponent.getKeyListeners();
            existingKeyListeners.put(editor, listeners);
            for (KeyListener listener : listeners) {
                contentComponent.removeKeyListener(listener);
            }
            contentComponent.addKeyListener(inputListener);

            Rectangle area = editor.getScrollingModel().getVisibleArea();
            highlighter.setBounds(area.x, area.y, area.width, area.height);
            contentComponent.add(highlighter);
            contentComponent.repaint();
        });
    }


    private void detach() {
        editorHighlighters.forEach((editor, highlighter) -> {
            JComponent contentComponent = editor.getContentComponent();
            contentComponent.removeKeyListener(inputListener);
            contentComponent.removeKeyListener(jumpListener);
            contentComponent.remove(highlighter);
            for (KeyListener listener : existingKeyListeners.get(editor)) {
                contentComponent.addKeyListener(listener);
            }
            contentComponent.repaint();
        });
        existingKeyListeners.clear();
        editorHighlighters.clear();
    }

}
