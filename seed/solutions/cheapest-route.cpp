#include <functional>
#include <iostream>
#include <queue>
#include <string>
#include <utility>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  int m = 0;
  std::cin >> n >> m;
  std::vector<std::vector<std::pair<int, long long>>> edges(n + 1);
  for (int i = 0; i < m; ++i) {
    int u = 0;
    int v = 0;
    long long w = 0;
    std::cin >> u >> v >> w;
    edges[u].emplace_back(v, w);
  }
  const long long unreachable = -1;
  std::vector<long long> distance(n + 1, unreachable);
  std::priority_queue<std::pair<long long, int>, std::vector<std::pair<long long, int>>, std::greater<>> heap;
  distance[1] = 0;
  heap.emplace(0, 1);
  while (!heap.empty()) {
    auto [d, node] = heap.top();
    heap.pop();
    if (d != distance[node]) continue;
    for (auto [next, w] : edges[node]) {
      long long candidate = d + w;
      if (distance[next] == unreachable || candidate < distance[next]) {
        distance[next] = candidate;
        heap.emplace(candidate, next);
      }
    }
  }
  std::string out;
  for (int node = 1; node <= n; ++node) {
    if (node > 1) out += ' ';
    out += std::to_string(distance[node]);
  }
  out += '\n';
  std::cout << out;
}
