#include <iostream>
#include <vector>

int main() {
  int n = 0;
  int q = 0;
  std::cin >> n >> q;
  std::vector<int> parent(n + 1, 0);
  std::vector<std::vector<int>> children(n + 1);
  for (int child = 2; child <= n; ++child) {
    std::cin >> parent[child];
    children[parent[child]].push_back(child);
  }
  std::vector<int> depth(n + 1, 0);
  std::vector<int> order = {1};
  for (std::size_t i = 0; i < order.size(); ++i) {
    for (int child : children[order[i]]) {
      depth[child] = depth[order[i]] + 1;
      order.push_back(child);
    }
  }
  while (q-- > 0) {
    int u = 0;
    int v = 0;
    std::cin >> u >> v;
    while (depth[u] > depth[v]) u = parent[u];
    while (depth[v] > depth[u]) v = parent[v];
    while (u != v) {
      u = parent[u];
      v = parent[v];
    }
    std::cout << u << '\n';
  }
}
