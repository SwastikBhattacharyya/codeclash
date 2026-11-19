#include <algorithm>
#include <cstdio>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x4E4554574F524BULL, mode.index);
  int n = 0;
  int q = 0;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 8));
    q = static_cast<int>(rng.between(1, 10));
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 3000));
    q = static_cast<int>(rng.between(1, 3000));
  } else {
    n = 100000;
    q = 70000;
  }
  std::printf("%d %d\n", n, q);
  bool chain = !mode.small && mode.index >= 6 && mode.index % 2 == 0;
  int linked = 1;
  for (int i = 0; i < q; ++i) {
    bool last = i + 1 == q;
    int type = last ? 2 : static_cast<int>(rng.between(1, 2));
    long long u = 0;
    long long v = 0;
    if (chain && type == 1 && linked < n) {
      u = linked;
      v = linked + 1;
      ++linked;
    } else if (chain && type == 2) {
      u = 1;
      v = rng.between(1, n);
    } else {
      long long span = mode.small ? n : std::min<long long>(n, q / 4 + 1);
      u = rng.between(1, span);
      v = rng.between(1, span);
    }
    std::printf("%d %lld %lld\n", type, u, v);
  }
}
