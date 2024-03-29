package checkmate.post.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import checkmate.ControllerTest;
import checkmate.config.WithMockAuthUser;
import checkmate.post.application.dto.request.PostCreateCommand;
import checkmate.post.application.dto.response.PostCreateResult;
import checkmate.post.application.dto.response.PostInfo;
import checkmate.post.application.dto.response.PostInfoResult;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;
import org.springframework.restdocs.request.RequestPartsSnippet;

class PostControllerTest extends ControllerTest {

    @WithMockAuthUser
    @Test
    @DisplayName("목표 인증 업로드 API")
    void upload() throws Exception {
        MockMultipartFile firstFile = getMockMultipartFile("imageFile1");
        MockMultipartFile secondFile = getMockMultipartFile("imageFile2");
        PostCreateCommand command = getPostUploadCommand(firstFile, secondFile);
        PostCreateResult result = new PostCreateResult(1L);

        given(postDtoMapper.toCommand(any(), anyLong())).willReturn(command);
        given(postCommandService.create(any(PostCreateCommand.class))).willReturn(result);

        mockMvc.perform(multipart("/posts")
                .file(firstFile).file(secondFile)
                .param("mateId", "1")
                .param("content", "posting content data")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(result)))
            .andDo(document("post-create",
                uploadRequestPartsSnippet(),
                uploadRequestParametersSnippet(),
                uploadResponseFieldsSnippet()
            ));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("목표의 날짜별 게시글 조회 API")
    void findPostInfoByDate() throws Exception {
        PostInfoResult result = new PostInfoResult("goalTitle", getPostInfoList());
        given(postQueryService.findPostByGoalIdAndDate(any(Long.class),
            any(String.class))).willReturn(result);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/goals/{goalId}/posts/{date}",
                    1L, "20220315")
                .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(result)))
            .andDo(document("post-find",
                findPostPathParametersSnippet(),
                findPostResponseFieldsSnippet()
            ));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("인증글 좋아요 API")
    void like() throws Exception {
        mockMvc.perform(post("/goals/{goalId}/posts/{postId}/like", 1L, 1L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("post-like",
                postIdPathParametersSnippet()
            ));

        Mockito.verify(postCommandService).like(any(Long.class), any(Long.class));
    }

    @WithMockAuthUser
    @Test
    @DisplayName("인증글 좋아요 취소 API")
    void unlike() throws Exception {
        mockMvc.perform(delete("/goals/{goalId}/posts/{postId}/unlike", 1L, 1L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(document("post-unlike",
                postIdPathParametersSnippet()
            ));

        Mockito.verify(postCommandService).unlike(any(Long.class), any(Long.class));
    }

    private PathParametersSnippet findPostPathParametersSnippet() {
        return pathParameters(
            parameterWithName("goalId").description("목표 ID"),
            parameterWithName("date").description("날짜 (ex. 20220217)")
        );
    }

    private PathParametersSnippet postIdPathParametersSnippet() {
        return pathParameters(parameterWithName("goalId").description("목표 ID"),
            parameterWithName("postId").description("포스트 ID"));
    }

    private ResponseFieldsSnippet findPostResponseFieldsSnippet() {
        return responseFields(
            fieldWithPath("posts[].postId").type(JsonFieldType.NUMBER).description("포스트 ID"),
            fieldWithPath("posts[].mateId").type(JsonFieldType.NUMBER).description("업로더의 팀원 ID"),
            fieldWithPath("posts[].uploaderNickname").type(JsonFieldType.STRING)
                .description("업로더의 닉네임"),
            fieldWithPath("posts[].uploadAt").type(JsonFieldType.STRING).description("업로드 시간"),
            fieldWithPath("posts[].imageUrls").type(JsonFieldType.ARRAY)
                .description("이미지 파일 접근 주소"),
            fieldWithPath("posts[].content").type(JsonFieldType.STRING).description("텍스트 인증 내용"),
            fieldWithPath("posts[].likedUserIds").type(JsonFieldType.ARRAY)
                .description("좋아요 누른 유저 ID"),
            fieldWithPath("goalTitle").type(JsonFieldType.STRING).description("해당 목표의 이름")
        );
    }

    private ResponseFieldsSnippet uploadResponseFieldsSnippet() {
        return responseFields(
            fieldWithPath("postId").type(JsonFieldType.NUMBER).description("생성된 포스트 ID"));
    }

    private RequestParametersSnippet uploadRequestParametersSnippet() {
        return requestParameters(
            parameterWithName("mateId").description("팀원 ID"),
            parameterWithName("content").description("목표 인증 본문"),
            parameterWithName("_csrf").description("csrf 토큰").ignored()
        );
    }

    private RequestPartsSnippet uploadRequestPartsSnippet() {
        return requestParts(
            partWithName("imageFile1").description("저장하고자 하는 이미지 1"),
            partWithName("imageFile2").description("저장하고자 하는 이미지 2")
        );
    }

    private PostCreateCommand getPostUploadCommand(MockMultipartFile firstFile,
        MockMultipartFile secondFile) {
        return PostCreateCommand.builder()
            .userId(1L)
            .mateId(2L)
            .content("~~~")
            .images(List.of(firstFile, secondFile))
            .build();
    }

    private List<PostInfo> getPostInfoList() {
        return List.of(PostInfo.builder()
                .postId(1L)
                .mateId(1L)
                .uploaderNickname("uploader1")
                .uploadAt(LocalDateTime.now())
                .content("content")
                .imageUrls(List.of("url1", "url2"))
                .likedUserIds(List.of(1L, 2L))
                .build(),
            PostInfo.builder()
                .postId(2L)
                .mateId(3L)
                .uploaderNickname("uploader1")
                .uploadAt(LocalDateTime.now())
                .content("content")
                .imageUrls(List.of("url3", "url4"))
                .likedUserIds(List.of(2L, 3L))
                .build()
        );
    }

    private MockMultipartFile getMockMultipartFile(String filename) {
        return new MockMultipartFile(filename, "someImage.jpeg",
            "image/jpeg", "test image file".getBytes(StandardCharsets.UTF_8));
    }

}