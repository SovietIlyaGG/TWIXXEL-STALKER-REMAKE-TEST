package com.thestalker.entity;

public enum StalkerKind {
    BIPEDAL,
    QUADRUPEDAL,
    TORMENTOR;

    public static StalkerKind fromImageIndex(int index) {
        return switch (index) {
            case 1, 2, 3 -> BIPEDAL;
            case 5 -> QUADRUPEDAL;
            case 0, 4, 6 -> TORMENTOR;
            default -> BIPEDAL;
        };
    }

    public boolean canDash() {
        return this == BIPEDAL || this == TORMENTOR;
    }

    public boolean canOpenDoors() {
        return this == BIPEDAL || this == TORMENTOR;
    }

    public boolean canBreakBarricades() {
        return this == BIPEDAL || this == TORMENTOR;
    }

    public boolean canPeekFromTrees() {
        return this == BIPEDAL || this == TORMENTOR;
    }

    public boolean canPeekFromHoles() {
        return this == QUADRUPEDAL;
    }

    public boolean canFreeze() {
        return this == BIPEDAL || this == TORMENTOR;
    }

    public boolean canCrawl() {
        return this == QUADRUPEDAL;
    }
}
