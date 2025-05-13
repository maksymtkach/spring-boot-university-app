package org.example.simpleuniversity.repository;

import lombok.RequiredArgsConstructor;
import org.example.simpleuniversity.model.Lector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectorRepository extends JpaRepository<Lector,Long> {
    List<Lector> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String fn, String ln);
    Optional<Lector> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    Object findByFirstNameIgnoreCase(String firstName);
}
