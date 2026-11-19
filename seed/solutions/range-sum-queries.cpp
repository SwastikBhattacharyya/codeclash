#include <iostream>
#include <string>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  int q = 0;
  std::cin >> n >> q;
  std::vector<long long> prefix(n + 1, 0);
  for (int i = 1; i <= n; ++i) {
    long long value = 0;
    std::cin >> value;
    prefix[i] = prefix[i - 1] + value;
  }
  std::string out;
  while (q-- > 0) {
    int l = 0;
    int r = 0;
    std::cin >> l >> r;
    out += std::to_string(prefix[r] - prefix[l - 1]);
    out += '\n';
  }
  std::cout << out;
}
