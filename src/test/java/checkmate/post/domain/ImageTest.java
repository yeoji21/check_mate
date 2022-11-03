package checkmate.post.domain;

import checkmate.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImageTest {
    private Post post;

    @BeforeEach
    void setUp() {
        post = Post.builder().teamMate(TestEntityFactory.teamMate(1L, 1L)).text("post body text").build();
    }

    @Test
    void 이미지_엔티티_생성_테스트() throws Exception{
        //given
        Image image = Image.builder()
                .post(post)
                .storedName("stored name")
                .originalName("original name").build();
        //when

        //then
        assertThat(image.getPost().getText()).contains("post body text");
        assertThat(post.getImages()).hasSize(1);

        Image findImage = post.getImages().get(0);
        assertThat(findImage.getPost()).isEqualTo(post);
        assertThat(findImage.getOriginalName()).isEqualTo("original name");
        assertThat(findImage.getStoredName()).isEqualTo("stored name");
    }
}