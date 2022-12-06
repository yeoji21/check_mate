package checkmate.exception;

import checkmate.ControllerTest;
import checkmate.config.WithMockAuthUser;
import checkmate.exception.code.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ErrorCodeDocumentationTest extends ControllerTest {
    @Test @WithMockAuthUser
    void errorCodeDocumentation() throws Exception{
        ResultActions result = mockMvc.perform(get("/error-code")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON));
        result.andExpect(status().isOk());
        result.andDo(document("에러 코드",
                codeResponseFields("code-response", beneathPath("errorCodes"),
                        attributes(key("title").value("에러코드")),
                        enumConvertFieldDescriptor(ErrorCode.values())
                )
        ));
    }

    private FieldDescriptor[] enumConvertFieldDescriptor(ErrorCode[] errorCodes) {
        return Arrays.stream(errorCodes)
                .sorted(Comparator.comparing((ErrorCode code) -> getDomain(code.getCode()))
                        .thenComparing((ErrorCode code) -> getNumber(code.getCode())))
                .map(enumType -> fieldWithPath(enumType.getCode()).description(enumType.getDetail()))
                .toArray(FieldDescriptor[]::new);
    }

    private String getDomain(String code) {
        return code.substring(0, code.indexOf("-"));
    }

    private String getNumber(String code) {
        return code.substring(code.indexOf("-") + 1);
    }

    private Snippet codeResponseFields(String type,
                                       PayloadSubsectionExtractor<?> errorCodes,
                                       Map<String, Object> attributes,
                                       FieldDescriptor... descriptors) {
        return new CodeResponseFieldsSnippet(type, errorCodes, Arrays.asList(descriptors), attributes,true);
    }
}
