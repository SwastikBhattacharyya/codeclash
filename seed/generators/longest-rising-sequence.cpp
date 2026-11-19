#include <cstdio>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x524953494E47ULL, mode.index);
  int n = 0;
  long long top = 100000;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 9));
    top = 6;
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 3000));
    top = mode.index % 2 == 0 ? 20 : 100000;
  } else {
    n = 150000;
  }
  std::printf("%d\n", n);
  for (int i = 0; i < n; ++i) {
    long long value = 0;
    if (!mode.small && mode.index == 7) {
      value = 1 + static_cast<long long>(i) * (top - 1) / n + rng.between(-50, 50);
      value = value < 1 ? 1 : value > top ? top : value;
    } else if (!mode.small && mode.index == 8) {
      value = top - static_cast<long long>(i) * (top - 1) / n;
    } else {
      value = rng.between(1, top);
    }
    std::printf("%lld%c", value, i + 1 == n ? '\n' : ' ');
  }
}
