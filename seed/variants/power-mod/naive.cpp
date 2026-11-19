#include <iostream>

int main() {
  int q = 0;
  std::cin >> q;
  while (q-- > 0) {
    unsigned long long base = 0;
    unsigned long long exponent = 0;
    unsigned long long modulus = 0;
    std::cin >> base >> exponent >> modulus;
    base %= modulus;
    unsigned long long result = 1 % modulus;
    for (unsigned long long step = 0; step < exponent; ++step) result = result * base % modulus;
    std::cout << result << '\n';
  }
}
