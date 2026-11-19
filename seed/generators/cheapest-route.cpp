#include <cstdio>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x524F555445ULL, mode.index);
  int n = 0;
  int m = 0;
  long long top = 1000000;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 6));
    m = static_cast<int>(rng.between(0, 10));
    top = 9;
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 3000));
    m = static_cast<int>(rng.between(n, 6000));
  } else {
    n = mode.index == 8 ? 25000 : 50000;
    m = 50000;
  }
  std::printf("%d %d\n", n, m);
  bool chain = !mode.small && mode.index >= 6 && mode.index != 8;
  for (int i = 0; i < m; ++i) {
    long long u = 0;
    long long v = 0;
    if (chain && i < n - 1) {
      u = n - 1 - i;
      v = n - i;
    } else if (!mode.small && !chain && i < n - 1) {
      u = rng.between(1, i + 1);
      v = i + 2;
    } else {
      u = rng.between(1, n);
      v = rng.between(1, n);
    }
    std::printf("%lld %lld %lld\n", u, v, rng.between(1, top));
  }
}
