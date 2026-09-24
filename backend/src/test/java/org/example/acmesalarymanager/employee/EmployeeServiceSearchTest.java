package org.example.acmesalarymanager.employee;

import org.example.acmesalarymanager.common.PageResponse;
import org.example.acmesalarymanager.currency.CountryCatalog;
import org.example.acmesalarymanager.currency.CurrencyConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceSearchTest {

    @Mock
    private EmployeeRepository repository;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2025-03-10T09:30:00Z"), ZoneOffset.UTC);
        service = new EmployeeService(repository, fixedClock, new CountryCatalog(), new CurrencyConverter());
    }

    @Test
    void search_mapsEntitiesAndPageMetadata() {
        Page<Employee> page = new PageImpl<>(List.of(employee(1L)), PageRequest.of(0, 20), 41);
        when(repository.findAll(anySpec(), any(Pageable.class))).thenReturn(page);

        PageResponse<EmployeeResponse> result =
                service.search(EmployeeFilter.empty(), PageRequest.of(0, 20));

        assertThat(result.content()).extracting(EmployeeResponse::fullName).containsExactly("Asha Rao");
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(41);
        assertThat(result.totalPages()).isEqualTo(3);
    }

    @Test
    void search_addsIdAsTieBreakerToRequestedSort() {
        when(repository.findAll(anySpec(), any(Pageable.class))).thenReturn(Page.empty());

        service.search(EmployeeFilter.empty(),
                PageRequest.of(2, 10, Sort.by(Sort.Direction.DESC, "salary")));

        Pageable used = capturedPageable();
        assertThat(used.getPageNumber()).isEqualTo(2);
        assertThat(used.getPageSize()).isEqualTo(10);
        assertThat(used.getSort()).isEqualTo(
                Sort.by(Sort.Order.desc("salary"), Sort.Order.asc("id")));
    }

    @Test
    void search_doesNotDuplicateIdWhenAlreadySortedById() {
        when(repository.findAll(anySpec(), any(Pageable.class))).thenReturn(Page.empty());

        service.search(EmployeeFilter.empty(), PageRequest.of(0, 20, Sort.by("id")));

        assertThat(capturedPageable().getSort()).isEqualTo(Sort.by("id"));
    }

    @Test
    void search_rejectsUnknownSortField() {
        assertThatThrownBy(() -> service.search(EmployeeFilter.empty(),
                PageRequest.of(0, 20, Sort.by("password"))))
                .isInstanceOf(InvalidSortException.class)
                .hasMessageContaining("password");

        verifyNoInteractions(repository);
    }

    @Test
    void filterOptions_combinesDistinctValuesAndAllStatuses() {
        when(repository.findDistinctCountries()).thenReturn(List.of("India", "UK"));
        when(repository.findDistinctDepartments()).thenReturn(List.of("Engineering"));
        when(repository.findDistinctJobTitles()).thenReturn(List.of("Analyst"));

        FilterOptionsResponse options = service.filterOptions();

        assertThat(options.countries()).containsExactly("India", "UK");
        assertThat(options.departments()).containsExactly("Engineering");
        assertThat(options.jobTitles()).containsExactly("Analyst");
        assertThat(options.statuses()).containsExactly(EmploymentStatus.values());
    }

    private Pageable capturedPageable() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(anySpec(), captor.capture());
        return captor.getValue();
    }

    private static Specification<Employee> anySpec() {
        return ArgumentMatchers.<Specification<Employee>>any();
    }

    private static Employee employee(Long id) {
        Employee employee = new Employee();
        ReflectionTestUtils.setField(employee, "id", id);
        employee.setFullName("Asha Rao");
        employee.setEmail("asha@acme.com");
        employee.setJobTitle("Software Engineer");
        employee.setDepartment("Engineering");
        employee.setCountry("India");
        employee.setSalary(new BigDecimal("85000.00"));
        employee.setHireDate(LocalDate.of(2022, 6, 1));
        employee.setStatus(EmploymentStatus.ACTIVE);
        employee.setCreatedAt(LocalDateTime.of(2025, 1, 1, 8, 0));
        employee.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 8, 0));
        return employee;
    }
}