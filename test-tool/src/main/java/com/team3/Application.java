package com.team3;

import com.team3.dynamicMetric.service.RunTimeTestService;
import com.team3.staticMetric.entity.TestCase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private final List<TestCase> testCases;

    public Application(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("----------------------------------------");
        System.out.println("   Welcome to our CLI project");
        System.out.println("----------------------------------------");

        while (true) {
            System.out.println();
            System.out.println("Please choose your option:");
            System.out.println("0. exit");
            System.out.println("1. dynamic metric entry");
            for (int i = 0; i < testCases.size(); i++) {
                System.out.println((i + 2) + ". " + testCases.get(i).getDisplayName());
            }
            System.out.print("enter: ");

            String input = scanner.nextLine().trim();

            if ("0".equals(input)) {
                System.out.println("exit");
                System.exit(0);
            }

            int index;
            try {
                index = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("invalid option. please enter again");
                continue;
            }

            if (index == 1) {
                System.out.println("Dynamic metric entry (reserved).");
                RunTimeTestService runTimeTestService = new RunTimeTestService();
                 runTimeTestService.testLoginThroughput();
            } else if (index >= 2 && index <= testCases.size() + 1) {
                testCases.get(index - 2).run();
            } else {
                System.out.println("invalid option. please enter again");
            }
        }
    }
}
