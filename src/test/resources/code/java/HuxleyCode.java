import java.util.Scanner;

/*
Problema 324
 */
public class HuxleyCode {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		double soma=0;
		int cont=0;
		int idades=0;
		do{
			idades = in.nextInt();
			if(idades >=0){
				cont++;
				soma = idades + soma;
			}
		}while(idades >= 0);
		in.close();
		double media =  soma/cont;
		System.out.printf("%.2f\n", media);
	}
}

