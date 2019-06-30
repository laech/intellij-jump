package com.gitlab.lae.intellij.jump;

import com.gitlab.lae.intellij.jump.editor.EditorOffset;
import com.gitlab.lae.intellij.jump.tree.TreeNode;
import com.google.auto.value.AutoValue;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorFontType;

import javax.swing.*;
import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

final class Highlighter extends JComponent {

    private static final int markerBorderRound = 6;
    private static final int markerInnerRound = 4;

    @SuppressWarnings("UseJBColor")
    private static final Color markerForeground = Color.BLACK;

    @SuppressWarnings("UseJBColor")
    private static final Color markerBackground = new Color(0xffe9a2);

    private static final Color markerBorder = markerBackground.darker();

    private final Editor editor;

    private final List<Entry> entries = new ArrayList<>();

    Highlighter(Editor editor) {
        this.editor = requireNonNull(editor);
    }

    void setTree(TreeNode<String, EditorOffset> value) {
        entries.clear();
        value.forEach((path, editorOffset) -> {
            if (editor == editorOffset.editor()) {
                entries.add(Entry.create(
                        path.keys().collect(joining()),
                        editorOffset.editor(),
                        editorOffset.offset()));
            }
        });
        // Draw from the smallest offset, so that if there are
        // overlaps, the head of the next offset will be drawn
        // on top, then the user knows which key to press for that.
        entries.sort(comparing(Entry::offset));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D gg = (Graphics2D) g.create();
        try {
            paintMarkers(gg);
        } finally {
            gg.dispose();
        }
    }

    private void paintMarkers(Graphics2D g) {
        Font font = editor.getColorsScheme().getFont(EditorFontType.PLAIN);
        JComponent contentComponent = editor.getContentComponent();
        FontMetrics fontMetrics = contentComponent.getFontMetrics(font);
        g.setFont(font);
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        int lineHeight = editor.getLineHeight();
        entries.forEach(entry -> {
            String label = entry.label();
            Editor editor = entry.editor();
            int offset = entry.offset();
            LineMetrics lineMetrics = fontMetrics.getLineMetrics(label, g);
            Rectangle2D fontRect = fontMetrics.getStringBounds(label, g);
            Point loc = editor.offsetToXY(offset);

            g.setColor(markerBackground);
            g.fillRoundRect(
                    loc.x + contentComponent.getX(),
                    loc.y + contentComponent.getY(),
                    (int) fontRect.getWidth(),
                    lineHeight,
                    markerInnerRound,
                    markerInnerRound);

            g.setColor(markerBorder);
            g.drawRoundRect(
                    loc.x + contentComponent.getX() - 1,
                    loc.y + contentComponent.getY(),
                    (int) (fontRect.getWidth() + 1),
                    lineHeight,
                    markerBorderRound,
                    markerBorderRound);

            g.setColor(markerForeground);
            g.drawString(
                    label,
                    loc.x + contentComponent.getX(),
                    (loc.y + contentComponent.getY() +
                            lineMetrics.getAscent() + lineMetrics.getLeading() +
                            (lineHeight - lineMetrics.getHeight()) / 2));
        });
    }

    @AutoValue
    static abstract class Entry {
        Entry() {
        }

        abstract String label();

        abstract Editor editor();

        abstract int offset();

        static Entry create(String label, Editor editor, int offset) {
            return new AutoValue_Highlighter_Entry(label, editor, offset);
        }
    }
}
