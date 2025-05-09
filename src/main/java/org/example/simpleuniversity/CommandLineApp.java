package org.example.simpleuniversity;

import lombok.AllArgsConstructor;
import org.example.simpleuniversity.config.DataInitializer;
import org.example.simpleuniversity.model.Degree;
import org.example.simpleuniversity.model.Department;
import org.example.simpleuniversity.model.Lector;
import org.example.simpleuniversity.service.UniversityService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@AllArgsConstructor
@SpringBootApplication
public class CommandLineApp implements CommandLineRunner {
    private final UniversityService service;

    public static void main(String[] args) {
        SpringApplication.run(CommandLineApp.class, args);
    }

    @Override
    public void run(String... args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter command (or 'exit'):");

        while (true) {
            String line = in.nextLine().trim();
            if ("exit".equalsIgnoreCase(line)) break;

            try {
                if (line.matches("(?i)who is head of department .+")) {
                    String dept = line.replaceAll("(?i)who is head of department ", "");
                    System.out.printf("Head of %s department is %s%n",
                            dept, service.getHeadOf(dept));

                } else if (line.matches("(?i)show .+ statistics\\.?")) {
                    String dept = line.replaceAll("(?i)show (.+) statistics\\.?","$1");
                    Map<?,Long> stats = service.getStatistics(dept);
                    System.out.printf("assistants - %d%n"  +
                                    "associate professors - %d%n" +
                                    "professors - %d%n",
                            stats.getOrDefault(Degree.ASSISTANT, 0L),
                            stats.getOrDefault(Degree.ASSOCIATE_PROFESSOR, 0L),
                            stats.getOrDefault(Degree.PROFESSOR, 0L)
                    );
                } else if (line.matches("(?i)show the average salary for the department .+")) {
                    String dept = line.replaceAll("(?i)show the average salary for the department ", "");
                    System.out.printf("The average salary of %s is %.2f%n",
                            dept, service.getAverageSalary(dept));
                } else if (line.matches("(?i)show count of employee for .+")) {
                    String dept = line.replaceAll("(?i)show count of employee for ", "");
                    System.out.println(service.getEmployeeCount(dept));
                } else if (line.matches("(?i)global search by .+")) {
                    String tmpl = line.replaceAll("(?i)global search by ", "");
                    System.out.println(service.globalSearch(tmpl));
                } else if (line.matches("(?i)add department .+ head .+")) {
                    String[] parts = line.split("\\s+");
                    String name = parts[2];
                    String headKey = parts[4];
                    service.createDepartment(name, headKey);
                    System.out.println("Department created.");
                } else if (line.toLowerCase().startsWith("add lector ")) {
                    String[] tokens = line.split("\\s+");
                    String firstName = tokens[2];
                    String lastName  = tokens[3];
                    String degree    = tokens[5];
                    double salary    = Double.parseDouble(tokens[7]);

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

                    service.createLector(
                            firstName,
                            lastName,
                            Degree.valueOf(degree.toUpperCase()),
                            salary,
                            deptKeys
                    );
                    System.out.println("Lector created.");
                } else if (line.matches("(?i)update department .+ head .+")) {
                    String[] parts = line.split("\\s+");
                    String deptKey   = parts[2];
                    String newHeadKey = parts[4];
                    service.updateDepartmentHead(deptKey, newHeadKey);
                    System.out.println("Department head updated.");
                } else if (line.toLowerCase().startsWith("update lector ")) {
                String[] parts = line.split("\\s+", 5);
                if (parts.length < 5) {
                    System.out.println("Invalid command format.");
                    continue;
                }

                String lectorKey = parts[2] + " " + parts[3];

                String[] tail = parts[4].split("\\s+", 2);
                String field    = tail[0].toLowerCase();

                String newValue = tail.length > 1 ? tail[1] : "";

                service.updateLector(lectorKey, field, newValue);
                System.out.println("Lector updated.");
            }
            else if (line.matches("(?i)delete department .+")) {
                    String deptKey = line.split("\\s+")[2];
                    service.deleteDepartment(deptKey);
                    System.out.println("Department deleted.");
            } else if (line.matches("(?i)delete lector .+")) {
                    String lectorKey = line.split("\\s+")[2];
                    service.deleteLector(lectorKey);
                    System.out.println("Lector deleted.");
            } else if (line.matches("(?i)list departments")) {
                    service.listDepartments().forEach(d ->
                            System.out.printf("id=%d name=%s head=%s%n",
                                    d.getId(),
                                    d.getName(),
                                    d.getHead() != null
                                            ? d.getHead().getFirstName() + " " + d.getHead().getLastName()
                                            : "–")
                    );
            } else if (line.matches("(?i)list lectors")) {
                    service.listLectors().forEach(l ->
                            System.out.printf("id=%d %s %s %s salary=%.2f%n",
                                    l.getId(),
                                    l.getFirstName(),
                                    l.getLastName(),
                                    l.getDegree(),
                                    l.getSalary())
                    );
            } else {
                    System.out.println("Unknown command.");
            }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.println("Goodbye!");
    }
}
