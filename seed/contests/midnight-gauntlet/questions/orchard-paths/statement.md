An orchard is a grid of `n` rows and `m` columns. A cell is either open (`.`) or blocked by a fallen tree (`#`). A night picker starts in the top-left cell and must reach the bottom-right cell. Each step moves one cell right or one cell down, and the picker can only stand on open cells.

Count the different routes. The count can be huge, so print it modulo `1000000007`. If the start or the finish is blocked, there are no routes.

## Input

The first line contains two integers `n` and `m`.

Each of the next `n` lines contains a string of `m` characters, each `.` or `#`.

## Output

Print the number of routes modulo `1000000007`.

## Constraints

- `1 ≤ n, m ≤ 1000`

## Examples

### Example 1

**Input**

```text
3 4
....
.#..
....
```

**Output**

```text
4
```

### Example 2

**Input**

```text
2 2
.#
#.
```

**Output**

```text
0
```
