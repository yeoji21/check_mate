package checkmate.mate.application;

import checkmate.exception.NotFoundException;
import checkmate.mate.application.dto.response.MateScheduleInfo;
import checkmate.mate.application.dto.response.SpecifiedGoalDetailInfo;
import checkmate.mate.domain.Mate;
import checkmate.mate.domain.MateRepository;
import checkmate.mate.infra.MateQueryDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static checkmate.exception.code.ErrorCode.MATE_NOT_FOUND;

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

    // TODO: 2023/05/01 조회용 쿼리로 개선
    @Transactional(readOnly = true)
    public SpecifiedGoalDetailInfo findSpecifiedGoalDetailInfo(long goalId, long userId) {
        Mate mate = mateRepository.findWithGoal(goalId, userId)
                .orElseThrow(() -> new NotFoundException(MATE_NOT_FOUND));

        return new SpecifiedGoalDetailInfo(mate,
                mateQueryDao.findUploadedDates(mate.getId()),
                mateQueryDao.findUploadInfo(goalId));
    }
}
