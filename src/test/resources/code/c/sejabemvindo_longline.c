#include<stdio.h>

int main()
{
    char s[300];
    int i;
//    gets(s);
//    printf("Seja muito bem-vindo %s",s);
    for (i = 0; i <  500 * 1024; ++i) {
        printf("A");
    }
    printf("\n");
    return 0;
}