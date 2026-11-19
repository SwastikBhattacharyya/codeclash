#include <algorithm>
#include <climits>
#include <iostream>
#include <numeric>
#include <vector>

int main() {
  int n = 0;
  std::cin >> n;
  std::vector<std::vector<long long>> distance(n, std::vector<long long>(n));
  for (auto& row : distance) {
    for (long long& cell : row) std::cin >> cell;
  }
  std::vector<int> order(n);
  std::iota(order.begin(), order.end(), 0);
  long long answer = LLONG_MAX;
  do {
    long long total = 0;
    for (int i = 0; i < n; ++i) total += distance[order[i]][order[(i + 1) % n]];
    answer = std::min(answer, total);
  } while (std::next_permutation(order.begin() + 1, order.end()));
  std::cout << answer << '\n';
}
