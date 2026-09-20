package org.example.acmesalarymanager.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository repository;

    @Test
    void savesEmployeeAndGeneratesId() {
        Employee saved = repository.saveAndFlush(employee("asha@acme.com"));

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId()))
                .get()
                .satisfies(found -> {
                    assertThat(found.getFullName()).isEqualTo("Asha Rao");
                    assertThat(found.getSalary()).isEqualByComparingTo("85000.00");
                    assertThat(found.getStatus()).isEqualTo(EmploymentStatus.ACTIVE);
                });
    }

    @Test
    void existsByEmail_isTrueOnlyForSavedEmail() {
        repository.saveAndFlush(employee("asha@acme.com"));

        assertThat(repository.existsByEmail("asha@acme.com")).isTrue();
        assertThat(repository.existsByEmail("other@acme.com")).isFalse();
    }

    @Test
    void existsByEmailAndIdNot_ignoresTheEmployeeItself() {
        Employee saved = repository.saveAndFlush(employee("asha@acme.com"));

        assertThat(repository.existsByEmailAndIdNot("asha@acme.com", saved.getId())).isFalse();
        assertThat(repository.existsByEmailAndIdNot("asha@acme.com", saved.getId() + 1)).isTrue();
    }

    @Test
    void rejectsDuplicateEmailAtDatabaseLevel() {
        repository.saveAndFlush(employee("asha@acme.com"));

        assertThatThrownBy(() -> repository.saveAndFlush(employee("asha@acme.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Employee employee(String email) {
        LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 0);
        Employee employee = new Employee();
        employee.setFullName("Asha Rao");
        employee.setEmail(email);
        employee.setJobTitle("Software Engineer");
        employee.setDepartment("Engineering");
        employee.setCountry("India");
        employee.setSalary(new BigDecimal("85000.00"));
        employee.setHireDate(LocalDate.of(2022, 6, 1));
        employee.setStatus(EmploymentStatus.ACTIVE);
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);
        return employee;
    }
}