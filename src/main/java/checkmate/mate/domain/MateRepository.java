package checkmate.mate.domain;

import java.util.List;
import java.util.Optional;

public interface MateRepository {
    Optional<Mate> findById(long mateId);

    Optional<Mate> findWithGoal(long mateId);

    List<Mate> findByGoalIds(List<Long> goalIds);

    Optional<Mate> findWithGoal(long goalId, long userId);

    List<Mate> findSuccessMates(long userId);

    // TODO: 2023/04/08 리턴 타입을 void로 바꾸고 따로 조회 메소드 생성 고려
    // 수정 후 영속성 컨텍스트 주의
    List<Mate> updateYesterdaySkippedMates();

    void updateLimitOveredMates(List<Mate> limitOveredMates);

    Mate save(Mate mate);
}
