package dev.sogong_girls.mr_world.global.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.sogong_girls.mr_world.domain.tour.service.TourService;
import dev.sogong_girls.mr_world.global.exception.BusinessErrorCode;
import dev.sogong_girls.mr_world.global.exception.BusinessException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class RequestLoggingFilterTest {
    @Autowired
    MockMvc mvc;
    @MockitoBean
    TourService tourService;

    @Test
    void logsSuccessfulRequestAtInfo(CapturedOutput output) throws Exception {
        given(tourService.getTourList()).willReturn(List.of());

        mvc.perform(get("/api/tours").queryParam("theme", "GOLF")).andExpect(status().isOk());

        assertThat(output).containsPattern("INFO .*GET /api/tours\\?theme=GOLF -> 200 OK \\(\\d+ms\\)");
    }

    @Test
    void logsBusinessExceptionCauseAtWarn(CapturedOutput output) throws Exception {
        given(tourService.getTour(99L)).willThrow(new BusinessException(BusinessErrorCode.NOT_FOUND));

        mvc.perform(get("/api/tours/99")).andExpect(status().isNotFound());

        assertThat(output).containsPattern("WARN .*GET /api/tours/99 -> 404 NOT_FOUND \\(\\d+ms\\)"
                + " \\| cause: BusinessException: 대상을 찾을 수 없습니다\\.");
    }

    @Test
    void logsInvalidFieldsForValidationFailure(CapturedOutput output) throws Exception {
        mvc.perform(post("/api/tours/1/price").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"tourStyleId":1,"hotelOptionId":1,"foodOptionId":1,"participantsCount":0}
                        """))
                .andExpect(status().isBadRequest());

        assertThat(output).containsPattern("POST /api/tours/1/price -> 400 BAD_REQUEST .*"
                + "cause: MethodArgumentNotValidException: participantsCount=");
    }

    @Test
    void logsAuthenticationFailureFromSecurityFilter(CapturedOutput output) throws Exception {
        mvc.perform(get("/api/admin/bookings")).andExpect(status().isUnauthorized());

        assertThat(output).containsPattern("GET /api/admin/bookings -> 401 UNAUTHORIZED .*"
                + "cause: InsufficientAuthenticationException");
    }

    @Test
    void logsUnexpectedErrorAtErrorWithStackTrace(CapturedOutput output) throws Exception {
        given(tourService.getTourList()).willThrow(new IllegalStateException("DB connection lost"));

        mvc.perform(get("/api/tours")).andExpect(status().isInternalServerError());

        assertThat(output).containsPattern("ERROR .*GET /api/tours -> 500 INTERNAL_SERVER_ERROR .*"
                + "cause: IllegalStateException: DB connection lost");
        assertThat(output).contains("java.lang.IllegalStateException: DB connection lost", "\tat ");
    }
}
