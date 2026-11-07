package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("\n--- Starting Coffee Shop System ---");

        DatabaseInitializer dbInitializer = new DatabaseInitializer();
        dbInitializer.initialize();
        System.out.println("... Database and tables verified/initialized.");

        UserDao userDao = new UserDao();
        Scanner scanner = new Scanner(System.in);

        Authentication auth = new Authentication(userDao, scanner);
        System.out.println("... Handover to Authentication Module.\n");

        auth.start();
    }
}