#include <iostream>
#include <string>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  std::cin >> n;
  std::vector<int> heights(n);
  for (int& height : heights) std::cin >> height;
  std::vector<int> answer(n, 0);
  std::vector<int> stack;
  for (int i = n - 1; i >= 0; --i) {
    while (!stack.empty() && heights[stack.back()] <= heights[i]) stack.pop_back();
    answer[i] = stack.empty() ? 0 : stack.back() + 1;
    stack.push_back(i);
  }
  std::string out;
  for (int i = 0; i < n; ++i) {
    if (i > 0) out += ' ';
    out += std::to_string(answer[i]);
  }
  out += '\n';
  std::cout << out;
}
