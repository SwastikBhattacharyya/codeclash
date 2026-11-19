#include <cstdio>
#include <vector>

#include "rng.hpp"

constexpr long long MaxDistance = 1000000;

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x44454C4956455259ULL, mode.index);
  int n = 0;
  long long limit = MaxDistance;
  bool trap = false;
  bool lopsided = false;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 6));
    limit = 20;
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 16));
    limit = rng.between(1, MaxDistance);
  } else if (mode.index == 6) {
    n = 16;
  } else if (mode.index == 7) {
    n = 16;
    trap = true;
  } else {
    n = 16;
    lopsided = true;
  }
  std::vector<std::vector<long long>> distance(n, std::vector<long long>(n, 0));
  for (int i = 0; i < n; ++i) {
    for (int j = 0; j < n; ++j) {
      if (i == j) continue;
      if (trap) {
        distance[i][j] = j == i + 1 ? rng.between(1, 10) : rng.between(MaxDistance / 2, MaxDistance);
        if (j == 0) distance[i][j] = i == n - 1 ? MaxDistance : rng.between(MaxDistance / 2, MaxDistance);
      } else if (lopsided) {
        distance[i][j] = i < j ? rng.between(0, 100) : rng.between(MaxDistance - 100, MaxDistance);
      } else {
        distance[i][j] = rng.between(0, limit);
      }
    }
  }
  std::printf("%d\n", n);
  for (int i = 0; i < n; ++i) {
    for (int j = 0; j < n; ++j) std::printf("%lld%c", distance[i][j], j + 1 == n ? '\n' : ' ');
  }
}
