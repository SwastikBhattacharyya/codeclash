#include <iostream>
#include <utility>
#include <vector>

int main() {
  int n = 0;
  int m = 0;
  std::cin >> n >> m;
  std::vector<std::pair<int, int>> edges(m + 1);
  std::vector<std::vector<std::pair<int, int>>> adjacent(n + 1);
  for (int edge = 1; edge <= m; ++edge) {
    std::cin >> edges[edge].first >> edges[edge].second;
    adjacent[edges[edge].first].push_back({edges[edge].second, edge});
    adjacent[edges[edge].second].push_back({edges[edge].first, edge});
  }
  std::vector<int> bridges;
  for (int removed = 1; removed <= m; ++removed) {
    std::vector<bool> seen(n + 1, false);
    std::vector<int> queue = {edges[removed].first};
    seen[edges[removed].first] = true;
    for (std::size_t i = 0; i < queue.size(); ++i) {
      for (auto [next, edge] : adjacent[queue[i]]) {
        if (edge == removed || seen[next]) continue;
        seen[next] = true;
        queue.push_back(next);
      }
    }
    if (!seen[edges[removed].second]) bridges.push_back(removed);
  }
  std::cout << bridges.size() << '\n';
  for (int edge : bridges) std::cout << edge << '\n';
}
