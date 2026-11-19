#include <cstdio>
#include <utility>
#include <vector>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x4352495449434C4BULL, mode.index);
  int n = 0;
  int m = 0;
  long long tree_share = 0;
  bool path = false;
  bool clusters = false;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 7));
    m = n >= 2 ? static_cast<int>(rng.between(0, 8)) : 0;
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(2, 2000));
    m = static_cast<int>(rng.between(0, 3000));
    tree_share = rng.between(0, 1);
  } else if (mode.index == 6) {
    n = 50000;
    m = 70000;
    tree_share = 1;
  } else if (mode.index == 7) {
    n = 50000;
    m = 70000;
    path = true;
  } else {
    n = 50000;
    m = 70000;
    clusters = true;
  }
  std::vector<std::pair<int, int>> edges;
  if (path) {
    for (int v = 2; v <= n; ++v) {
      edges.push_back({v - 1, v});
      if (v % 4 == 0) edges.push_back({v - 1, v});
    }
    while (static_cast<int>(edges.size()) < m) {
      long long u = rng.between(1, n - 10);
      edges.push_back({static_cast<int>(u), static_cast<int>(u + rng.between(2, 10))});
    }
  } else if (clusters) {
    int size = 50;
    for (int start = 1; start + size - 1 <= n; start += size) {
      for (int v = start + 1; v < start + size; ++v) edges.push_back({v - 1, v});
      edges.push_back({start + size - 1, start});
      if (start > 1) edges.push_back({static_cast<int>(rng.between(1, start - 1)), static_cast<int>(rng.between(start, start + size - 1))});
    }
    while (static_cast<int>(edges.size()) < m) {
      long long block = rng.between(0, n / size - 1) * size + 1;
      long long u = rng.between(block, block + size - 1);
      long long v = rng.between(block, block + size - 1);
      if (u != v) edges.push_back({static_cast<int>(u), static_cast<int>(v)});
    }
  } else {
    if (tree_share > 0 && n >= 2) {
      for (int v = 2; v <= n && static_cast<int>(edges.size()) < m; ++v) {
        edges.push_back({static_cast<int>(rng.between(1, v - 1)), v});
      }
    }
    while (static_cast<int>(edges.size()) < m) {
      long long u = rng.between(1, n);
      long long v = rng.between(1, n);
      if (u == v) continue;
      if (tree_share > 0 && rng.between(0, 3) != 0) v = u + rng.between(-3, 3);
      if (v < 1 || v > n || u == v) continue;
      edges.push_back({static_cast<int>(u), static_cast<int>(v)});
    }
  }
  rng.shuffle(edges);
  std::printf("%d %d\n", n, static_cast<int>(edges.size()));
  for (auto [u, v] : edges) {
    if (rng.between(0, 1) == 0) std::swap(u, v);
    std::printf("%d %d\n", u, v);
  }
}
