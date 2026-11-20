package dev.sogong_girls.mr_world.global.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
@WithMockUser
class GlobalExceptionHandlerTest {
    @Autowired
    MockMvc mvc;

    @Test
    void respondsNotFoundForUnmappedUrl(CapturedOutput output) throws Exception {
        mvc.perform(get("/api/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(BusinessErrorCode.NOT_FOUND.getMessage()));

        assertThat(output).containsPattern("WARN .*GET /api/does-not-exist -> 404 NOT_FOUND .*"
                + "cause: NoResourceFoundException");
    }

    @Test
    void respondsMethodNotAllowedWithAllowHeader(CapturedOutput output) throws Exception {
        mvc.perform(patch("/api/tours"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "GET"))
                .andExpect(content().string(BusinessErrorCode.METHOD_NOT_ALLOWED.getMessage()));

        assertThat(output).containsPattern("WARN .*PATCH /api/tours -> 405 METHOD_NOT_ALLOWED .*"
                + "cause: HttpRequestMethodNotSupportedException");
    }
}
