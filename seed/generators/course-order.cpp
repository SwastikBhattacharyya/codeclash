#include <cstdio>
#include <numeric>
#include <vector>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x434F55525345ULL, mode.index);
  int n = 0;
  int m = 0;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 7));
    m = static_cast<int>(rng.between(0, 8));
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(2, 3000));
    m = static_cast<int>(rng.between(0, 3000));
  } else {
    n = 100000;
    m = 70000;
  }
  std::vector<int> rank(n);
  std::iota(rank.begin(), rank.end(), 1);
  rng.shuffle(rank);
  bool cyclic = mode.small ? rng.between(0, 3) == 0 : mode.index == 4 || mode.index == 7;
  bool chain = !mode.small && mode.index == 8;
  std::printf("%d %d\n", n, n < 2 ? 0 : m);
  if (n < 2) return 0;
  long long cycleFrom = rng.between(0, n - 2);
  long long cycleTo = rng.between(cycleFrom + 1, n - 1);
  for (int i = 0; i < m; ++i) {
    long long a = 0;
    long long b = 0;
    if (chain && i < n - 1) {
      a = n - i;
      b = n - i - 1;
    } else if (cyclic && m >= 2 && i + 2 == m) {
      a = rank[cycleFrom];
      b = rank[cycleTo];
    } else if (cyclic && m >= 2 && i + 1 == m) {
      a = rank[cycleTo];
      b = rank[cycleFrom];
    } else {
      long long x = rng.between(0, n - 2);
      long long y = rng.between(x + 1, n - 1);
      a = rank[x];
      b = rank[y];
    }
    std::printf("%lld %lld\n", a, b);
  }
}
