#include <iostream>
#include <vector>

int main() {
  int n = 0;
  long long k = 0;
  std::cin >> n >> k;
  std::vector<long long> values(n);
  for (long long& value : values) std::cin >> value;
  long long pairs = 0;
  for (int i = 0; i < n; ++i) {
    for (int j = i + 1; j < n; ++j) {
      if (values[i] + values[j] == k) ++pairs;
    }
  }
  std::cout << pairs << '\n';
}
