#include <cstdio>
#include <vector>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x5041495253554D53ULL, mode.index);
  int n = 0;
  long long limit = 1000000;
  bool constant = false;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 8));
    limit = 5;
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 2000));
    const long long ranges[] = {10, 1000, 1000000};
    limit = ranges[rng.between(0, 2)];
  } else if (mode.index == 6) {
    n = 100000;
  } else if (mode.index == 7) {
    n = 100000;
    constant = true;
  } else {
    n = 100000;
    limit = 50;
  }
  std::vector<long long> values(n);
  long long fixed = rng.between(-limit, limit);
  for (long long& value : values) value = constant ? fixed : rng.between(-limit, limit);
  long long k = 0;
  if (constant) {
    k = 2 * fixed;
  } else if (mode.index == 8 && !mode.small) {
    k = 0;
  } else if (n >= 2) {
    k = values[rng.between(0, n - 1)] + values[rng.between(0, n - 1)];
  } else {
    k = rng.between(-2 * limit, 2 * limit);
  }
  std::printf("%d %lld\n", n, k);
  for (int i = 0; i < n; ++i) std::printf("%lld%c", values[i], i + 1 == n ? '\n' : ' ');
}
