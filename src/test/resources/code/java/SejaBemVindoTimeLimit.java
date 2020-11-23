package code.java;

import java.util.Scanner;

public class SejaBemVindoTimeLimit{

    public static void main(String args[]) throws Exception{
        Scanner s = new Scanner(System.in);
        String name = s.nextLine();
        Thread.sleep(10000);;
        System.out.printf("Seja muito bem-vindo %s\n", name);
    }
}

