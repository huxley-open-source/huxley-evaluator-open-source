#include<stdio.h>

int main()
{
    char s[300];
    while (1)
        gets(s);
    printf("Seja muito bem-vindo %s\n",s);
    return 0;
}