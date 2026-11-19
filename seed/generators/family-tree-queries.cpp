#include <cstdio>
#include <vector>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x46414D494C595452ULL, mode.index);
  int n = 0;
  int q = 0;
  long long reach = 0;
  int legs = 0;
  if (mode.small) {
    n = static_cast<int>(rng.between(1, 8));
    q = static_cast<int>(rng.between(1, 8));
  } else if (mode.index <= 5) {
    n = static_cast<int>(rng.between(1, 2000));
    q = static_cast<int>(rng.between(1, 2000));
    const long long reaches[] = {0, 1, 3, 50};
    reach = reaches[rng.between(0, 3)];
  } else if (mode.index == 6) {
    n = 50000;
    q = 50000;
  } else if (mode.index == 7) {
    n = 50000;
    q = 50000;
    legs = 2;
  } else {
    n = 50000;
    q = 50000;
    legs = 5;
  }
  std::vector<int> parent(n, 0);
  std::vector<int> leg_of(n, -1);
  std::vector<std::vector<int>> leg_nodes(legs);
  int leg_length = legs > 0 ? (n - 1) * 9 / 10 / legs : 0;
  int next = 1;
  for (int leg = 0; leg < legs; ++leg) {
    int previous = 0;
    for (int step = 0; step < leg_length; ++step, ++next) {
      parent[next] = previous;
      leg_of[next] = leg;
      leg_nodes[leg].push_back(next);
      previous = next;
    }
  }
  for (; next < n; ++next) {
    long long low = reach > 0 && next > reach ? next - reach : 0;
    parent[next] = static_cast<int>(rng.between(low, next - 1));
  }
  std::vector<int> label(n);
  std::vector<int> shuffled;
  for (int i = 2; i <= n; ++i) shuffled.push_back(i);
  rng.shuffle(shuffled);
  label[0] = 1;
  for (int i = 1; i < n; ++i) label[i] = shuffled[i - 1];
  std::vector<int> parent_label(n + 1, 0);
  for (int i = 1; i < n; ++i) parent_label[label[i]] = label[parent[i]];
  std::printf("%d %d\n", n, q);
  for (int child = 2; child <= n; ++child) std::printf("%d%c", parent_label[child], child == n ? '\n' : ' ');
  if (n == 1) std::printf("\n");
  for (int i = 0; i < q; ++i) {
    int u = 0;
    int v = 0;
    if (legs > 0 && rng.between(0, 9) != 0) {
      long long a = rng.between(0, legs - 1);
      long long b = (a + rng.between(1, legs - 1)) % legs;
      u = leg_nodes[a][rng.between(leg_length * 4 / 5, leg_length - 1)];
      v = leg_nodes[b][rng.between(leg_length * 4 / 5, leg_length - 1)];
    } else {
      u = static_cast<int>(rng.between(0, n - 1));
      v = static_cast<int>(rng.between(0, n - 1));
    }
    std::printf("%d %d\n", label[u], label[v]);
  }
}
