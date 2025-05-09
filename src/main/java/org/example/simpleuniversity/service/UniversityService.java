package org.example.simpleuniversity.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.simpleuniversity.model.Degree;
import org.example.simpleuniversity.model.Department;
import org.example.simpleuniversity.model.Lector;
import org.example.simpleuniversity.repository.DepartmentRepository;
import org.example.simpleuniversity.repository.LectorRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class UniversityService {
    private final DepartmentRepository departmentRepository;
    private final LectorRepository lectorRepository;

    public String getHeadOf(String deptName) {
        Department d = departmentRepository.findByNameIgnoreCase(deptName)
                .orElseThrow(() -> new IllegalArgumentException("No such department"));
        return d.getHead().getFirstName() + " " + d.getHead().getLastName();
    }

    public Map<Degree, Long> getStatistics(String deptName) {
        return departmentRepository.findByNameIgnoreCase(deptName)
                .orElseThrow(() -> new IllegalArgumentException("No such department"))
                .getLectors().stream()
                .collect(Collectors.groupingBy(Lector::getDegree, Collectors.counting()));
    }

    public double getAverageSalary(String deptName) {
        return departmentRepository.findByNameIgnoreCase(deptName)
                .orElseThrow(() -> new IllegalArgumentException("No such department"))
                .getLectors().stream()
                .mapToDouble(Lector::getSalary)
                .average().orElse(0.0);
    }

    public long getEmployeeCount(String deptName) {
        return departmentRepository.findByNameIgnoreCase(deptName)
                .orElseThrow(() -> new IllegalArgumentException("No such department"))
                .getLectors().size();
    }

    public String globalSearch(String template) {
        return lectorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(template, template)
                .stream()
                .map(l -> l.getFirstName() + " " + l.getLastName())
                .collect(Collectors.joining(", "));
    }

    private Department findDepartment(String key) {
        if (key.matches("\\d+")) {
            return departmentRepository.findById(Long.parseLong(key))
                    .orElseThrow(() -> new IllegalArgumentException("No such department id=" + key));
        }
        return departmentRepository.findByNameIgnoreCase(key)
                .orElseThrow(() -> new IllegalArgumentException("No such department name=" + key));
    }

    private Lector findLector(String key) {
        if (key.trim().contains(" ")) {
            String[] parts = key.trim().split("\\s+", 2);
            return lectorRepository
                    .findByFirstNameIgnoreCaseAndLastNameIgnoreCase(parts[0], parts[1])
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("No lector with name \"%s\"", key)));
        }

        if (key.matches("\\d+")) {
            return lectorRepository.findById(Long.parseLong(key))
                    .orElseThrow(() -> new IllegalArgumentException("No such lector id=" + key));
        }

        List<Lector> list = lectorRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(key, key);
        if (list.isEmpty()) {
            throw new IllegalArgumentException("No such lector name contains=" + key);
        }
        return list.get(0);
    }

    public Department createDepartment(String name, String headKey) {
        Lector head = findLector(headKey);

        Department d = new Department();
        d.setName(name);
        d.setHead(head);

        d.getLectors().add(head);

        return departmentRepository.save(d);
    }


    public void updateDepartmentHead(String deptKey, String headKey) {
        Department d = findDepartment(deptKey);

        Lector old = d.getHead();
        if (old != null) {
            d.getLectors().remove(old);
        }

        Lector newHead = findLector(headKey);
        d.setHead(newHead);

        d.getLectors().add(newHead);

        departmentRepository.save(d);
    }


    public void deleteDepartment(String deptKey) {
        Department d = findDepartment(deptKey);
        departmentRepository.delete(d);
    }

    public Lector createLector(String firstName, String lastName, Degree degree, double salary, List<String> deptKeys) {
        Lector l = new Lector();
        l.setFirstName(firstName);
        l.setLastName(lastName);
        l.setDegree(degree);
        l.setSalary(salary);

        Set<Department> deps = deptKeys.stream()
                .map(this::findDepartment)
                .collect(Collectors.toSet());
        l.setDepartments(deps);
        Lector saved = lectorRepository.save(l);

        deps.forEach(d -> {
            d.getLectors().add(saved);
            departmentRepository.save(d);
        });

        return saved;
    }

    public void updateLector(String lectorKey, String field, String newValue) {
        Lector l = findLector(lectorKey);
        switch (field.toLowerCase()) {
            case "firstname": l.setFirstName(newValue); break;
            case "lastname":  l.setLastName(newValue);  break;
            case "degree":    l.setDegree(Degree.valueOf(newValue.toUpperCase())); break;
            case "salary":    l.setSalary(Double.parseDouble(newValue)); break;
            case "departments":
                Set<Department> deps = Arrays.stream(newValue.split(","))
                        .map(String::trim)
                        .map(this::findDepartment)
                        .collect(Collectors.toSet());
                l.setDepartments(deps);
                break;
            default:
                throw new IllegalArgumentException("Unknown field: " + field);
        }
        lectorRepository.save(l);
    }

    public void deleteLector(String lectorKey) {
        Lector l = findLector(lectorKey);
        l.getDepartments().forEach(d -> {
            d.getLectors().remove(l);
            departmentRepository.save(d);
        });
        lectorRepository.delete(l);
    }

    public List<Department> listDepartments() {
        return departmentRepository.findAll();
    }

    public List<Lector> listLectors() {
        return lectorRepository.findAll();
    }
}

