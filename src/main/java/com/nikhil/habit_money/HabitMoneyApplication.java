package com.nikhil.habit_money;

import com.nikhil.habit_money.util.EnvironmentLoader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HabitMoneyApplication {

    public static void main(String[] args) {
        EnvironmentLoader.load();
        SpringApplication.run(HabitMoneyApplication.class, args);
    }

}
