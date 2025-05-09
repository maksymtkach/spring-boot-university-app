package org.example.simpleuniversity.config;

import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.example.simpleuniversity.CommandLineApp;
import org.example.simpleuniversity.model.Degree;
import org.example.simpleuniversity.model.Department;
import org.example.simpleuniversity.model.Lector;
import org.example.simpleuniversity.repository.DepartmentRepository;
import org.example.simpleuniversity.repository.LectorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Order(1)
@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final LectorRepository lectorRepo;
    private final DepartmentRepository deptRepo;

    @Override
    @Transactional
    public void run(String... args) {
        if (lectorRepo.count() > 0) return;
        System.out.println("Running DataInitializer…");

        Lector john = Lector.builder()
                .firstName("Ivan")
                .lastName("Petrenko")
                .salary(5000)
                .degree(Degree.ASSISTANT)
                .build();
        john.setDepartments(new HashSet<>());

        Lector jane = Lector.builder()
                .firstName("Petro")
                .lastName("Ivanov")
                .salary(4500)
                .degree(Degree.ASSISTANT)
                .build();
        jane.setDepartments(new HashSet<>());

        Lector albert = Lector.builder()
                .firstName("Albert")
                .lastName("Einstein")
                .salary(7000)
                .degree(Degree.PROFESSOR)
                .build();
        albert.setDepartments(new HashSet<>());


        lectorRepo.saveAll(List.of(john, jane, albert));

        Department physics = Department.builder()
                .name("Physics")
                .head(albert)
                .lectors(new HashSet<>(List.of(albert, john)))
                .build();

        Department math = Department.builder()
                .name("Mathematics")
                .head(john)
                .lectors(new HashSet<>(List.of(john, jane)))
                .build();

        deptRepo.saveAll(List.of(physics, math));

        john.getDepartments().addAll(List.of(physics, math));
        jane.getDepartments().add(math);
        albert.getDepartments().add(physics);

        lectorRepo.saveAll(List.of(john, jane, albert));

        System.out.println("Sample data loaded.");
    }
}
