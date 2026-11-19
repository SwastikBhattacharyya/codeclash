#include <iostream>
#include <vector>

int main() {
  std::vector<long long> values;
  long long value = 0;
  while (std::cin >> value) values.push_back(value);
  std::cout << values.at(values.size() * 2) << '\n';
}
