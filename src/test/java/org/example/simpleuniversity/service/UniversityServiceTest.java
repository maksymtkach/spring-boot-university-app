package org.example.simpleuniversity.service;

import org.example.simpleuniversity.model.Degree;
import org.example.simpleuniversity.model.Department;
import org.example.simpleuniversity.model.Lector;
import org.example.simpleuniversity.repository.DepartmentRepository;
import org.example.simpleuniversity.repository.LectorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UniversityServiceTest {

    @Mock
    private DepartmentRepository deptRepo;

    @Mock
    private LectorRepository lectorRepo;

    @InjectMocks
    private UniversityService svc;

    @Test
    void whenGetHeadOfExists_thenReturnsFullName() {
        Lector head = new Lector();
        head.setFirstName("Anna");
        head.setLastName("Kowalska");
        Department dept = new Department();
        dept.setHead(head);
        when(deptRepo.findByNameIgnoreCase("Physics"))
                .thenReturn(Optional.of(dept));

        String result = svc.getHeadOf("Physics");

        assertEquals("Anna Kowalska", result);
        verify(deptRepo).findByNameIgnoreCase("Physics");
    }

    @Test
    void whenGetHeadOfNotExists_thenThrows() {
        when(deptRepo.findByNameIgnoreCase("Unknown"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> svc.getHeadOf("Unknown"));
        verify(deptRepo).findByNameIgnoreCase("Unknown");
    }

    @Test
    void whenGetStatistics_thenGroupByDegree() {
        Lector l1 = new Lector(); l1.setDegree(Degree.PROFESSOR);
        Lector l2 = new Lector(); l2.setDegree(Degree.ASSISTANT);
        Department dept = new Department();
        dept.setLectors(new HashSet<>(Arrays.asList(l1, l2)));
        when(deptRepo.findByNameIgnoreCase("Math"))
                .thenReturn(Optional.of(dept));

        Map<Degree, Long> stats = svc.getStatistics("Math");

        assertEquals(1L, stats.get(Degree.PROFESSOR));
        assertEquals(1L, stats.get(Degree.ASSISTANT));
        verify(deptRepo).findByNameIgnoreCase("Math");
    }

    @Test
    void whenGetStatisticsEmpty_thenEmptyMap() {
        Department dept = new Department();
        dept.setLectors(Collections.emptySet());
        when(deptRepo.findByNameIgnoreCase("Empty"))
                .thenReturn(Optional.of(dept));

        Map<Degree, Long> stats = svc.getStatistics("Empty");

        assertTrue(stats.isEmpty());
        verify(deptRepo).findByNameIgnoreCase("Empty");
    }

    @Test
    void whenGetAverageSalary_thenReturnsCorrect() {
        Lector l1 = new Lector(); l1.setSalary(100.0);
        Lector l2 = new Lector(); l2.setSalary(200.0);
        Department dept = new Department();
        dept.setLectors(new HashSet<>(Arrays.asList(l1, l2)));
        when(deptRepo.findByNameIgnoreCase("Eng"))
                .thenReturn(Optional.of(dept));

        double avg = svc.getAverageSalary("Eng");

        assertEquals(150.0, avg);
        verify(deptRepo).findByNameIgnoreCase("Eng");
    }

    @Test
    void whenGetAverageSalaryEmpty_thenZero() {
        Department dept = new Department();
        dept.setLectors(Collections.emptySet());
        when(deptRepo.findByNameIgnoreCase("None"))
                .thenReturn(Optional.of(dept));

        double avg = svc.getAverageSalary("None");

        assertEquals(0.0, avg);
        verify(deptRepo).findByNameIgnoreCase("None");
    }

    @Test
    void whenGetEmployeeCount_thenReturnsSize() {
        Lector l1 = new Lector(); l1.setId(1L);
        Lector l2 = new Lector(); l2.setId(2L);
        Lector l3 = new Lector(); l3.setId(3L);

        Department dept = new Department();
        dept.setLectors(new HashSet<>(Arrays.asList(l1, l2, l3)));

        when(deptRepo.findByNameIgnoreCase("Dept"))
                .thenReturn(Optional.of(dept));

        long count = svc.getEmployeeCount("Dept");

        assertEquals(3, count);
        verify(deptRepo).findByNameIgnoreCase("Dept");
    }


    @Test
    void whenGetEmployeeCountNotFound_thenThrows() {
        when(deptRepo.findByNameIgnoreCase("X"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> svc.getEmployeeCount("X"));
        verify(deptRepo).findByNameIgnoreCase("X");
    }

    @Test
    void whenGlobalSearchMatches_thenReturnsNames() {
        Lector a = new Lector(); a.setFirstName("John"); a.setLastName("Doe");
        Lector b = new Lector(); b.setFirstName("Jane"); b.setLastName("Smith");
        when(lectorRepo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("jo", "jo"))
                .thenReturn(Arrays.asList(a, b));

        String out = svc.globalSearch("jo");

        assertEquals("John Doe, Jane Smith", out);
        verify(lectorRepo).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("jo", "jo");
    }

    @Test
    void whenGlobalSearchNoMatches_thenEmptyString() {
        when(lectorRepo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("zzz", "zzz"))
                .thenReturn(Collections.emptyList());

        String out = svc.globalSearch("zzz");

        assertEquals("", out);
        verify(lectorRepo).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("zzz", "zzz");
    }

    @Test
    void whenCreateDepartment_thenSavesWithHead() {
        Lector head = new Lector(); head.setId(1L);
        when(lectorRepo.findById(1L)).thenReturn(Optional.of(head));
        when(lectorRepo.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(any(), any()))
                .thenReturn(Optional.of(head));
        when(lectorRepo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(any(), any()))
                .thenReturn(Arrays.asList(head));

        Department toSave = new Department();
        toSave.setName("NewDept");
        toSave.setHead(head);
        when(deptRepo.save(any(Department.class))).thenReturn(toSave);

        Department saved = svc.createDepartment("NewDept", "1");

        assertEquals("NewDept", saved.getName());
        assertSame(head, saved.getHead());
        verify(deptRepo).save(any(Department.class));
    }

    @Test
    void whenCreateDepartmentHeadNotFound_thenThrows() {
        when(lectorRepo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("foo", "foo"))
                .thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> svc.createDepartment("D", "foo"));
    }

    @Test
    void whenUpdateDepartmentHead_thenReassigns() {
        Lector old = new Lector(); old.setId(1L);
        Lector neu = new Lector(); neu.setId(2L);
        Department dept = new Department();
        dept.setHead(old);
        dept.setLectors(new HashSet<>(Arrays.asList(old)));
        when(deptRepo.findById(10L)).thenReturn(Optional.of(dept));
        when(deptRepo.findByNameIgnoreCase("10")).thenReturn(Optional.of(dept));
        when(lectorRepo.findById(2L)).thenReturn(Optional.of(neu));
        when(lectorRepo.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(any(), any()))
                .thenReturn(Arrays.asList(neu));

        svc.updateDepartmentHead("10", "2");

        assertEquals(neu, dept.getHead());
        assertTrue(dept.getLectors().contains(neu));
        verify(deptRepo, atLeastOnce()).save(dept);
    }

    @Test
    void whenUpdateDepartmentHeadDeptNotFound_thenThrows() {
        when(deptRepo.findByNameIgnoreCase("X")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> svc.updateDepartmentHead("X", "1"));
    }

    @Test
    void whenDeleteDepartment_thenDeletes() {
        Department dept = new Department();
        when(deptRepo.findByNameIgnoreCase("Del")).thenReturn(Optional.of(dept));

        svc.deleteDepartment("Del");

        verify(deptRepo).delete(dept);
    }

    @Test
    void whenDeleteDepartmentNotFound_thenThrows() {
        when(deptRepo.findByNameIgnoreCase("N")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> svc.deleteDepartment("N"));
    }

    @Test
    void whenCreateLector_thenAssignsToDepartments() {
        Department d1 = new Department(); d1.setId(1L);
        Department d2 = new Department(); d2.setId(2L);
        when(deptRepo.findById(1L)).thenReturn(Optional.of(d1));
        when(deptRepo.findByNameIgnoreCase("1")).thenReturn(Optional.of(d1));
        when(deptRepo.findById(2L)).thenReturn(Optional.of(d2));
        when(deptRepo.findByNameIgnoreCase("2")).thenReturn(Optional.of(d2));

        Lector saved = new Lector(); saved.setId(5L);
        when(lectorRepo.save(any(Lector.class))).thenReturn(saved);

        Lector result = svc.createLector("A", "B", Degree.ASSOCIATE_PROFESSOR, 123.0, Arrays.asList("1", "2"));

        assertEquals(5L, result.getId());
        verify(lectorRepo).save(any(Lector.class));
        verify(deptRepo, times(2)).save(any(Department.class));
    }

    @Test
    void whenCreateLectorDeptNotFound_thenThrows() {
        when(deptRepo.findByNameIgnoreCase("X")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> svc.createLector("A","B",Degree.ASSISTANT,100.0, List.of("X")));
    }

    @Test
    void whenUpdateLectorLastName_thenSaves() {
        Lector l = new Lector(); l.setLastName("Old");
        when(lectorRepo.findById(1L)).thenReturn(Optional.of(l));
        when(lectorRepo.findByNameIgnoreCase("1")).thenReturn(Optional.of(l));

        svc.updateLector("1", "lastname", "New");

        assertEquals("New", l.getLastName());
        verify(lectorRepo).save(l);
    }

    @Test
    void whenDeleteLector_thenRemovesFromDepartmentsAndDeletes() {
        Department d = new Department();
        Lector l = new Lector(); l.setId(3L);
        d.setLectors(new HashSet<>(List.of(l)));
        l.setDepartments(new HashSet<>(List.of(d)));
        when(lectorRepo.findById(3L)).thenReturn(Optional.of(l));
        when(lectorRepo.findByNameIgnoreCase("3")).thenReturn(Optional.of(l));

        svc.deleteLector("3");

        assertFalse(d.getLectors().contains(l));
        verify(deptRepo).save(d);
        verify(lectorRepo).delete(l);
    }
}
