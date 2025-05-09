package org.example.simpleuniversity.repository;

import org.example.simpleuniversity.model.Lector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LectorRepository extends JpaRepository<Lector,Long> {
    List<Lector> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String fn, String ln);
    Optional<Lector> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);
}
