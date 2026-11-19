A city skyline is a row of `n` towers, numbered from 1 on the left. Tower `i` has height `h_i`.

For every tower, find the nearest tower to its **right** that is **strictly taller**. Print its number, or `0` if no tower to the right is taller.

## Input

The first line contains an integer `n`.

The second line contains `n` integers `h1 h2 … hn`.

## Output

Print `n` integers on one line, separated by spaces. The `i`-th integer is the number of the nearest strictly taller tower to the right of tower `i`, or `0`.

## Constraints

- `1 ≤ n ≤ 120000`
- `1 ≤ h_i ≤ 100000`

## Examples

### Example 1

**Input**

```text
6
4 2 3 6 6 7
```

**Output**

```text
4 3 4 6 6 0
```

### Example 2

**Input**

```text
3
5 5 5
```

**Output**

```text
0 0 0
```
