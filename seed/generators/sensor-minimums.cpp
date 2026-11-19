#include <algorithm>
#include <cstdio>
#include <vector>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x53454E534F524D49ULL, mode.index);
  int n = 0;
  int q = 0;
  long long limit = 9999;
  long long update_share = 3;
  bool wide = false;
  bool descending = false;
  bool chase = false;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 8));
    q = static_cast<int>(rng.between(1, 8));
    limit = 9;
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 2000));
    q = static_cast<int>(rng.between(1, 2000));
  } else if (mode.index == 6) {
    n = 100000;
    q = 28000;
    wide = true;
  } else if (mode.index == 7) {
    n = 100000;
    q = 28000;
    wide = true;
    descending = true;
    update_share = 0;
  } else {
    n = 100000;
    q = 28000;
    wide = true;
    chase = true;
  }
  std::vector<long long> values(n);
  for (int i = 0; i < n; ++i) {
    values[i] = descending ? limit - static_cast<long long>(i) * limit / n : rng.between(0, limit);
  }
  std::printf("%d %d\n", n, q);
  for (int i = 0; i < n; ++i) std::printf("%lld%c", values[i], i + 1 == n ? '\n' : ' ');
  for (int i = 0; i < q; ++i) {
    bool update = i + 1 < q && (chase ? i % 2 == 0 : update_share > 0 && rng.between(1, update_share) == 1);
    if (update) {
      long long at = 0;
      if (chase) {
        at = std::min_element(values.begin(), values.end()) - values.begin();
      } else {
        at = rng.between(0, n - 1);
      }
      values[at] = rng.between(0, limit);
      std::printf("1 %lld %lld\n", at + 1, values[at]);
    } else {
      long long l = 0;
      long long r = 0;
      if (wide) {
        l = rng.between(1, std::min(n, 100));
        r = rng.between(std::max<long long>(l, n - 99), n);
      } else {
        l = rng.between(1, n);
        r = rng.between(l, n);
      }
      std::printf("2 %lld %lld\n", l, r);
    }
  }
}
