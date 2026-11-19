#include <cstdio>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x4D454554494E4750ULL, mode.index);
  int n = 0;
  long long horizon = 1000000;
  long long max_length = 1000000;
  bool nested = false;
  bool chain = false;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 8));
    horizon = 20;
    max_length = 8;
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 2000));
    max_length = rng.between(1, horizon);
  } else if (mode.index == 6) {
    n = 50000;
    max_length = 200;
  } else if (mode.index == 7) {
    n = 50000;
    nested = true;
  } else {
    n = 50000;
    chain = true;
  }
  std::printf("%d\n", n);
  for (int i = 0; i < n; ++i) {
    long long start = 0;
    long long end = 0;
    if (nested) {
      start = rng.between(0, horizon / 2 - 1);
      end = horizon - rng.between(0, horizon / 2 - 1);
      if (rng.between(0, 9) == 0) end = start + rng.between(1, 3);
    } else if (chain) {
      long long step = horizon / n;
      start = i * step;
      end = start + step;
      if (i % 3 == 1) end = start + step + rng.between(1, step);
      if (end > horizon) end = horizon;
    } else {
      start = rng.between(0, horizon - 1);
      end = start + rng.between(1, max_length);
      if (end > horizon) end = horizon;
    }
    std::printf("%lld %lld\n", start, end);
  }
}
