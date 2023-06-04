package checkmate.mate.application;

import static checkmate.exception.code.ErrorCode.MATE_NOT_FOUND;

import checkmate.exception.NotFoundException;
import checkmate.mate.application.dto.response.MateScheduleInfo;
import checkmate.mate.application.dto.response.SpecifiedGoalDetailInfo;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.infra.MateQueryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MateQueryService {

    private final MateQueryDao mateQueryDao;
    private final MateRepository mateRepository;

    @Transactional(readOnly = true)
    public MateScheduleInfo findCalenderInfo(long teamMateId) {
        return mateQueryDao.findScheduleInfo(teamMateId)
            .orElseThrow(IllegalArgumentException::new);
    }

    @Transactional(readOnly = true)
    public SpecifiedGoalDetailInfo findSpecifiedGoalDetailInfo(long goalId, long userId) {
        return new SpecifiedGoalDetailInfo(
            findMate(goalId, userId),
            mateQueryDao.findUploadedDates(findMate(goalId, userId).getId()),
            mateQueryDao.findUploadInfo(goalId)
        );
    }

    private Mate findMate(long goalId, long userId) {
        return mateRepository.findWithGoal(goalId, userId)
            .orElseThrow(() -> new NotFoundException(MATE_NOT_FOUND));
    }
}
