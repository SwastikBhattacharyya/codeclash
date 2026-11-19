#include <algorithm>
#include <iostream>
#include <vector>

std::vector<long long> costs;
std::vector<long long> values;

long long best(std::size_t index, long long left) {
  if (index == costs.size()) return 0;
  long long skip = best(index + 1, left);
  if (costs[index] > left) return skip;
  return std::max(skip, values[index] + best(index + 1, left - costs[index]));
}

int main() {
  int n = 0;
  long long budget = 0;
  std::cin >> n >> budget;
  costs.resize(n);
  values.resize(n);
  for (int i = 0; i < n; ++i) std::cin >> costs[i] >> values[i];
  std::cout << best(0, budget) << '\n';
}
