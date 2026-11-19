A line of `n` frost sensors runs along a valley, and sensor `i` currently reads `a_i`. The control room sends `q` operations, one after another:

- `1 i x`: sensor `i` now reads `x`.
- `2 l r`: report the lowest reading among sensors `l` to `r`, both inclusive.

Answer every report.

## Input

The first line contains two integers `n` and `q`.

The second line contains `n` integers `a1 a2 … an`.

Each of the next `q` lines contains one operation in the format above.

## Output

For every operation of type `2`, print the lowest reading on its own line.

## Constraints

- `1 ≤ n ≤ 100000`
- `1 ≤ q ≤ 28000`
- `0 ≤ a_i, x ≤ 9999`
- `1 ≤ i ≤ n`
- `1 ≤ l ≤ r ≤ n`
- There is at least one operation of type `2`.

## Examples

### Example 1

**Input**

```text
5 5
4 2 7 5 3
2 1 5
2 3 5
1 2 9
2 1 3
2 2 2
```

**Output**

```text
2
3
4
9
```

### Example 2

**Input**

```text
1 3
8
2 1 1
1 1 0
2 1 1
```

**Output**

```text
8
0
```
