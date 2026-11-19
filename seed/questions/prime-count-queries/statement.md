A prime is an integer greater than 1 whose only divisors are 1 and itself.

You are given `q` ranges. For each range `[l, r]`, count the primes `p` with `l ≤ p ≤ r`.

## Input

The first line contains an integer `q`.

Each of the next `q` lines contains two integers `l` and `r`.

## Output

For every range, print the number of primes in it on its own line.

## Constraints

- `1 ≤ q ≤ 50000`
- `1 ≤ l ≤ r ≤ 10000000`

## Examples

### Example 1

**Input**

```text
3
1 10
11 20
14 16
```

**Output**

```text
4
4
0
```

### Example 2

**Input**

```text
1
2 2
```

**Output**

```text
1
```
