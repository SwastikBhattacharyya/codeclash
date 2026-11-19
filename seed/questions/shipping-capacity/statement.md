A warehouse must ship `n` packages using one truck. The packages leave **in the given order**, and the truck makes exactly one trip per day. Each day the truck takes a consecutive group of the next packages, as long as their total weight does not exceed its capacity.

Find the smallest capacity that lets the truck ship every package within `d` days.

## Input

The first line contains two integers `n` and `d`.

The second line contains `n` integers `w1 w2 … wn`, the package weights in shipping order.

## Output

Print the minimum capacity.

## Constraints

- `1 ≤ d ≤ n ≤ 150000`
- `1 ≤ w_i ≤ 10000`

## Examples

### Example 1

**Input**

```text
6 3
4 2 7 1 3 5
```

**Output**

```text
8
```

### Example 2

**Input**

```text
4 4
2 9 3 4
```

**Output**

```text
9
```
