#include <algorithm>
#include <iostream>
#include <utility>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int n = 0;
  std::cin >> n;
  std::vector<std::pair<int, int>> meetings(n);
  for (auto& [end, start] : meetings) std::cin >> start >> end;
  std::sort(meetings.begin(), meetings.end());
  int picked = 0;
  int free_at = 0;
  for (const auto& [end, start] : meetings) {
    if (start >= free_at) {
      ++picked;
      free_at = end;
    }
  }
  std::cout << picked << '\n';
}
