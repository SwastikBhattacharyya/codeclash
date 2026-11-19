#pragma once

#include <cstdint>
#include <cstdlib>
#include <random>
#include <string>
#include <utility>
#include <vector>

class Rng {
 public:
  Rng(std::uint64_t salt, int index)
      : engine_(salt ^ (static_cast<std::uint64_t>(index) * 0x9E3779B97F4A7C15ULL)) {}

  long long between(long long low, long long high) {
    return low + static_cast<long long>(engine_() % static_cast<std::uint64_t>(high - low + 1));
  }

  template <class T>
  void shuffle(std::vector<T>& values) {
    for (std::size_t i = values.size(); i > 1; --i) {
      std::swap(values[i - 1], values[static_cast<std::size_t>(between(0, static_cast<long long>(i) - 1))]);
    }
  }

 private:
  std::mt19937_64 engine_;
};

struct Mode {
  int index;
  bool small;
};

inline Mode parse_mode(int argc, char** argv) {
  if (argc < 2) std::exit(2);
  return {std::atoi(argv[1]), argc > 2 && std::string(argv[2]) == "small"};
}
