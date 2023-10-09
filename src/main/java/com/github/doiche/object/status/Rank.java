package com.github.doiche.object.status;

public enum Rank {
    COMMON("일반"),
    RARE("레어"),
    EPIC("에픽"),
    UNIQUE("유니크"),
    LEGENDARY("레전더리"),
    ANIMAL_GUARDIAN("동물 수호자")
    ;

    private final String kor;

    Rank(String kor) {
        this.kor = kor;
    }

    public String getKor() {
        return kor;
    }
}
