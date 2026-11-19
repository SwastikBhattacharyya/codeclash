#include <cstdio>

#include "rng.hpp"

constexpr long long MaxValue = 1000000000000000000LL;
constexpr long long MaxModulus = 1000000000LL;

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x504F5745524D4F44ULL, mode.index);
  int q = 0;
  long long max_base = MaxValue;
  long long max_exponent = MaxValue;
  long long max_modulus = MaxModulus;
  bool edges = false;
  bool near_top = false;
  if (mode.small) {
    q = static_cast<int>(rng.between(1, 8));
    max_base = 10;
    max_exponent = 10;
    max_modulus = 20;
  } else if (mode.index <= 5) {
    q = static_cast<int>(rng.between(1, 2000));
    const long long exponents[] = {1000, 1000000000LL, MaxValue};
    max_exponent = exponents[rng.between(0, 2)];
  } else if (mode.index == 6) {
    q = 15000;
  } else if (mode.index == 7) {
    q = 15000;
    edges = true;
  } else {
    q = 15000;
    near_top = true;
  }
  std::printf("%d\n", q);
  for (int i = 0; i < q; ++i) {
    long long base = rng.between(0, max_base);
    long long exponent = rng.between(0, max_exponent);
    long long modulus = rng.between(1, max_modulus);
    if (edges) {
      switch (rng.between(0, 5)) {
        case 0: modulus = 1; break;
        case 1: base = 0; break;
        case 2: exponent = 0; break;
        case 3: base = modulus * rng.between(0, MaxValue / modulus); break;
        case 4:
          modulus = 1;
          exponent = 0;
          break;
        default: break;
      }
    } else if (near_top) {
      modulus = MaxModulus - rng.between(0, 1000);
      base = rng.between(0, 1) == 0 ? modulus - 1 : MaxValue - rng.between(0, 1000);
      exponent = MaxValue - rng.between(0, 1000);
    }
    std::printf("%lld %lld %lld\n", base, exponent, modulus);
  }
}
