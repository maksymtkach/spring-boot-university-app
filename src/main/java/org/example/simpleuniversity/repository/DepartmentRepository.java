package org.example.simpleuniversity.repository;

import org.example.simpleuniversity.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department,Long> {
    Optional<Department> findByNameIgnoreCase(String name);
}
