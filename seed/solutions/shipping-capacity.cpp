#include <algorithm>
#include <iostream>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  long long days = 0;
  std::cin >> n >> days;
  std::vector<long long> weights(n);
  long long low = 0;
  long long high = 0;
  for (long long& weight : weights) {
    std::cin >> weight;
    low = std::max(low, weight);
    high += weight;
  }
  auto fits = [&](long long capacity) {
    long long used = 1;
    long long load = 0;
    for (long long weight : weights) {
      if (load + weight > capacity) {
        ++used;
        load = 0;
      }
      load += weight;
    }
    return used <= days;
  };
  while (low < high) {
    long long middle = low + (high - low) / 2;
    if (fits(middle)) {
      high = middle;
    } else {
      low = middle + 1;
    }
  }
  std::cout << low << '\n';
}
