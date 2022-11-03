package checkmate;

import checkmate.config.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ContextConfiguration(classes = WebSecurityConfig.class)
@ExtendWith(RestDocumentationExtension.class)
public abstract class ExampleSimpleRestDocsTest {
    private RestDocumentationContextProvider restDocument;
    protected ObjectMapper objectMapper = new ObjectMapper();
    private FilterChainProxy springSecurityFilterChain;

    @BeforeEach
    void setUp(RestDocumentationContextProvider contextProvider) {

        SecurityFilterChain chain = new SecurityFilterChain() {
            @Override
            public boolean matches(HttpServletRequest request) {
                return true;
            }

            @Override
            public List<Filter> getFilters() {
                return Collections.emptyList();
            }
        };
        springSecurityFilterChain = new FilterChainProxy(chain);
        restDocument = contextProvider;
    }

    protected MockMvc mockMvc(Object controller) {
        return createMockMvc(controller);
    }

    private MockMvc createMockMvc(Object controller) {
        return MockMvcBuilders
                .standaloneSetup(controller)
                .apply(SecurityMockMvcConfigurers.springSecurity(springSecurityFilterChain))
                .apply(documentationConfiguration(restDocument))
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();
    }
}
