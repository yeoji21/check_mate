package checkmate.post.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.fileUpload;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import checkmate.IntegrationTest;
import checkmate.TestEntityFactory;
import checkmate.config.WithMockAuthUser;
import checkmate.goal.domain.Goal;
import checkmate.mate.domain.Mate;
import checkmate.user.domain.User;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

@Disabled
public class PostUploadIntegrationTest extends IntegrationTest {

    @Test
    @WithMockAuthUser
    void 목표_인증_테스트() throws Exception {
        //given
        MockMultipartFile firstFile = getMockMultipartFile("imageFile1");
        MockMultipartFile secondFile = getMockMultipartFile("imageFile2");

        Goal goal = TestEntityFactory.goal(null, "testGoal");
        entityManager.persist(goal);

        User user = TestEntityFactory.user(null, "tester");
        entityManager.persist(user);
        Mate mate = goal.createMate(user);
        entityManager.persist(mate);
        int beforeWorkingDays = mate.getWorkingDays();

        entityManager.flush();
        entityManager.clear();

        //when
        mockMvc.perform(fileUpload("/post")
                .file(firstFile).file(secondFile)
                .param("mateId", String.valueOf(mate.getId()))
                .param("text", "test")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(1L)))
            .andDo(print());

        entityManager.flush();
        entityManager.clear();

        //then
//        TestTransaction.flagForCommit();
//        TestTransaction.end();

        Mate findMate = entityManager.find(Mate.class, mate.getId());
        assertThat(findMate.getWorkingDays()).isGreaterThan(beforeWorkingDays);
    }

    private MockMultipartFile getMockMultipartFile(String filename) {
        return new MockMultipartFile(filename, "someImage.jpeg",
            "image/jpeg", "test image file".getBytes(StandardCharsets.UTF_8));
    }
}
