#include <algorithm>
#include <iostream>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  long long budget = 0;
  std::cin >> n >> budget;
  std::vector<long long> costs(n);
  for (long long& cost : costs) std::cin >> cost;
  long long sum = 0;
  int best = 0;
  int left = 0;
  for (int right = 0; right < n; ++right) {
    sum += costs[right];
    while (sum > budget) sum -= costs[left++];
    best = std::max(best, right - left + 1);
  }
  std::cout << best << '\n';
}
