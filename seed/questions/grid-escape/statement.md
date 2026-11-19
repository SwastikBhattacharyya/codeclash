You are standing in a building drawn as a grid of `R` rows and `C` columns. Each cell is one of:

- `.` an open floor,
- `#` a wall,
- `S` your starting cell (open),
- `E` the exit (open).

In one step you move to an adjacent open cell up, down, left or right. Find the fewest steps needed to reach the exit, or report that it is unreachable.

## Input

The first line contains two integers `R` and `C`.

Each of the next `R` lines contains a string of exactly `C` characters describing a row of the grid.

## Output

Print the minimum number of steps from `S` to `E`, or `-1` if the exit cannot be reached.

## Constraints

- `1 ≤ R, C ≤ 1000` and `R·C ≥ 2`
- The grid contains exactly one `S` and exactly one `E`.

## Examples

### Example 1

**Input**

```text
4 5
S..#.
.#...
.#.#.
...#E
```

**Output**

```text
7
```

### Example 2

**Input**

```text
2 3
S#E
.#.
```

**Output**

```text
-1
```
