#include <algorithm>
#include <cstdio>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x52414E474553554DULL, mode.index);
  int n = 0;
  int q = 0;
  long long limit = 1000;
  bool wide = false;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 8));
    q = static_cast<int>(rng.between(1, 8));
    limit = 10;
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 2000));
    q = static_cast<int>(rng.between(1, 2000));
  } else {
    n = 100000;
    q = 35000;
    wide = true;
  }
  std::printf("%d %d\n", n, q);
  for (int i = 0; i < n; ++i) std::printf("%lld%c", rng.between(-limit, limit), i + 1 == n ? '\n' : ' ');
  for (int i = 0; i < q; ++i) {
    long long l = 0;
    long long r = 0;
    if (wide) {
      l = rng.between(1, std::min(n, 100));
      r = rng.between(std::max<long long>(l, n - 99), n);
    } else {
      l = rng.between(1, n);
      r = rng.between(l, n);
    }
    std::printf("%lld %lld\n", l, r);
  }
}
