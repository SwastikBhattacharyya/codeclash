#include <iostream>
#include <string>
#include <vector>

constexpr long long Modulus = 1000000007;

std::vector<std::string> grid;

long long walk(int row, int col) {
  if (row >= static_cast<int>(grid.size()) || col >= static_cast<int>(grid[0].size())) return 0;
  if (grid[row][col] == '#') return 0;
  if (row + 1 == static_cast<int>(grid.size()) && col + 1 == static_cast<int>(grid[0].size())) return 1;
  return (walk(row + 1, col) + walk(row, col + 1)) % Modulus;
}

int main() {
  int n = 0;
  int m = 0;
  std::cin >> n >> m;
  grid.resize(n);
  for (std::string& line : grid) std::cin >> line;
  std::cout << walk(0, 0) << '\n';
}
