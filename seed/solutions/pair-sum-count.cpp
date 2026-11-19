#include <iostream>
#include <unordered_map>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  long long k = 0;
  std::cin >> n >> k;
  std::unordered_map<long long, long long> seen;
  seen.reserve(static_cast<std::size_t>(n) * 2);
  long long pairs = 0;
  for (int i = 0; i < n; ++i) {
    long long value = 0;
    std::cin >> value;
    auto match = seen.find(k - value);
    if (match != seen.end()) pairs += match->second;
    ++seen[value];
  }
  std::cout << pairs << '\n';
}
