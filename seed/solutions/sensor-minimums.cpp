#include <algorithm>
#include <climits>
#include <iostream>
#include <string>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  int q = 0;
  std::cin >> n >> q;
  std::vector<int> tree(2 * n, INT_MAX);
  for (int i = 0; i < n; ++i) std::cin >> tree[n + i];
  for (int i = n - 1; i >= 1; --i) tree[i] = std::min(tree[2 * i], tree[2 * i + 1]);
  std::string out;
  while (q-- > 0) {
    int type = 0;
    int a = 0;
    int b = 0;
    std::cin >> type >> a >> b;
    if (type == 1) {
      int at = n + a - 1;
      tree[at] = b;
      for (at /= 2; at >= 1; at /= 2) tree[at] = std::min(tree[2 * at], tree[2 * at + 1]);
    } else {
      int low = INT_MAX;
      for (int l = n + a - 1, r = n + b; l < r; l /= 2, r /= 2) {
        if (l & 1) low = std::min(low, tree[l++]);
        if (r & 1) low = std::min(low, tree[--r]);
      }
      out += std::to_string(low);
      out += '\n';
    }
  }
  std::cout << out;
}
