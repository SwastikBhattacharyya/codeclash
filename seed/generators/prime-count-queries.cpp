#include <algorithm>
#include <cstdio>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x5052494D4553ULL, mode.index);
  const long long top = 10000000;
  int q = 0;
  long long range = top;
  bool wide = false;
  if (mode.small) {
    q = static_cast<int>(rng.between(1, 8));
    range = 60;
  } else if (mode.index <= 5) {
    q = static_cast<int>(rng.between(1, 2000));
    range = mode.index % 2 == 0 ? 100000 : top;
  } else {
    q = 50000;
    wide = true;
  }
  std::printf("%d\n", q);
  for (int i = 0; i < q; ++i) {
    long long l = 0;
    long long r = 0;
    if (wide) {
      l = rng.between(1, 1000);
      r = rng.between(top - 1000, top);
    } else {
      l = rng.between(1, range);
      r = rng.between(l, range);
    }
    std::printf("%lld %lld\n", l, r);
  }
}
