#include <cstdio>
#include <vector>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x4255444745ULL, mode.index);
  int n = 0;
  long long top = 10000;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 8));
    top = 10;
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 3000));
  } else {
    n = 150000;
  }
  std::vector<long long> costs(n);
  long long total = 0;
  for (long long& cost : costs) {
    cost = rng.between(1, top);
    total += cost;
  }
  long long budget = 0;
  if (mode.small) {
    budget = rng.between(1, total + 2);
  } else if (mode.index <= 5) {
    budget = rng.between(1, total);
  } else if (mode.index == 6) {
    budget = total / 2;
  } else if (mode.index == 7) {
    budget = total / 3;
  } else {
    budget = top / 2;
  }
  std::printf("%d %lld\n", n, budget);
  for (int i = 0; i < n; ++i) std::printf("%lld%c", costs[i], i + 1 == n ? '\n' : ' ');
}
