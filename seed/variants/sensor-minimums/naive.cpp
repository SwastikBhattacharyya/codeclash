#include <algorithm>
#include <iostream>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  int q = 0;
  std::cin >> n >> q;
  std::vector<int> values(n);
  for (int& value : values) std::cin >> value;
  while (q-- > 0) {
    int type = 0;
    int a = 0;
    int b = 0;
    std::cin >> type >> a >> b;
    if (type == 1) {
      values[a - 1] = b;
    } else {
      std::cout << *std::min_element(values.begin() + (a - 1), values.begin() + b) << '\n';
    }
  }
}
