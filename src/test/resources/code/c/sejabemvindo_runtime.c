#include<stdio.h>

int main()
{
    char s[300];
    int b = 1/0;
    int *p = NULL;
    for (b=0; b < 3000; ++b)
    {
        *p = b;
    }

    gets(s);
    printf("Seja muito bem-vindo %s %d\n",s,b);
    return 0;
}