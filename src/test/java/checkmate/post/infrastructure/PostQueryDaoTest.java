package checkmate.post.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import checkmate.RepositoryTest;
import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.post.application.dto.response.PostInfo;
import checkmate.post.domain.Image;
import checkmate.post.domain.Post;
import checkmate.user.domain.User;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PostQueryDaoTest extends RepositoryTest {

    @Test
    void 타임라인_게시글_조회_테스트() throws Exception {
        //given
        Goal goal = getGoal();
        Mate mate = getMate(goal);

        Post post1 = getPost(mate);
        Post post2 = getPost(mate);
        Post post3 = getPost(mate);

        post3.addLikes(1L);
        post3.addLikes(2L);
        em.persist(getImage(post2));

        em.flush();
        em.clear();

        //when
        List<PostInfo> postInfos = postQueryDao.findTimelinePosts(goal.getId(), LocalDate.now());

        //then
        assertThat(postInfos.size()).isEqualTo(3);
        assertThat(postInfos.get(0).getLikedUserIds().size()).isEqualTo(2);
        assertThat(postInfos.get(1).getImageUrls().size()).isEqualTo(1);
    }

    private Goal getGoal() {
        Goal goal = TestEntityFactory.goal(null, "testGoal");
        em.persist(goal);
        return goal;
    }

    private Image getImage(Post post2) {
        return Image.builder()
            .post(post2)
            .originalName("filename")
            .storedName("filename")
            .build();
    }

    private Post getPost(Mate mate) {
        Post post = TestEntityFactory.post(mate);
        em.persist(post);
        return post;
    }

    private Mate getMate(Goal goal) {
        User user = TestEntityFactory.user(null, "tester");
        em.persist(user);
        Mate mate = goal.createMate(user);
        em.persist(mate);
        return mate;
    }
}