package code.java; // esse pacote deve ser ignorado e não causar um erro de compilação

import java.util.Scanner;

public class SejaBemVindoCorrect2{

    public static void main(String args[]){
        Scanner s = new Scanner(System.in);
        String name = s.nextLine();
        System.out.printf("Seja muito bem-vindo %s\n", name);

    }
}

