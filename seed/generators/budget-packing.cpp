#include <cstdio>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x4255444745545050ULL, mode.index);
  int n = 0;
  long long budget = 0;
  long long max_cost = 0;
  long long max_value = 1000000000;
  bool correlated = false;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 8));
    budget = rng.between(1, 20);
    max_cost = 10;
    max_value = 20;
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 200));
    budget = rng.between(1, 5000);
    max_cost = rng.between(1, 5000);
  } else if (mode.index == 6) {
    n = 2000;
    budget = 50000;
    max_cost = 50000;
  } else if (mode.index == 7) {
    n = 2000;
    budget = 50000;
    max_cost = 100;
  } else {
    n = 2000;
    budget = 50000;
    correlated = true;
  }
  std::printf("%d %lld\n", n, budget);
  for (int i = 0; i < n; ++i) {
    long long cost = 0;
    long long value = 0;
    if (correlated) {
      cost = rng.between(20000, 30000);
      value = cost * 1000 + rng.between(0, 999);
    } else {
      cost = rng.between(1, max_cost);
      value = rng.between(1, max_value);
    }
    std::printf("%lld %lld\n", cost, value);
  }
}
