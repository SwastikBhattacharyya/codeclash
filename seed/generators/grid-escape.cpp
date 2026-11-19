#include <cstdio>
#include <string>
#include <vector>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x47524944ULL, mode.index);
  int rows = 0;
  int cols = 0;
  int wallPercent = 25;
  if (mode.small) {
    rows = static_cast<int>(rng.between(1, 5));
    cols = static_cast<int>(rng.between(rows == 1 ? 2 : 1, 5));
    wallPercent = 30;
  } else if (mode.index <= 4) {
    rows = static_cast<int>(rng.between(2, 100));
    cols = static_cast<int>(rng.between(2, 100));
  } else {
    rows = 1000;
    cols = 1000;
  }
  std::vector<std::string> grid(rows, std::string(cols, '.'));
  int sr = 0;
  int sc = 0;
  int er = rows - 1;
  int ec = cols - 1;
  if (!mode.small && mode.index == 5) {
    for (int r = 0; r < rows; ++r) {
      for (int c = 0; c < cols; ++c) {
        if (rng.between(0, 99) < wallPercent) grid[r][c] = '#';
      }
    }
    grid[er - 1][ec] = '#';
    grid[er][ec - 1] = '#';
    grid[er - 1][ec - 1] = '#';
  } else if (!mode.small && mode.index == 6) {
    for (int r = 1; r < rows; r += 2) {
      for (int c = 0; c < cols; ++c) grid[r][c] = '#';
      grid[r][(r / 2) % 2 == 0 ? cols - 1 : 0] = '.';
    }
    er = rows - 1;
    ec = (rows / 2) % 2 == 0 ? cols - 1 : 0;
  } else if (!mode.small && mode.index == 7) {
    sr = rows / 2;
    sc = 0;
    er = rows / 2;
    ec = cols - 1;
  } else {
    for (int r = 0; r < rows; ++r) {
      for (int c = 0; c < cols; ++c) {
        if (rng.between(0, 99) < wallPercent) grid[r][c] = '#';
      }
    }
    if (mode.small) {
      sr = static_cast<int>(rng.between(0, rows - 1));
      sc = static_cast<int>(rng.between(0, cols - 1));
      do {
        er = static_cast<int>(rng.between(0, rows - 1));
        ec = static_cast<int>(rng.between(0, cols - 1));
      } while (er == sr && ec == sc);
    }
  }
  if (!mode.small && mode.index != 5 && mode.index != 6) {
    int r = sr;
    int c = sc;
    while (r != er || c != ec) {
      grid[r][c] = '.';
      bool down = r < er && (c == ec || rng.between(0, 1) == 0);
      if (down) {
        ++r;
      } else {
        ++c;
      }
    }
  }
  grid[sr][sc] = 'S';
  grid[er][ec] = 'E';
  std::printf("%d %d\n", rows, cols);
  for (const std::string& row : grid) std::printf("%s\n", row.c_str());
}
