#include <iostream>
#include <string>

int main() {
  std::ios::sync_with_stdio(false);
  std::cin.tie(nullptr);
  int q = 0;
  std::cin >> q;
  std::string out;
  while (q-- > 0) {
    unsigned long long base = 0;
    unsigned long long exponent = 0;
    unsigned long long modulus = 0;
    std::cin >> base >> exponent >> modulus;
    unsigned long long result = 1 % modulus;
    base %= modulus;
    while (exponent > 0) {
      if (exponent & 1) result = result * base % modulus;
      base = base * base % modulus;
      exponent >>= 1;
    }
    out += std::to_string(result);
    out += '\n';
  }
  std::cout << out;
}
