package org.example.simpleuniversity;

import org.example.simpleuniversity.model.Degree;
import org.example.simpleuniversity.model.Department;
import org.example.simpleuniversity.model.Lector;
import org.example.simpleuniversity.repository.DepartmentRepository;
import org.example.simpleuniversity.repository.LectorRepository;
import org.example.simpleuniversity.service.UniversityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class UniversityServiceIT {

    @Autowired
    private UniversityService service;

    @Autowired
    private DepartmentRepository deptRepo;

    @Autowired
    private LectorRepository lectorRepo;

    @BeforeEach
    void cleanDatabase() {
        lectorRepo.deleteAll();
        deptRepo.deleteAll();
    }

    @Test
    void testGetHeadOfExists() {
        Lector head = new Lector();
        head.setFirstName("Alice");
        head.setLastName("Wonderland");
        lectorRepo.save(head);

        Department dept = new Department();
        dept.setName("Magic");
        dept.setHead(head);
        dept.getLectors().add(head);
        deptRepo.save(dept);

        String actual = service.getHeadOf("Magic");
        assertThat(actual).isEqualTo("Alice Wonderland");
    }

    @Test
    void testGetHeadOfNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getHeadOf("Nonexistent"));
    }

    @Test
    void testGetStatisticsCountsDegrees() {
        Lector a = new Lector(); a.setDegree(Degree.ASSISTANT);
        Lector b = new Lector(); b.setDegree(Degree.ASSOCIATE_PROFESSOR);
        Lector c = new Lector(); c.setDegree(Degree.ASSISTANT);
        lectorRepo.saveAll(List.of(a, b, c));

        Department dept = new Department();
        dept.setName("Eng");
        dept.getLectors().addAll(List.of(a, b, c));
        deptRepo.save(dept);

        Map<Degree, Long> stats = service.getStatistics("Eng");
        assertThat(stats)
                .containsEntry(Degree.ASSISTANT, 2L)
                .containsEntry(Degree.ASSOCIATE_PROFESSOR, 1L)
                .doesNotContainKey(Degree.PROFESSOR);
    }

    @Test
    void testGetStatisticsEmpty() {
        Department dept = new Department();
        dept.setName("Empty");
        deptRepo.save(dept);

        Map<Degree, Long> stats = service.getStatistics("Empty");
        assertThat(stats).isEmpty();
    }

    @Test
    void testGetAverageSalaryCalculation() {
        Lector l1 = new Lector(); l1.setSalary(1000.0);
        Lector l2 = new Lector(); l2.setSalary(3000.0);
        lectorRepo.saveAll(List.of(l1, l2));

        Department dept = new Department();
        dept.setName("Salaries");
        dept.getLectors().addAll(List.of(l1, l2));
        deptRepo.save(dept);

        double avg = service.getAverageSalary("Salaries");
        assertThat(avg).isEqualTo((1000.0 + 3000.0) / 2, org.assertj.core.api.Assertions.within(1e-6));
    }

    @Test
    void testGetAverageSalaryNoLectors() {
        Department dept = new Department();
        dept.setName("Zero");
        deptRepo.save(dept);

        double avg = service.getAverageSalary("Zero");
        assertThat(avg).isZero();
    }

    @Test
    void testGetEmployeeCount() {
        Lector x = new Lector();
        Lector y = new Lector();
        Lector z = new Lector();
        lectorRepo.saveAll(List.of(x, y, z));

        Department dept = new Department();
        dept.setName("CountMe");
        dept.getLectors().addAll(List.of(x, y, z));
        deptRepo.save(dept);

        long count = service.getEmployeeCount("CountMe");
        assertThat(count).isEqualTo(3);
    }

    @Test
    void testGetEmployeeCountNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getEmployeeCount("Nobody"));
    }

    @Test
    void testGlobalSearchFound() {
        Lector a = new Lector(); a.setFirstName("John");  a.setLastName("Doe");
        Lector b = new Lector(); b.setFirstName("Jane");  b.setLastName("Smith");
        lectorRepo.saveAll(List.of(a, b));

        String result = service.globalSearch("jo");
        assertThat(result).isEqualTo("John Doe");
    }

    @Test
    void testGlobalSearchNoMatch() {
        String result = service.globalSearch("zzz");
        assertThat(result).isEmpty();
    }

    @Test
    void testCreateDepartment() {
        Lector head = new Lector();
        head.setFirstName("Bob");
        head.setLastName("Builder");
        lectorRepo.save(head);

        Department created = service.createDepartment("BuildDept", head.getId().toString());
        assertThat(created.getName()).isEqualTo("BuildDept");
        assertThat(created.getHead().getId()).isEqualTo(head.getId());

        Optional<Department> persisted = deptRepo.findByNameIgnoreCase("BuildDept");
        assertThat(persisted).isPresent();
    }

    @Test
    void testCreateDepartmentHeadNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createDepartment("NoDept", "9999"));
    }

    @Test
    void testUpdateDepartmentHead() {
        Lector oldHead = new Lector(); oldHead.setFirstName("Old"); oldHead.setLastName("Head");
        Lector newHead = new Lector(); newHead.setFirstName("New"); newHead.setLastName("Head");
        lectorRepo.saveAll(List.of(oldHead, newHead));

        Department dept = new Department();
        dept.setName("DeptX");
        dept.setHead(oldHead);
        dept.getLectors().add(oldHead);
        deptRepo.save(dept);

        service.updateDepartmentHead(dept.getId().toString(), newHead.getId().toString());

        Department updated = deptRepo.findById(dept.getId()).get();
        assertThat(updated.getHead().getId()).isEqualTo(newHead.getId());
        assertThat(updated.getLectors()).contains(newHead).doesNotContain(oldHead);
    }

    @Test
    void testUpdateDepartmentHeadDeptNotFound() {
        Lector newHead = new Lector(); newHead.setFirstName("Nobody"); newHead.setLastName("Home");
        lectorRepo.save(newHead);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateDepartmentHead("12345", newHead.getId().toString()));
    }

    @Test
    void testDeleteDepartment() {
        Department dept = new Department();
        dept.setName("Trash");
        deptRepo.save(dept);

        service.deleteDepartment("Trash");
        assertThat(deptRepo.findByNameIgnoreCase("Trash")).isEmpty();
    }

    @Test
    void testDeleteDepartmentNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteDepartment("DoesNotExist"));
    }

    @Test
    void testCreateLectorWithDepartments() {
        Department d1 = new Department(); d1.setName("D1");
        Department d2 = new Department(); d2.setName("D2");
        deptRepo.saveAll(List.of(d1, d2));

        Lector created = service.createLector(
                "First", "Last", Degree.ASSISTANT, 1234.0,
                List.of(d1.getId().toString(), d2.getId().toString())
        );

        assertThat(created.getId()).isNotNull();
        Lector persisted = lectorRepo.findById(created.getId()).get();
        assertThat(persisted.getDepartments())
                .extracting(Department::getId)
                .containsExactlyInAnyOrder(d1.getId(), d2.getId());
    }

    @Test
    void testCreateLectorDeptNotFound() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createLector(
                        "X", "Y", Degree.ASSISTANT, 1000.0,
                        List.of("9999")
                ));
    }

    @Test
    void testUpdateLectorLastName() {
        Lector lect = new Lector();
        lect.setFirstName("A"); lect.setLastName("Old");
        lectorRepo.save(lect);

        service.updateLector(lect.getId().toString(), "lastname", "New");
        Lector updated = lectorRepo.findById(lect.getId()).get();
        assertThat(updated.getLastName()).isEqualTo("New");
    }

    @Test
    void testDeleteLector() {
        Lector lect = new Lector();
        lect.setFirstName("To"); lect.setLastName("Gone");
        lectorRepo.save(lect);

        Department dep = new Department();
        dep.setName("Dep");
        dep.getLectors().add(lect);
        deptRepo.save(dep);

        lect.getDepartments().add(dep);
        lectorRepo.save(lect);

        service.deleteLector(lect.getId().toString());

        assertThat(lectorRepo.findById(lect.getId())).isEmpty();
        Department refreshed = deptRepo.findById(dep.getId()).get();
        assertThat(refreshed.getLectors()).doesNotContain(lect);
    }
}
