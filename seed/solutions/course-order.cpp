#include <functional>
#include <iostream>
#include <queue>
#include <string>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  int m = 0;
  std::cin >> n >> m;
  std::vector<std::vector<int>> next(n + 1);
  std::vector<int> indegree(n + 1, 0);
  for (int i = 0; i < m; ++i) {
    int a = 0;
    int b = 0;
    std::cin >> a >> b;
    next[a].push_back(b);
    ++indegree[b];
  }
  std::priority_queue<int, std::vector<int>, std::greater<>> ready;
  for (int course = 1; course <= n; ++course) {
    if (indegree[course] == 0) ready.push(course);
  }
  std::vector<int> order;
  order.reserve(n);
  while (!ready.empty()) {
    int course = ready.top();
    ready.pop();
    order.push_back(course);
    for (int after : next[course]) {
      if (--indegree[after] == 0) ready.push(after);
    }
  }
  if (static_cast<int>(order.size()) != n) {
    std::cout << -1 << '\n';
    return 0;
  }
  std::string out;
  for (int i = 0; i < n; ++i) {
    if (i > 0) out += ' ';
    out += std::to_string(order[i]);
  }
  out += '\n';
  std::cout << out;
}
