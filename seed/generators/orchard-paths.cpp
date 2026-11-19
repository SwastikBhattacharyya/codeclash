#include <cstdio>
#include <string>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x4F52434841524450ULL, mode.index);
  int n = 0;
  int m = 0;
  long long blocked_percent = 10;
  bool clear_corners = true;
  bool wall = false;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 5));
    m = static_cast<int>(rng.between(1, 5));
    blocked_percent = 25;
    clear_corners = rng.between(0, 3) != 0;
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 60));
    m = static_cast<int>(rng.between(1, 60));
    blocked_percent = rng.between(0, 20);
  } else if (mode.index == 6) {
    n = 1000;
    m = 1000;
    blocked_percent = 0;
  } else if (mode.index == 7) {
    n = 1000;
    m = 1000;
    blocked_percent = 22;
  } else {
    n = 1000;
    m = 1000;
    blocked_percent = 5;
    wall = true;
  }
  long long gap = rng.between(0, n - 1);
  std::printf("%d %d\n", n, m);
  std::string line(m, '.');
  for (int row = 0; row < n; ++row) {
    for (int col = 0; col < m; ++col) {
      bool blocked = rng.between(1, 100) <= blocked_percent;
      if (wall && row + col == n - 1) blocked = row != gap;
      if (clear_corners && ((row == 0 && col == 0) || (row == n - 1 && col == m - 1))) blocked = false;
      line[col] = blocked ? '#' : '.';
    }
    std::printf("%s\n", line.c_str());
  }
}
