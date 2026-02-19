package com.team3;

import com.team3.service.IMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.Scanner;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    IMetric iMetric;

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
            System.out.println("1. run execution time test");
            System.out.println("2. run count if statement test");
            System.out.println("3. run cyclomatic complexity test");
            System.out.println("0. exit");
            System.out.print("enter: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {

                    System.out.println("execution time test");

                }
                case "2" -> {

                    System.out.println("count if statement test");

                }

                case "3" -> {

                    System.out.println("run cyclomatic complexity test");

                }
                case "0" -> {
                    System.out.println("exit");
                    System.exit(0);
                }
                default -> System.out.println("invalid option. please enter again");
            }

        }
    }
}