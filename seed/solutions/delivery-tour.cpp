#include <algorithm>
#include <climits>
#include <iostream>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  std::cin >> n;
  std::vector<std::vector<long long>> distance(n, std::vector<long long>(n));
  for (auto& row : distance) {
    for (long long& cell : row) std::cin >> cell;
  }
  int full = 1 << n;
  std::vector<std::vector<long long>> best(full, std::vector<long long>(n, LLONG_MAX));
  best[1][0] = 0;
  for (int mask = 1; mask < full; ++mask) {
    if (!(mask & 1)) continue;
    for (int last = 0; last < n; ++last) {
      if (best[mask][last] == LLONG_MAX) continue;
      for (int next = 0; next < n; ++next) {
        if (mask >> next & 1) continue;
        int grown = mask | 1 << next;
        best[grown][next] = std::min(best[grown][next], best[mask][last] + distance[last][next]);
      }
    }
  }
  long long answer = LLONG_MAX;
  for (int last = 0; last < n; ++last) {
    if (best[full - 1][last] != LLONG_MAX) answer = std::min(answer, best[full - 1][last] + distance[last][0]);
  }
  std::cout << answer << '\n';
}
