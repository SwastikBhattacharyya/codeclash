#include <iostream>
#include <numeric>
#include <string>
#include <utility>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  int q = 0;
  std::cin >> n >> q;
  std::vector<int> parent(n + 1);
  std::vector<int> size(n + 1, 1);
  std::iota(parent.begin(), parent.end(), 0);
  auto find = [&](int node) {
    while (parent[node] != node) {
      parent[node] = parent[parent[node]];
      node = parent[node];
    }
    return node;
  };
  std::string out;
  while (q-- > 0) {
    int type = 0;
    int u = 0;
    int v = 0;
    std::cin >> type >> u >> v;
    int a = find(u);
    int b = find(v);
    if (type == 1) {
      if (a == b) continue;
      if (size[a] < size[b]) std::swap(a, b);
      parent[b] = a;
      size[a] += size[b];
    } else {
      out += a == b ? "YES\n" : "NO\n";
    }
  }
  std::cout << out;
}
