The frost cipher encrypts each block by raising a number to a large power and keeping only the remainder. You have `q` blocks to check. For each block you are given `a`, `b` and `m`; print `a^b mod m`.

Take `a^0 = 1` for every `a`, including `a = 0`. The result is always in the range `0` to `m - 1`, so any number modulo `1` is `0`.

## Input

The first line contains an integer `q`.

Each of the next `q` lines contains three integers `a`, `b` and `m`.

## Output

For every block, print `a^b mod m` on its own line.

## Constraints

- `1 ≤ q ≤ 15000`
- `0 ≤ a ≤ 10^18`
- `0 ≤ b ≤ 10^18`
- `1 ≤ m ≤ 10^9`

## Examples

### Example 1

**Input**

```text
3
2 10 1000
3 0 7
123456789 2 999999937
```

**Output**

```text
24
1
710409998
```

### Example 2

**Input**

```text
3
5 3 1
0 0 9
1000000000000000000 1000000000000000000 1000000000
```

**Output**

```text
0
1
0
```
