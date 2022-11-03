package checkmate.post.infrastructure;

import checkmate.post.application.dto.response.PostInfo;
import checkmate.post.application.dto.response.QPostInfo;
import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

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
                .from(post)
                .join(user).on(user.id.eq(post.teamMate.userId))
                .leftJoin(post.images.images, image)
                .leftJoin(post.likes, likes)
                .where(post.teamMate.goal.id.eq(goalId),
                        post.uploadedDate.eq(date))
                .orderBy(post.createdDate.desc())
                .transform(GroupBy.groupBy(post).list(new QPostInfo(post.id, post.teamMate.id, user.nickname,
                        post.createdDate, list(image.storedName), post.text, list(likes.userId))));
    }
}
