A ranger checks two trail sensors at a time and gets an alert only when their readings add up exactly to a calibration value `k`. The sensors report `n` readings `a1 a2 … an`.

Count how many pairs of positions `i < j` satisfy `a_i + a_j = k`. Different positions holding equal values count as different pairs.

## Input

The first line contains two integers `n` and `k`.

The second line contains `n` integers `a1 a2 … an`.

## Output

Print the number of pairs `i < j` with `a_i + a_j = k`.

## Constraints

- `1 ≤ n ≤ 100000`
- `-1000000 ≤ a_i ≤ 1000000`
- `-2000000 ≤ k ≤ 2000000`

## Examples

### Example 1

**Input**

```text
5 6
1 5 3 3 7
```

**Output**

```text
2
```

The pairs are the readings `1 + 5` and the two readings `3 + 3`.

### Example 2

**Input**

```text
4 0
0 0 0 0
```

**Output**

```text
6
```
