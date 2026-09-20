package org.example.acmesalarymanager.employee;

import org.example.acmesalarymanager.common.PageResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService service;

    @Test
    void list_passesFiltersPagingAndSortToService() throws Exception {
        when(service.search(any(EmployeeFilter.class), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 1, 10, 0, 0));

        mockMvc.perform(get("/api/employees")
                        .param("search", "ash")
                        .param("country", "India")
                        .param("department", "Engineering")
                        .param("jobTitle", "Software Engineer")
                        .param("status", "ACTIVE")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "salary,desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<EmployeeFilter> filter = ArgumentCaptor.forClass(EmployeeFilter.class);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).search(filter.capture(), pageable.capture());

        assertThat(filter.getValue()).isEqualTo(new EmployeeFilter(
                "ash", "India", "Engineering", "Software Engineer", EmploymentStatus.ACTIVE));
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageable.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "salary"));
    }

    @Test
    void list_usesDefaultPageWhenNoParamsGiven() throws Exception {
        when(service.search(any(EmployeeFilter.class), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/employees")).andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).search(any(EmployeeFilter.class), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void list_returnsPageJson() throws Exception {
        LocalDateTime now = LocalDateTime.of(2025, 3, 10, 9, 30);
        EmployeeResponse asha = new EmployeeResponse(1L, "Asha Rao", "asha@acme.com",
                "Software Engineer", "Engineering", "India", new BigDecimal("85000.00"),
                LocalDate.of(2022, 6, 1), EmploymentStatus.ACTIVE, now, now);
        when(service.search(any(EmployeeFilter.class), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(asha), 0, 20, 41, 3));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Asha Rao"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(41))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void list_returns400ForUnknownStatus() throws Exception {
        mockMvc.perform(get("/api/employees").param("status", "RETIRED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid parameter"));
    }

    @Test
    void list_returns400ForUnsupportedSortField() throws Exception {
        when(service.search(any(EmployeeFilter.class), any(Pageable.class)))
                .thenThrow(new InvalidSortException("password"));

        mockMvc.perform(get("/api/employees").param("sort", "password,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid sort"));
    }

    @Test
    void filterOptions_returnsDropdownValues() throws Exception {
        when(service.filterOptions()).thenReturn(new FilterOptionsResponse(
                List.of("India", "UK"), List.of("Engineering"), List.of("Analyst"),
                List.of(EmploymentStatus.ACTIVE)));

        mockMvc.perform(get("/api/employees/filter-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countries[1]").value("UK"))
                .andExpect(jsonPath("$.departments[0]").value("Engineering"))
                .andExpect(jsonPath("$.jobTitles[0]").value("Analyst"))
                .andExpect(jsonPath("$.statuses[0]").value("ACTIVE"));
    }
}