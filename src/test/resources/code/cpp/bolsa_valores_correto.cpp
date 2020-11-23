#include <bits/stdc++.h>
using namespace std;
typedef pair<int,int> ii;
const int INF = 1e9;
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
const int MAX = 2* 1e5 + 1;
int n, c;
int a[MAX];
int DP[MAX][2];
int dp(int id, int st){
	if(id >= n) return 0;
	if(DP[id][st] != -1) return DP[id][st];
	if(!st)return DP[id][st] = max(dp(id+1, st), dp(id+1, 1-st) -c -a[id]);
	return DP[id][st] = max(dp(id+1, st), dp(id+1, 1-st) + a[id]);
}
int main(){
	
	scanf("%d %d", &n, &c);
	for(int i = 0; i < n; i++)scanf("%d", a+i);
	m1(DP);
	printf("%d\n", dp(0, 0));
}