#include <algorithm>
#include <iostream>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  std::cin >> n;
  std::vector<int> values(n);
  int largest = 1;
  for (int& value : values) {
    std::cin >> value;
    largest = std::max(largest, value);
  }
  std::vector<int> tree(largest + 1, 0);
  long long inversions = 0;
  for (int i = 0; i < n; ++i) {
    int seenAtMost = 0;
    for (int k = values[i]; k > 0; k -= k & -k) seenAtMost += tree[k];
    inversions += i - seenAtMost;
    for (int k = values[i]; k <= largest; k += k & -k) ++tree[k];
  }
  std::cout << inversions << '\n';
}
