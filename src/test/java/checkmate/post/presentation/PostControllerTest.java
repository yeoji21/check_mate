package checkmate.post.presentation;

import checkmate.ControllerTest;
import checkmate.config.WithMockAuthUser;
import checkmate.post.application.dto.request.PostUploadCommand;
import checkmate.post.application.dto.response.PostInfo;
import checkmate.post.application.dto.response.PostInfoListResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;
import org.springframework.restdocs.request.RequestPartsSnippet;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerTest extends ControllerTest {
    @WithMockAuthUser
    @Test
    void 목표인증_테스트() throws Exception{
        MockMultipartFile firstFile = getMockMultipartFile("imageFile1");
        MockMultipartFile secondFile = getMockMultipartFile("imageFile2");
        PostUploadCommand command = PostUploadCommand.builder()
                .teamMateId(1L)
                .text("~~~")
                .images(List.of(new MockMultipartFile("file1", new byte[10]),
                                new MockMultipartFile("file1", new byte[10])))
                .build();

        given(postDtoMapper.toUploadCommand(any())).willReturn(command);
        given(postCommandService.upload(any(PostUploadCommand.class))).willReturn(1L);

        mockMvc.perform(fileUpload("/post")
                        .file(firstFile).file(secondFile)
                        .param("teamMateId", "1")
                        .param("text", "posting text data")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(1L)))
                .andDo(document("save-post",
                        getRequestParts(),
                        getRequestParameters()
                ));
    }

    @WithMockAuthUser
    @Test
    void 목표의_날짜별_게시글_조회_테스트() throws Exception{
        PostInfoListResult result = new PostInfoListResult("goalTitle", getPostInquiryResponses());

        given(postQueryService.findPostByGoalIdAndDate(any(Long.class), any(String.class))).willReturn(result);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/post?goalId=1&date=20210412")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(result)))
                .andDo(document("find-posts",
                        requestParameters(
                                parameterWithName("goalId").description("해당 목표의 goalId"),
                                parameterWithName("date").description("날짜 (예. 20220217)")
                        ),
                        getFindPostsResponseFields()
                ));
    }

    @WithMockAuthUser
    @Test
    void 인증글_좋아요_테스트() throws Exception{
        mockMvc.perform(post("/post/{postId}/like", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("like-post",
                        pathParameters(parameterWithName("postId").description("postId"))
                ));

        Mockito.verify(postCommandService).like(any(Long.class), any(Long.class));
    }

    @WithMockAuthUser
    @Test
    void 인증글_좋아요_취소_테스트() throws Exception{
        mockMvc.perform(delete("/post/{postId}/unlike", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("unlike-post",
                        pathParameters(parameterWithName("postId").description("postId"))
                ));
    }

    private RequestParametersSnippet getRequestParameters() {
        return requestParameters(
//                parameterWithName("goalId").description("목표의 id"),
                parameterWithName("teamMateId").description("목표를 인증하는 팀 메이트의 id"),
                parameterWithName("text").description("목표 인증 게시글 본문"),
                parameterWithName("_csrf").description("csrf 토큰")
        );
    }

    private RequestPartsSnippet getRequestParts() {
        return requestParts(
                partWithName("imageFile1").description("저장하고자 하는 이미지"),
                partWithName("imageFile2").description("저장하고자 하는 이미지")
        );
    }

    private List<PostInfo> getPostInquiryResponses() {
        return List.of(new PostInfo(1L, 1L, "uploader1", LocalDateTime.now(), List.of("url1", "url2"), "text", List.of(1L, 2L)),
                new PostInfo(2L, 2L, "uploader2", LocalDateTime.now(), List.of("url3", "url4"), "text", List.of(2L, 3L)));
    }

    private ResponseFieldsSnippet getFindPostsResponseFields() {
        return responseFields(
                fieldWithPath("posts[].postId").type(JsonFieldType.NUMBER).description("postId"),
                fieldWithPath("posts[].teamMateId").type(JsonFieldType.NUMBER).description("업로더의 teamMateId"),
                fieldWithPath("posts[].uploaderNickname").type(JsonFieldType.STRING).description("업로더의 닉네임"),
                fieldWithPath("posts[].uploadAt").type(JsonFieldType.STRING).description("업로드 시간"),
                fieldWithPath("posts[].imageUrls").type(JsonFieldType.ARRAY).description("이미지 파일 접근 주소"),
                fieldWithPath("posts[].text").type(JsonFieldType.STRING).description("글로 인증 내용"),
                fieldWithPath("posts[].likedUserIds").type(JsonFieldType.ARRAY).description("좋아요 누른 유저들"),
                fieldWithPath("goalTitle").type(JsonFieldType.STRING).description("해당 목표의 타이틀")
        );
    }

    private MockMultipartFile getMockMultipartFile(String filename) {
        return new MockMultipartFile(filename, "someImage.jpeg",
                "image/jpeg", "test image file".getBytes(StandardCharsets.UTF_8));
    }

}