#include <iostream>
#include <string>
#include <vector>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int rows = 0;
  int cols = 0;
  std::cin >> rows >> cols;
  std::vector<std::string> grid(rows);
  int start = -1;
  int exit = -1;
  for (int r = 0; r < rows; ++r) {
    std::cin >> grid[r];
    for (int c = 0; c < cols; ++c) {
      if (grid[r][c] == 'S') start = r * cols + c;
      if (grid[r][c] == 'E') exit = r * cols + c;
    }
  }
  std::vector<int> distance(rows * cols, -1);
  std::vector<int> queue;
  queue.reserve(rows * cols);
  distance[start] = 0;
  queue.push_back(start);
  const int dr[4] = {1, -1, 0, 0};
  const int dc[4] = {0, 0, 1, -1};
  for (std::size_t head = 0; head < queue.size(); ++head) {
    int cell = queue[head];
    int r = cell / cols;
    int c = cell % cols;
    for (int k = 0; k < 4; ++k) {
      int nr = r + dr[k];
      int nc = c + dc[k];
      if (nr < 0 || nr >= rows || nc < 0 || nc >= cols || grid[nr][nc] == '#') continue;
      int next = nr * cols + nc;
      if (distance[next] != -1) continue;
      distance[next] = distance[cell] + 1;
      queue.push_back(next);
    }
  }
  std::cout << distance[exit] << '\n';
}
