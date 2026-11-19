A snow courier leaves the depot, location `1`, delivers to every one of the other `n - 1` locations exactly once, and returns to the depot. Driving from location `i` to location `j` costs `d_ij`. Costs can differ by direction, because some roads are uphill.

Find the cheapest round trip.

## Input

The first line contains an integer `n`.

Each of the next `n` lines contains `n` integers. The `j`-th integer on line `i` is `d_ij`.

## Output

Print the cost of the cheapest round trip. With only the depot (`n = 1`) the cost is `0`.

## Constraints

- `1 ≤ n ≤ 16`
- `0 ≤ d_ij ≤ 1000000`
- `d_ii = 0`

## Examples

### Example 1

**Input**

```text
4
0 10 15 20
5 0 9 10
6 13 0 12
8 8 9 0
```

**Output**

```text
35
```

The trip `1 → 2 → 4 → 3 → 1` costs `10 + 10 + 9 + 6 = 35`.

### Example 2

**Input**

```text
1
0
```

**Output**

```text
0
```
