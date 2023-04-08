package checkmate.mate.domain;

import java.util.List;
import java.util.Optional;

public interface MateRepository {
    Optional<Mate> findById(long mateId);

    Optional<Mate> findMateWithGoal(long mateId);

    Optional<Mate> findMateWithGoal(long goalId, long userId);

    // TODO: 2023/04/08 리턴 타입을 void로 바꾸고 따로 조회 메소드 생성 고려
    // 수정 후 영속성 컨텍스트 주의
    List<Mate> updateYesterdaySkippedMates();

    void updateLimitOveredMates(List<Mate> limitOveredMates);

    List<Mate> findMatesInGoals(List<Long> goalIds);

    Mate save(Mate mate);
}
