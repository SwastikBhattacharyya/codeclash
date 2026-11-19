#include <cstdio>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x53484950ULL, mode.index);
  int n = 0;
  long long top = 10000;
  long long days = 1;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 8));
    top = 10;
    days = rng.between(1, n);
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 3000));
    days = rng.between(1, n);
  } else {
    n = 150000;
    const long long choices[3] = {1, 2, 1000};
    days = choices[(mode.index - 6) % 3];
  }
  std::printf("%d %lld\n", n, days);
  for (int i = 0; i < n; ++i) std::printf("%lld%c", rng.between(1, top), i + 1 == n ? '\n' : ' ');
}
