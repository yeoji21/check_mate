package checkmate.post.domain;

import checkmate.TestEntityFactory;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImageTest {
    @Test
    void 이미지_엔티티_생성_테스트() throws Exception {
        //given
        Goal goal = TestEntityFactory.goal(1L, "test");
        Mate mate = goal.join(TestEntityFactory.user(1L, "user"));
        Post post = TestEntityFactory.post(mate);
        Image image = Image.builder()
                .post(post)
                .storedName("stored name")
                .originalName("original name").build();
        //when

        //then
        assertThat(image.getPost().getContent()).isNotNull();
        assertThat(post.getImages()).hasSize(1);

        Image findImage = post.getImages().get(0);
        assertThat(findImage.getPost()).isEqualTo(post);
        assertThat(findImage.getOriginalName()).isEqualTo("original name");
        assertThat(findImage.getStoredName()).isEqualTo("stored name");
    }
}