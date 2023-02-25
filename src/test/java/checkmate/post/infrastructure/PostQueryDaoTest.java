package checkmate.post.infrastructure;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.post.application.dto.response.PostInfo;
import checkmate.post.domain.Image;
import checkmate.post.domain.Likes;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostQueryDaoTest extends RepositoryTest {

    @Test
    void 타임라인_게시글_조회_테스트() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        User user = TestEntityFactory.user(null, "tester");
        em.persist(user);
        Mate mate = goal.join(user);
        em.persist(mate);

        Post post1 = TestEntityFactory.post(mate);
        Post post2 = TestEntityFactory.post(mate);
        Post post3 = TestEntityFactory.post(mate);
        em.persist(post1);
        em.persist(post2);
        em.persist(post3);

        post3.addLikes(new Likes(1L));
        post3.addLikes(new Likes(2L));

        em.persist(Image.builder()
                .post(post2)
                .originalName("filename")
                .storedName("filename")
                .build());

        em.flush();
        em.clear();

        //when
        List<PostInfo> postInfos = postQueryDao.findTimelinePosts(goal.getId(), LocalDate.now());

        //then
        assertThat(postInfos.size()).isEqualTo(3);
        assertThat(postInfos.get(0).getLikedUserIds().size()).isEqualTo(2);
        assertThat(postInfos.get(1).getImageUrls().size()).isEqualTo(1);
    }
}