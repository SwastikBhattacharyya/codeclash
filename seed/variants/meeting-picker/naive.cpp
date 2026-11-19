#include <algorithm>
#include <iostream>
#include <utility>
#include <vector>

int main() {
  int n = 0;
  std::cin >> n;
  std::vector<std::pair<long long, long long>> meetings(n);
  for (auto& [end, start] : meetings) std::cin >> start >> end;
  std::sort(meetings.begin(), meetings.end());
  std::vector<int> chain(n, 1);
  int best = 0;
  for (int i = 0; i < n; ++i) {
    for (int j = 0; j < i; ++j) {
      if (meetings[j].first <= meetings[i].second && chain[j] + 1 > chain[i]) chain[i] = chain[j] + 1;
    }
    best = std::max(best, chain[i]);
  }
  std::cout << best << '\n';
}
