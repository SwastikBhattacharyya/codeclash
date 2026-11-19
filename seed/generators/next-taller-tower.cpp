#include <cstdio>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x544F57455253ULL, mode.index);
  int n = 0;
  long long top = 100000;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 8));
    top = 5;
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 3000));
    top = mode.index % 2 == 0 ? 50 : 100000;
  } else {
    n = 120000;
  }
  std::printf("%d\n", n);
  for (int i = 0; i < n; ++i) {
    long long height = 0;
    if (!mode.small && mode.index == 6) {
      height = top - static_cast<long long>(i) * (top - 1) / n;
    } else if (!mode.small && mode.index == 7) {
      height = i < n / 2 ? top - static_cast<long long>(i) * (top - 1) / n : rng.between(1, top);
    } else {
      height = rng.between(1, top);
    }
    std::printf("%lld%c", height, i + 1 == n ? '\n' : ' ');
  }
}
