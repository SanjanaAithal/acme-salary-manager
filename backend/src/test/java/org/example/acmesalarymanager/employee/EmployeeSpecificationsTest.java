package org.example.acmesalarymanager.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeSpecificationsTest {

    @Autowired
    private EmployeeRepository repository;

    @BeforeEach
    void seed() {
        save("Asha Rao", "asha@acme.com", "India", "Engineering", "Software Engineer", "85000", EmploymentStatus.ACTIVE);
        save("Ravi Kumar", "ravi@acme.com", "India", "Engineering", "Engineering Manager", "120000", EmploymentStatus.ACTIVE);
        save("John Smith", "john@acme.com", "UK", "Finance", "Analyst", "50000", EmploymentStatus.ON_LEAVE);
        save("Maria Garcia", "maria@acme.com", "Spain", "Engineering", "Software Engineer", "70000", EmploymentStatus.TERMINATED);
        save("Ashley Brown", "ashley@acme.com", "UK", "Sales", "Account Executive", "60000", EmploymentStatus.ACTIVE);
        repository.flush();
    }

    @Test
    void emptyFilterReturnsEveryone() {
        assertThat(names(EmployeeFilter.empty())).hasSize(5);
    }

    @Test
    void blankValuesAreIgnored() {
        EmployeeFilter filter = new EmployeeFilter("  ", " ", "", null, null);

        assertThat(names(filter)).hasSize(5);
    }

    @Test
    void searchMatchesNamePartsIgnoringCase() {
        EmployeeFilter filter = new EmployeeFilter("ASH", null, null, null, null);

        assertThat(names(filter)).containsExactlyInAnyOrder("Asha Rao", "Ashley Brown");
    }

    @Test
    void searchAlsoMatchesEmail() {
        EmployeeFilter filter = new EmployeeFilter("john@", null, null, null, null);

        assertThat(names(filter)).containsExactly("John Smith");
    }

    @Test
    void searchTreatsPercentSignLiterally() {
        EmployeeFilter filter = new EmployeeFilter("%", null, null, null, null);

        assertThat(names(filter)).isEmpty();
    }

    @Test
    void filtersByCountry() {
        EmployeeFilter filter = new EmployeeFilter(null, "India", null, null, null);

        assertThat(names(filter)).containsExactlyInAnyOrder("Asha Rao", "Ravi Kumar");
    }

    @Test
    void filtersByDepartmentAndJobTitle() {
        EmployeeFilter filter = new EmployeeFilter(null, null, "Engineering", "Software Engineer", null);

        assertThat(names(filter)).containsExactlyInAnyOrder("Asha Rao", "Maria Garcia");
    }

    @Test
    void filtersByStatus() {
        EmployeeFilter filter = new EmployeeFilter(null, null, null, null, EmploymentStatus.TERMINATED);

        assertThat(names(filter)).containsExactly("Maria Garcia");
    }

    @Test
    void combinesAllFiltersWithAnd() {
        EmployeeFilter filter = new EmployeeFilter("ash", "UK", null, null, EmploymentStatus.ACTIVE);

        assertThat(names(filter)).containsExactly("Ashley Brown");
    }

    @Test
    void returnsRequestedPageSortedBySalaryDescending() {
        PageRequest firstPage = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "salary"));

        Page<Employee> page = repository.findAll(
                EmployeeSpecifications.matching(EmployeeFilter.empty()), firstPage);

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getContent()).extracting(Employee::getFullName)
                .containsExactly("Ravi Kumar", "Asha Rao");
    }

    @Test
    void distinctFilterValuesAreSortedAlphabetically() {
        assertThat(repository.findDistinctCountries()).containsExactly("India", "Spain", "UK");
        assertThat(repository.findDistinctDepartments()).containsExactly("Engineering", "Finance", "Sales");
        assertThat(repository.findDistinctJobTitles()).containsExactly(
                "Account Executive", "Analyst", "Engineering Manager", "Software Engineer");
    }

    private List<String> names(EmployeeFilter filter) {
        return repository.findAll(EmployeeSpecifications.matching(filter)).stream()
                .map(Employee::getFullName)
                .toList();
    }

    private void save(String name, String email, String country, String department,
                      String jobTitle, String salary, EmploymentStatus status) {
        LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 0);
        Employee employee = new Employee();
        employee.setFullName(name);
        employee.setEmail(email);
        employee.setCountry(country);
        employee.setDepartment(department);
        employee.setJobTitle(jobTitle);
        employee.setSalary(new BigDecimal(salary));
        employee.setHireDate(LocalDate.of(2022, 6, 1));
        employee.setStatus(status);
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);
        repository.save(employee);
    }
}