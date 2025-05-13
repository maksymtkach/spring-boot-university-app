// src/main/java/org/example/simpleuniversity/CliRunner.java
package org.example.simpleuniversity;

import org.example.simpleuniversity.model.Degree;
import org.example.simpleuniversity.model.Department;
import org.example.simpleuniversity.model.Lector;
import org.example.simpleuniversity.service.UniversityService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Component
@Profile("!test")
public class CliRunner implements CommandLineRunner {

    private final UniversityService service;

    public CliRunner(UniversityService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter command (or 'exit'):");

        while (true) {
            String line = in.nextLine().trim();
            if ("exit".equalsIgnoreCase(line)) break;

            if (line.matches("(?i)who is head of department .+")) {
                String dept = line.replaceAll("(?i)who is head of department ", "");
                try {
                    String head = service.getHeadOf(dept);
                    System.out.printf("Head of %s department is %s%n", dept, head);
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if (line.matches("(?i)show .+ statistics\\.?")) {
                String dept = line.replaceAll("(?i)show (.+) statistics\\.?", "$1");
                try {
                    Map<Degree, Long> stats = service.getStatistics(dept);
                    System.out.printf(
                            "assistants - %d%n" +
                                    "associate professors - %d%n" +
                                    "professors - %d%n",
                            stats.getOrDefault(Degree.ASSISTANT, 0L),
                            stats.getOrDefault(Degree.ASSOCIATE_PROFESSOR, 0L),
                            stats.getOrDefault(Degree.PROFESSOR, 0L)
                    );
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if (line.matches("(?i)show the average salary for the department .+")) {
                String dept = line.replaceAll("(?i)show the average salary for the department ", "");
                try {
                    double avg = service.getAverageSalary(dept);
                    System.out.printf("The average salary of %s is %.2f%n", dept, avg);
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if (line.matches("(?i)show count of employee for .+")) {
                String dept = line.replaceAll("(?i)show count of employee for ", "");
                try {
                    long count = service.getEmployeeCount(dept);
                    System.out.println(count);
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if (line.matches("(?i)global search by .+")) {
                String tmpl = line.replaceAll("(?i)global search by ", "");
                try {
                    String result = service.globalSearch(tmpl);
                    if (result.isEmpty()) {
                        System.out.println("No matches found.");
                    } else {
                        System.out.println(result);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if (line.matches("(?i)add department .+ head .+")) {
                String[] parts = line.split("\\s+");
                String name = parts[2];
                String headKey = parts[4];
                try {
                    service.createDepartment(name, headKey);
                    System.out.println("Department created.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if (line.toLowerCase().startsWith("add lector ")) {
                String[] tokens = line.split("\\s+");
                String firstName = tokens[2];
                String lastName = tokens[3];
                String degree = tokens[5];
                double salary = Double.parseDouble(tokens[7]);

                List<String> deptKeys;
                int depIndex = line.toLowerCase().indexOf("departments");
                if (depIndex >= 0) {
                    String depsPart = line.substring(depIndex + "departments".length()).trim();
                    deptKeys = Arrays.stream(depsPart.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
                } else {
                    deptKeys = List.of();
                }

                try {
                    service.createLector(
                            firstName,
                            lastName,
                            Degree.valueOf(degree.toUpperCase()),
                            salary,
                            deptKeys
                    );
                    System.out.println("Lector created.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if (line.matches("(?i)update department .+ head .+")) {
                String[] parts = line.split("\\s+");
                String deptKey = parts[2];
                String newHeadKey = parts[4];
                try {
                    service.updateDepartmentHead(deptKey, newHeadKey);
                    System.out.println("Department head updated.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if (line.toLowerCase().startsWith("update lector ")) {
                String[] parts = line.split("\\s+", 5);
                if (parts.length < 5) {
                    System.out.println("Error: invalid update-lector format.");
                    continue;
                }
                String lectorKey = parts[2] + " " + parts[3];
                String[] tail = parts[4].split("\\s+", 2);
                String field = tail[0].toLowerCase();
                String newValue = tail.length > 1 ? tail[1] : "";

                try {
                    service.updateLector(lectorKey, field, newValue);
                    System.out.println("Lector updated.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if (line.matches("(?i)delete department .+")) {
                String deptKey = line.split("\\s+")[2];
                try {
                    service.deleteDepartment(deptKey);
                    System.out.println("Department deleted.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if (line.matches("(?i)delete lector .+")) {
                String lectorKey = line.split("\\s+")[2];
                try {
                    service.deleteLector(lectorKey);
                    System.out.println("Lector deleted.");
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if (line.matches("(?i)list departments")) {
                List<Department> depts = service.listDepartments();
                if (depts.isEmpty()) {
                    System.out.println("No departments found.");
                } else {
                    depts.forEach(d ->
                            System.out.printf("id=%d name=%s head=%s%n",
                                    d.getId(),
                                    d.getName(),
                                    d.getHead() != null
                                            ? d.getHead().getFirstName() + " " + d.getHead().getLastName()
                                            : "–")
                    );
                }

            } else if (line.matches("(?i)list lectors")) {
                List<Lector> lectors = service.listLectors();
                if (lectors.isEmpty()) {
                    System.out.println("No lectors found.");
                } else {
                    lectors.forEach(l ->
                            System.out.printf("id=%d %s %s %s salary=%.2f%n",
                                    l.getId(),
                                    l.getFirstName(),
                                    l.getLastName(),
                                    l.getDegree(),
                                    l.getSalary())
                    );
                }

            } else {
                System.out.println("Unknown command.");
            }
        }

        System.out.println("Goodbye!");
    }
}
