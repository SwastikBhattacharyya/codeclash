#include <iostream>
#include <string>
#include <utility>
#include <vector>

constexpr int Levels = 17;

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  int q = 0;
  std::cin >> n >> q;
  std::vector<std::vector<int>> up(Levels, std::vector<int>(n + 1, 1));
  std::vector<std::vector<int>> children(n + 1);
  for (int child = 2; child <= n; ++child) {
    std::cin >> up[0][child];
    children[up[0][child]].push_back(child);
  }
  std::vector<int> depth(n + 1, 0);
  std::vector<int> order = {1};
  for (std::size_t i = 0; i < order.size(); ++i) {
    for (int child : children[order[i]]) {
      depth[child] = depth[order[i]] + 1;
      order.push_back(child);
    }
  }
  for (int level = 1; level < Levels; ++level) {
    for (int node = 1; node <= n; ++node) up[level][node] = up[level - 1][up[level - 1][node]];
  }
  std::string out;
  while (q-- > 0) {
    int u = 0;
    int v = 0;
    std::cin >> u >> v;
    if (depth[u] < depth[v]) std::swap(u, v);
    int climb = depth[u] - depth[v];
    for (int level = 0; level < Levels; ++level) {
      if (climb >> level & 1) u = up[level][u];
    }
    if (u != v) {
      for (int level = Levels - 1; level >= 0; --level) {
        if (up[level][u] != up[level][v]) {
          u = up[level][u];
          v = up[level][v];
        }
      }
      u = up[0][u];
    }
    out += std::to_string(u);
    out += '\n';
  }
  std::cout << out;
}
