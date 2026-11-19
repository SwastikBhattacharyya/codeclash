#include <iostream>
#include <string>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  std::string text;
  std::string pattern;
  std::cin >> text >> pattern;
  std::size_t m = pattern.size();
  std::vector<std::size_t> border(m, 0);
  for (std::size_t i = 1; i < m; ++i) {
    std::size_t k = border[i - 1];
    while (k > 0 && pattern[i] != pattern[k]) k = border[k - 1];
    if (pattern[i] == pattern[k]) ++k;
    border[i] = k;
  }
  long long count = 0;
  std::size_t matched = 0;
  for (char ch : text) {
    while (matched > 0 && ch != pattern[matched]) matched = border[matched - 1];
    if (ch == pattern[matched]) ++matched;
    if (matched == m) {
      ++count;
      matched = border[m - 1];
    }
  }
  std::cout << count << '\n';
}
