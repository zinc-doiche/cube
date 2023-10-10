package com.github.doiche.object.status;

import net.kyori.adventure.text.format.TextColor;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public enum Rank {
    COMMON("일반", GRAY),
    RARE("레어", GREEN),
    EPIC("에픽", TextColor.fromHexString("#4ddce5")),
    UNIQUE("유니크", TextColor.fromHexString("#864dff")),
    LEGENDARY("레전더리", YELLOW),
    ANIMAL_GUARDIAN("동물 수호자", TextColor.fromHexString("#ff314e"))
    ;

    private final String kor;
    private final TextColor color;

    Rank(String kor, TextColor color) {
        this.kor = kor;
        this.color = color;
    }

    public String getKor() {
        return kor;
    }

    public TextColor getColor() {
        return color;
    }
}
