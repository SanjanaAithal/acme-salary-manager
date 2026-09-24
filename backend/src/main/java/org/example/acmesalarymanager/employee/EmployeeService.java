package org.example.acmesalarymanager.employee;

import org.example.acmesalarymanager.common.PageResponse;
import org.example.acmesalarymanager.currency.CountryCatalog;
import org.example.acmesalarymanager.currency.CountryCurrency;
import org.example.acmesalarymanager.currency.CurrencyConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class EmployeeService {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "id", "fullName", "email", "jobTitle", "department",
            "country", "salary", "hireDate", "status");

    private final EmployeeRepository repository;
    private final Clock clock;
    private final CountryCatalog countryCatalog;
    private final CurrencyConverter currencyConverter;

    public EmployeeService(EmployeeRepository repository, Clock clock,
                           CountryCatalog countryCatalog, CurrencyConverter currencyConverter) {
        this.repository = repository;
        this.clock = clock;
        this.countryCatalog = countryCatalog;
        this.currencyConverter = currencyConverter;
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        CountryCurrency country = countryCatalog.require(request.country());
        String email = normalizeEmail(request.email());
        if (repository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        Employee employee = new Employee();
        apply(employee, request, email, country);
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        return EmployeeResponse.from(repository.save(employee));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(Long id) {
        return EmployeeResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> search(EmployeeFilter filter, Pageable pageable) {
        Page<Employee> page = repository.findAll(
                EmployeeSpecifications.matching(filter),
                withStableSort(pageable));
        return PageResponse.from(page.map(EmployeeResponse::from));
    }

    @Transactional(readOnly = true)
    public FilterOptionsResponse filterOptions() {
        return new FilterOptionsResponse(
                repository.findDistinctCountries(),
                repository.findDistinctDepartments(),
                repository.findDistinctJobTitles(),
                List.of(EmploymentStatus.values()));
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findOrThrow(id);
        CountryCurrency country = countryCatalog.require(request.country());
        String email = normalizeEmail(request.email());
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateEmailException(email);
        }

        apply(employee, request, email, country);
        employee.setUpdatedAt(LocalDateTime.now(clock));

        return EmployeeResponse.from(repository.save(employee));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        repository.deleteById(id);
    }

    /**
     * Only allows known sort fields, and always adds id as a final tie-breaker so
     * pages don't overlap or skip rows when many employees share the same value.
     */
    private static Pageable withStableSort(Pageable pageable) {
        Sort sort = pageable.getSort();
        sort.forEach(order -> {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new InvalidSortException(order.getProperty());
            }
        });
        if (sort.getOrderFor("id") == null) {
            sort = sort.and(Sort.by("id"));
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Employee findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void apply(Employee employee, EmployeeRequest request, String email, CountryCurrency country) {
        employee.setFullName(request.fullName().trim());
        employee.setEmail(email);
        employee.setJobTitle(request.jobTitle().trim());
        employee.setDepartment(request.department().trim());
        employee.setCountry(country.country());
        employee.setCurrency(country.currency());
        employee.setSalary(request.salary());
        employee.setSalaryUsd(currencyConverter.toUsd(request.salary(), country.currency()));
        employee.setHireDate(request.hireDate());
        employee.setStatus(request.status());
    }
}