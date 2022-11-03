package checkmate;

import checkmate.config.WebSecurityConfig;
import checkmate.config.WithMockAuthUser;
import checkmate.goal.domain.GoalCategory;
import checkmate.goal.presentation.dto.request.GoalCreateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;


@Import({WebSecurityConfig.class})
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Transactional
@Disabled
public class GoalIntegrationTest {
    @LocalServerPort private int port;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.defaultParser = Parser.JSON;
    }

    @Test @WithMockAuthUser
    void test() throws Exception{
        //given

        //when
        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
        .when()
                .get("/test")
        .then()
                .log().body()
                .statusCode(HttpStatus.OK.value());

        //then

    }

    @Test
    void 목표저장_통합테스트() throws JsonProcessingException {
        GoalCreateDto request = GoalCreateDto.builder()
                .category(GoalCategory.학습)
                .title("자바의 정석 스터디")
                .startDate(LocalDate.now().minusDays(20))
                .endDate(LocalDate.now().plusDays(10))
                .weekDays("월수금")
                .build();

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
        .when()
                .post("/goal")
        .then()
                .log().body()
                .statusCode(HttpStatus.OK.value());


//        assertThat(response.getTitle()).isEqualTo(request.getTitle());
//        assertThat(response.getGoalMethod()).isEqualTo(request.getGoalMethod());

//        List<TeamMate> teamMates = goalRepository.findById(response.getId()).orElseThrow().getTeamMates();
//        assertThat(teamMates).hasSize(1);
//        assertThat(teamMates.get(0).getTeamMateStatus()).isEqualTo(TeamMateStatus.ONGOING);
//        assertThat(teamMates).extracting("user").containsExactly(goalCreator);
    }

}
