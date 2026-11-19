#include <algorithm>
#include <iostream>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  int budget = 0;
  std::cin >> n >> budget;
  std::vector<long long> best(budget + 1, 0);
  for (int i = 0; i < n; ++i) {
    int cost = 0;
    long long value = 0;
    std::cin >> cost >> value;
    for (int spent = cost; spent <= budget; ++spent) {
      best[spent] = std::max(best[spent], best[spent - cost] + value);
    }
  }
  std::cout << best[budget] << '\n';
}
