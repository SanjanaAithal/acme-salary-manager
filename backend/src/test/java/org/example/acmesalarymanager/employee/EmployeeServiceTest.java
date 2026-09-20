package org.example.acmesalarymanager.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2025, 3, 10, 9, 30);

    @Mock
    private EmployeeRepository repository;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2025-03-10T09:30:00Z"), ZoneOffset.UTC);
        service = new EmployeeService(repository, fixedClock);
    }

    @Test
    void create_savesEmployeeWithLowercasedEmailAndTimestamps() {
        when(repository.existsByEmail("asha@acme.com")).thenReturn(false);
        when(repository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            ReflectionTestUtils.setField(employee, "id", 1L);
            return employee;
        });

        EmployeeResponse response = service.create(request("  Asha@Acme.com "));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("asha@acme.com");
        assertThat(response.salary()).isEqualByComparingTo("85000.00");
        assertThat(response.createdAt()).isEqualTo(NOW);
        assertThat(response.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void create_rejectsDuplicateEmail() {
        when(repository.existsByEmail("asha@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("asha@acme.com")))
                .isInstanceOf(DuplicateEmailException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void get_returnsEmployee() {
        when(repository.findById(5L)).thenReturn(Optional.of(existing(5L)));

        EmployeeResponse response = service.get(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.fullName()).isEqualTo("Old Name");
    }

    @Test
    void get_throwsWhenEmployeeDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_changesFieldsAndUpdatedAtButKeepsCreatedAt() {
        Employee existing = existing(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmailAndIdNot("asha@acme.com", 5L)).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);

        EmployeeResponse response = service.update(5L, request("asha@acme.com"));

        assertThat(response.fullName()).isEqualTo("Asha Rao");
        assertThat(response.salary()).isEqualByComparingTo("85000.00");
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 8, 0));
        assertThat(response.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void update_rejectsEmailUsedByAnotherEmployee() {
        when(repository.findById(5L)).thenReturn(Optional.of(existing(5L)));
        when(repository.existsByEmailAndIdNot("asha@acme.com", 5L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(5L, request("asha@acme.com")))
                .isInstanceOf(DuplicateEmailException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void update_throwsWhenEmployeeDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request("asha@acme.com")))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void delete_removesExistingEmployee() {
        when(repository.existsById(5L)).thenReturn(true);

        service.delete(5L);

        verify(repository).deleteById(5L);
    }

    @Test
    void delete_throwsWhenEmployeeDoesNotExist() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(EmployeeNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    private static EmployeeRequest request(String email) {
        return new EmployeeRequest(
                "Asha Rao",
                email,
                "Software Engineer",
                "Engineering",
                "India",
                new BigDecimal("85000.00"),
                LocalDate.of(2022, 6, 1),
                EmploymentStatus.ACTIVE);
    }

    private static Employee existing(Long id) {
        Employee employee = new Employee();
        ReflectionTestUtils.setField(employee, "id", id);
        employee.setFullName("Old Name");
        employee.setEmail("old@acme.com");
        employee.setJobTitle("Analyst");
        employee.setDepartment("Finance");
        employee.setCountry("UK");
        employee.setSalary(new BigDecimal("50000.00"));
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setStatus(EmploymentStatus.ON_LEAVE);
        employee.setCreatedAt(LocalDateTime.of(2024, 1, 1, 8, 0));
        employee.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 8, 0));
        return employee;
    }
}