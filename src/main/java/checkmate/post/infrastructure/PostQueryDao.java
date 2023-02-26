package checkmate.post.infrastructure;

import checkmate.post.application.dto.response.PostInfo;
import checkmate.post.application.dto.response.QPostInfo;
import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static checkmate.mate.domain.QMate.mate;
import static checkmate.post.domain.QImage.image;
import static checkmate.post.domain.QLikes.likes;
import static checkmate.post.domain.QPost.post;
import static checkmate.user.domain.QUser.user;
import static com.querydsl.core.group.GroupBy.list;

@RequiredArgsConstructor
@Repository
public class PostQueryDao {
    private final JPAQueryFactory queryFactory;

    public List<PostInfo> findTimelinePosts(long goalId, LocalDate date) {
        return queryFactory
                .from(mate)
                .join(user).on(mate.userId.eq(user.id))
                .leftJoin(post).on(mate.eq(post.mate))
                .leftJoin(image).on(image.post.eq(post))
                .leftJoin(likes).on(likes.post.eq(post))
                .where(mate.goal.id.eq(goalId),
                        post.uploadedDate.eq(date))
                .orderBy(post.createdDateTime.desc())
                .transform(GroupBy.groupBy(post).list(new QPostInfo(post.id, mate.id, user.nickname,
                        post.createdDateTime, list(image.storedName), post.content, list(likes.userId))));
    }
}
