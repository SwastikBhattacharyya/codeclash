#include <algorithm>
#include <string>
#include <iostream>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  std::cin >> n;
  std::vector<int> tails;
  for (int i = 0; i < n; ++i) {
    int value = 0;
    std::cin >> value;
    auto position = std::lower_bound(tails.begin(), tails.end(), value);
    if (position == tails.end()) {
      tails.push_back(value);
    } else {
      *position = value;
    }
  }
  std::cout << tails.size() << '\n';
}
