#include <algorithm>
#include <iostream>
#include <string>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  int q = 0;
  std::cin >> n >> q;
  std::vector<int> log(n + 1, 0);
  for (int i = 2; i <= n; ++i) log[i] = log[i / 2] + 1;
  std::vector<std::vector<int>> table(log[n] + 1, std::vector<int>(n));
  for (int i = 0; i < n; ++i) std::cin >> table[0][i];
  for (int level = 1; level <= log[n]; ++level) {
    for (int i = 0; i + (1 << level) <= n; ++i) {
      table[level][i] = std::min(table[level - 1][i], table[level - 1][i + (1 << (level - 1))]);
    }
  }
  std::string out;
  while (q-- > 0) {
    int type = 0;
    int a = 0;
    int b = 0;
    std::cin >> type >> a >> b;
    if (type == 1) {
      table[0][a - 1] = b;
    } else {
      int level = log[b - a + 1];
      out += std::to_string(std::min(table[level][a - 1], table[level][b - (1 << level)]));
      out += '\n';
    }
  }
  std::cout << out;
}
