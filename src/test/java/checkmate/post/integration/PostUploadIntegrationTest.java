package checkmate.post.integration;

import checkmate.IntegrationTest;
import checkmate.TestEntityFactory;
import checkmate.config.WithMockAuthUser;
import checkmate.goal.domain.Goal;
import checkmate.goal.domain.TeamMate;
import checkmate.user.domain.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.fileUpload;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
public class PostUploadIntegrationTest extends IntegrationTest {

    @Test @WithMockAuthUser
    void 목표_인증_테스트() throws Exception{
        //given
        MockMultipartFile firstFile = getMockMultipartFile("imageFile1");
        MockMultipartFile secondFile = getMockMultipartFile("imageFile2");

        Goal goal = TestEntityFactory.goal(null, "testGoal");
        entityManager.persist(goal);

        User user = TestEntityFactory.user(null, "tester");
        entityManager.persist(user);
        TeamMate teamMate = TestEntityFactory.teamMate(null, user.getId());
        goal.addTeamMate(teamMate);
        entityManager.persist(teamMate);
        int beforeWorkingDays = teamMate.getWorkingDays();

        entityManager.flush();
        entityManager.clear();

        //when
        mockMvc.perform(fileUpload("/post")
                        .file(firstFile).file(secondFile)
                        .param("teamMateId", String.valueOf(teamMate.getId()))
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

        TeamMate findTeamMate = entityManager.find(TeamMate.class, teamMate.getId());
        assertThat(findTeamMate.getWorkingDays()).isGreaterThan(beforeWorkingDays);
    }

    private MockMultipartFile getMockMultipartFile(String filename) {
        return new MockMultipartFile(filename, "someImage.jpeg",
                "image/jpeg", "test image file".getBytes(StandardCharsets.UTF_8));
    }
}
