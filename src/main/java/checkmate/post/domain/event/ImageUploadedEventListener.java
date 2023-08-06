package checkmate.post.domain.event;

import checkmate.post.application.FileManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Service
public class ImageUploadedEventListener {

    private final FileManageService fileManageService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveImages(FileUploadedEvent event) {
        fileManageService.upload(event.getPost(), event.getImageName(), event.getInputStream());
    }
}
