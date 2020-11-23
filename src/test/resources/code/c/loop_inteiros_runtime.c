/*
Problema 20
*/
#include <stdio.h>

int main() 
{
	int i, j=0;
	int *p = NULL;
	for (j=1; j<=1000; ++j)
	{
	    printf("%d\n",j);
	    p[4000+i++] = 2*i + j;
	}

}