#include <bits/stdc++.h>
using namespace std;
typedef pair<int,int> ii;
const int INF = 1e9;
const int MAX = 50000 + 10;
#define pb push_back
#define mp make_pair
#define sqr(x) ((x)*(x))
#define fi first
#define se second
#define SS stringstream
#define m0(x) memset(x, 0, sizeof x)
#define m1(x) memset(x, -1, sizeof x)
#define sz(x) (int)x.size()
typedef long long ll;
typedef pair<int, int> ii;
int cnt[MAX] = {0};
map<int, ii>idx;
vector<int>adj[MAX];
int h[MAX], par[MAX];
void dfs(int v, int p = -1){
    if(par + 1)
        h[v] = h[p] + 1;
    par[v] = p;
    for(int i = 0; i < adj[v].size(); i++){
        int u = adj[v][i];
        if(p - u) dfs(u, v);
    }
}
  
int LCA (int v, int u){
    if(v == u)
        return v;
    if(h[v] < h[u]) swap(v, u);
    return LCA(par[v], u);
}
  
int main(){
    int n, cur, u, v;
    scanf("%d", &n);
    for(int i = 1 ; i <=n; i++){
        scanf("%d", &cur);
        if(!cnt[cur])
            idx[cur].fi = i, cnt[cur]++;
        else
            idx[cur].se = i;
    }
    for(int i = 0 ; i < n-1; i++){
        scanf("%d %d", &u, &v);
        adj[u].pb(v);
        adj[v].pb(u);
    }
    dfs(1);
    int ans = 0;
    for(int i = 1; i <= n/2; i++){
        int k = LCA(idx[i].fi, idx[i].se);
        ans += (h[k]-h[idx[i].fi])+(h[k]-h[idx[i].se]);
    }
    // for(int i =1 ; i<=n; i++){
    //  printf("%d : %d = %d %d = %d \n", i, idx[i].fi,  h[idx[i].fi], idx[i].se ,h[idx[i].se]);
    // }
    printf("%d\n", abs(ans));
  
    return 0;
}