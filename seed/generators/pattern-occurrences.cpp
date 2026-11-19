#include <cstdio>
#include <string>

#include "rng.hpp"

int main(int argc, char** argv) {
  Mode mode = parse_mode(argc, argv);
  Rng rng(0x50415454ULL, mode.index);
  std::string text;
  std::string pattern;
  auto random = [&](std::size_t length, char last) {
    std::string value(length, 'a');
    for (char& ch : value) ch = static_cast<char>('a' + rng.between(0, last - 'a'));
    return value;
  };
  if (mode.small) {
    text = random(static_cast<std::size_t>(rng.between(1, 12)), 'b');
    pattern = random(static_cast<std::size_t>(rng.between(1, 4)), 'b');
  } else if (mode.index <= 4) {
    text = random(static_cast<std::size_t>(rng.between(1, 5000)), mode.index % 2 == 0 ? 'b' : 'z');
    pattern = random(static_cast<std::size_t>(rng.between(1, 6)), mode.index % 2 == 0 ? 'b' : 'z');
  } else if (mode.index == 5) {
    text = std::string(900000, 'a');
    pattern = std::string(100000, 'a');
  } else if (mode.index == 6) {
    text = std::string(900000, 'a');
    pattern = std::string(99999, 'a') + 'b';
  } else if (mode.index == 7) {
    text = random(900000, 'b');
    pattern = random(10, 'b');
  } else {
    text = random(900000, 'z');
    pattern = text.substr(123456, 3);
  }
  std::printf("%s\n%s\n", text.c_str(), pattern.c_str());
}
