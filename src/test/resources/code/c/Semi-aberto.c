#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int M, L, op=0, qt[1000000];

typedef struct jazigo {
int stat;
unsigned int k;
}tum;

int hash (int K) {
return (K%M);
}

tum** adic (tum **bld, int h, int K) {
    int i, j;
    for (i=h;i<M;i++) {
            for (j=0;j<L;j++) {
                if (bld[i][j].stat == 0) {
                    bld[i][j].stat = 1;
                    bld[i][j].k = K;
                    qt[i]+=1;
                    return bld;
                }
            }
    }
    for (i=0;i<h;i++) {
            for (j=0;j<L;j++) {
                if (bld[i][j].stat == 0) {
                    bld[i][j].stat = 1;
                    bld[i][j].k = K;
                    qt[i]+=1;
                    return bld;
                }
            }
    }
}

tum **realoca (tum **bld, int k){
    //bld = (tum**) realloc(bld, M*sizeof(tum*));
    int i;
    int j;
    //reallocando prédio novo e criando temporário;
    tum **temp;
    temp = (tum**) calloc (M, sizeof(tum*));
    for (i=0;i<M;i++) {
        temp[i] = (tum*) calloc (L,sizeof(tum));
        qt[i]=0;
    }

    //realocando predio temporario
    int h;
    for (i=0;i<k;i++) {
            for (j=0;j<L;j++) {
                if (bld[i][j].stat == 1) {
                    h = hash(bld[i][j].k);
                    temp=adic(temp, h, bld[i][j].k);
                }
            }
    }
    //jazigos no predio novo
    free(bld);
    bld=NULL;
    bld=temp;

    return bld;
}

tum** adc (tum **bld, int h, int K) {
int i, j;
for (i=h;i<M;i++) {
 if(qt[i]<L) {
    for (j=0;j<L;j++) {
        if (bld[i][j].stat == 0) {
            bld[i][j].stat = 1;
            bld[i][j].k = K;
            qt[i]+=1;
            printf("%d %d.%d\n", op, i,j);
            return bld;
        }
    }
 }
}
for (i=0;i<h;i++) {
    if (qt[i]<L) {
        for (j=0;j<L;j++) {
            if (bld[i][j].stat == 0) {
                bld[i][j].stat = 1;
                bld[i][j].k = K;
                qt[i]+=1;
                printf("%d %d.%d\n", op, i,j);
                return bld;
            }
        }
    }
}
    int k=M;
    M = (2*M)+1;
    bld=realoca(bld, k);
    h = hash(K);
    for (i=h;i<M;i++) {
        if (qt[i]<L) {
            for (j=0;j<L;j++) {
                if (bld[i][j].stat == 0) {
                    bld[i][j].stat = 1;
                    bld[i][j].k = K;
                    qt[i]+=1;
                    printf("%d %d.%d\n", op, i, j);
                    return bld;
                }
            }
        }
    }
    for (i=0;i<h;i++) {
        if (qt[i]<L) {
            for (j=0;j<L;j++) {
                if (bld[i][j].stat == 0) {
                    bld[i][j].stat = 1;
                    bld[i][j].k = K;
                    qt[i]+=1;
                    printf("%d %d.%d\n", op, i, j);
                    return bld;
                }
            }
        }
    }
return bld;
}

void rmve (tum **bld, int h, int K) {
int i, j;
for (i=h;i<M;i++) {
    if (qt[i]!=0) {
        for (j=0;j<L;j++) {
            if (bld[i][j].stat == 0) {
                printf("%d ?.?\n", op);
                return;// bld;
            } else if (bld[i][j].k==K) {
                if (bld[i][j].stat==1) {
                    bld[i][j].stat=2;
                    printf("%d %d.%d\n", op, i,j);
                    return;// bld;
                } else {
                    printf("%d ?.?\n", op);
                    return;// bld;
                }
            }
        }
    } else {
        printf("%d ?.?\n", op);
        return;
    }
}
for (i=0;i<h;i++) {
    if (qt[i]!=0) {
        for (j=0;j<L;j++) {
            if (bld[i][j].stat == 0) {
                printf("%d ?.?\n", op);
                return;// bld;
            } else if (bld[i][j].k==K) {
                if (bld[i][j].stat==1) {
                    bld[i][j].stat=2;
                    printf("%d %d.%d\n", op, i,j);
                    return;// bld;
                } else {
                    printf("%d ?.?\n", op);
                    return;// bld;
                }
            }
        }
    } else {
        printf("%d ?.?\n", op);
        return;
    }
}
printf("%d ?.?\n", op);
return;// bld;

}

void qry (tum **bld, int h, int K) {
int i, j;
for (i=h;i<M;i++) {
    if (qt[i]!=0) {
        for (j=0;j<L;j++) {
            if (bld[i][j].stat == 0) {
                printf("%d ?.?\n", op);
                return;// bld;
            } else if (bld[i][j].k==K) {
                if (bld[i][j].stat==1) {
                    printf("%d %d.%d\n", op, i,j);
                    return;// bld;
                } else {
                    printf("%d ?.?\n", op);
                    return;// bld;
                }
            }
        }
    } else {
        printf("%d ?.?\n", op);
        return;
    }
}
for (i=0;i<h;i++) {
    if (qt[i]!=0) {
        for (j=0;j<L;j++) {
            if (bld[i][j].stat == 0) {
                printf("%d ?.?\n", op);
                return;// bld;
            } else if (bld[i][j].k==K) {
                if (bld[i][j].stat==1) {
                    printf("%d %d.%d\n", op, i,j);
                    return;// bld;
                } else {
                    printf("%d ?.?\n", op);
                    return;// bld;
                }
            }
        }
    } else {
        printf("%d ?.?\n", op);
        return;
    }
}
printf("%d ?.?\n", op);
return;
}

int main() {
int K;
scanf("%d %d", &M, &L);
tum **bld;
//Aloca
bld = (tum**) calloc(M, sizeof(tum*));
int i;
for (i=0;i<M;i++) {
    bld[i] = (tum*) calloc(L, sizeof(tum));
}
//Fim aloca

char ADD[4] = "ADD", REM[4] = "REM", QRY[4]="QRY", OP[4];
int h;
while (scanf("%s %u", OP, &K)!=EOF) {
    if (K<0 || K>1048576) {
        return 0;
        
    }
    h=hash(K);
    if (OP[0]=='A') {
        bld=adc(bld, h, K);
        op+=1;

    } else if (OP[0]=='R') {
        rmve(bld, h, K);
        op+=1;

    } else if (OP[0]=='Q') {
        qry(bld, h, K);
        op+=1;
    }
    if (op+1 == 131072) {
        break;
        
    }
}
free(bld);
bld = NULL;
return 0;
}
