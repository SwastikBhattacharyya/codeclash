#include <algorithm>
#include <iostream>
#include <string>
#include <utility>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  int m = 0;
  std::cin >> n >> m;
  std::vector<std::vector<std::pair<int, int>>> adjacent(n + 1);
  for (int edge = 1; edge <= m; ++edge) {
    int u = 0;
    int v = 0;
    std::cin >> u >> v;
    adjacent[u].push_back({v, edge});
    adjacent[v].push_back({u, edge});
  }
  std::vector<int> entered(n + 1, 0);
  std::vector<int> low(n + 1, 0);
  std::vector<int> via(n + 1, 0);
  std::vector<std::size_t> cursor(n + 1, 0);
  std::vector<int> bridges;
  int clock = 0;
  for (int root = 1; root <= n; ++root) {
    if (entered[root]) continue;
    std::vector<int> stack = {root};
    entered[root] = low[root] = ++clock;
    while (!stack.empty()) {
      int node = stack.back();
      if (cursor[node] < adjacent[node].size()) {
        auto [next, edge] = adjacent[node][cursor[node]++];
        if (edge == via[node]) continue;
        if (entered[next]) {
          low[node] = std::min(low[node], entered[next]);
        } else {
          via[next] = edge;
          entered[next] = low[next] = ++clock;
          stack.push_back(next);
        }
      } else {
        stack.pop_back();
        if (!stack.empty()) {
          int parent = stack.back();
          low[parent] = std::min(low[parent], low[node]);
          if (low[node] > entered[parent]) bridges.push_back(via[node]);
        }
      }
    }
  }
  std::sort(bridges.begin(), bridges.end());
  std::string out = std::to_string(bridges.size()) + '\n';
  for (int edge : bridges) {
    out += std::to_string(edge);
    out += '\n';
  }
  std::cout << out;
}
