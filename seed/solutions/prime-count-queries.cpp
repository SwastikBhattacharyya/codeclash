#include <algorithm>
#include <iostream>
#include <string>
#include <utility>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int q = 0;
  std::cin >> q;
  std::vector<std::pair<int, int>> queries(q);
  int limit = 1;
  for (auto& [l, r] : queries) {
    std::cin >> l >> r;
    limit = std::max(limit, r);
  }
  std::vector<char> composite(limit + 1, 0);
  composite[0] = 1;
  composite[1] = 1;
  for (long long i = 2; i * i <= limit; ++i) {
    if (composite[i]) continue;
    for (long long j = i * i; j <= limit; j += i) composite[j] = 1;
  }
  std::vector<int> count(limit + 1, 0);
  for (int i = 1; i <= limit; ++i) count[i] = count[i - 1] + (composite[i] ? 0 : 1);
  std::string out;
  for (const auto& [l, r] : queries) {
    out += std::to_string(count[r] - count[l - 1]);
    out += '\n';
  }
  std::cout << out;
}
