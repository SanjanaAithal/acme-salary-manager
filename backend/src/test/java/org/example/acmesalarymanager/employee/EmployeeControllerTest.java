package org.example.acmesalarymanager.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    private static final String VALID_BODY = """
            {
              "fullName": "Asha Rao",
              "email": "asha@acme.com",
              "jobTitle": "Software Engineer",
              "department": "Engineering",
              "country": "India",
              "salary": 85000.00,
              "hireDate": "2022-06-01",
              "status": "ACTIVE"
            }
            """;

    private static final String INVALID_BODY = """
            {
              "fullName": "",
              "email": "not-an-email",
              "jobTitle": "Software Engineer",
              "department": "Engineering",
              "country": "India",
              "salary": -5,
              "hireDate": "2022-06-01",
              "status": "ACTIVE"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService service;

    @Test
    void create_returns201WithLocationAndBody() throws Exception {
        when(service.create(any(EmployeeRequest.class))).thenReturn(response(1L));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/employees/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("asha@acme.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void create_returns400WithFieldErrorsWhenInvalid() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.salary").exists());

        verifyNoInteractions(service);
    }

    @Test
    void create_returns400WhenBodyIsNotJson() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed request"));
    }

    @Test
    void create_returns409WhenEmailAlreadyExists() throws Exception {
        when(service.create(any(EmployeeRequest.class)))
                .thenThrow(new DuplicateEmailException("asha@acme.com"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate email"));
    }

    @Test
    void get_returnsEmployee() throws Exception {
        when(service.get(1L)).thenReturn(response(1L));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Asha Rao"))
                .andExpect(jsonPath("$.salary").value(85000.00));
    }

    @Test
    void get_returns404WhenMissing() throws Exception {
        when(service.get(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Employee not found"));
    }

    @Test
    void get_returns400WhenIdIsNotANumber() throws Exception {
        mockMvc.perform(get("/api/employees/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid parameter"));
    }

    @Test
    void update_returnsUpdatedEmployee() throws Exception {
        when(service.update(any(Long.class), any(EmployeeRequest.class))).thenReturn(response(1L));

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void delete_returns404WhenMissing() throws Exception {
        doThrow(new EmployeeNotFoundException(99L)).when(service).delete(99L);

        mockMvc.perform(delete("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    private static EmployeeResponse response(Long id) {
        LocalDateTime now = LocalDateTime.of(2025, 3, 10, 9, 30);
        return new EmployeeResponse(id, "Asha Rao", "asha@acme.com", "Software Engineer",
                "Engineering", "India", new BigDecimal("85000.00"),
                LocalDate.of(2022, 6, 1), EmploymentStatus.ACTIVE, now, now);
    }
}