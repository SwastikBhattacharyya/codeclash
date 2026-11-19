#include <iostream>
#include <string>
#include <vector>

constexpr long long Modulus = 1000000007;

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  int m = 0;
  std::cin >> n >> m;
  std::vector<long long> ways(m, 0);
  for (int row = 0; row < n; ++row) {
    std::string line;
    std::cin >> line;
    for (int col = 0; col < m; ++col) {
      if (line[col] == '#') {
        ways[col] = 0;
      } else if (row == 0 && col == 0) {
        ways[col] = 1;
      } else if (col > 0) {
        ways[col] = (ways[col] + ways[col - 1]) % Modulus;
      }
    }
  }
  std::cout << ways[m - 1] << '\n';
}
