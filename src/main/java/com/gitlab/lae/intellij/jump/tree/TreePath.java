package com.gitlab.lae.intellij.jump.tree;

import com.google.auto.value.AutoValue;

import java.util.stream.IntStream;

public abstract class TreePath {
    private TreePath() {
    }

    private static final TreePath EMPTY = new TreePath() {
        @Override
        public IntStream values() {
            return IntStream.empty();
        }

        @Override
        public String toString() {
            return "TreePath.empty()";
        }
    };

    static TreePath empty() {
        return EMPTY;
    }

    TreePath append(int value) {
        return SubPath.of(this, value);
    }

    public abstract IntStream values();

    @AutoValue
    static abstract class SubPath extends TreePath {
        SubPath() {
        }

        abstract TreePath parent();

        abstract int value();

        static SubPath of(TreePath parent, int value) {
            return new AutoValue_TreePath_SubPath(parent, value);
        }

        @Override
        public IntStream values() {
            return IntStream.concat(
                    parent().values(),
                    IntStream.of(value()));
        }
    }
}
