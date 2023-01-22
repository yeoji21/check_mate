package checkmate.notification.presentation.dto;

import checkmate.MapperTest;
import checkmate.notification.application.dto.request.NotificationDetailsCriteria;
import org.junit.jupiter.api.Test;

class NotificationDtoMapperTest extends MapperTest {
    private static final NotificationDtoMapper mapper = NotificationDtoMapper.INSTANCE;

    @Test
    void notificationDetailsCriteria() throws Exception{
        //given
        long cursorId = 1L;
        int size = 10;
        long userId = 2L;

        //when
        NotificationDetailsCriteria criteria = mapper.toCriteria(cursorId, size, userId);

        //then
        isEqualTo(criteria.cursorId(), cursorId);
        isEqualTo(criteria.userId(), userId);
        isEqualTo(criteria.size(), size);
    }
}