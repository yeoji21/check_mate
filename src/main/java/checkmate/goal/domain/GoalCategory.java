package checkmate.goal.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GoalCategory {
    EXERCISE("운동"),
    LIFESTYLE("생활습관"),
    READING("독서"),
    LEARNING("학습"),
    HOBBIES("취미 생활"),
    ETC("기타"),
    ;

    private final String kor;
}
