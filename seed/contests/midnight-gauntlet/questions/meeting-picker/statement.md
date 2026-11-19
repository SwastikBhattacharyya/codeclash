The midnight hackathon has `n` talks, and talk `i` runs from minute `s_i` to minute `e_i`. A talk occupies the half-open interval `[s_i, e_i)`, so a talk that ends at minute `t` and another that starts at minute `t` don't overlap.

You can attend only one talk at a time and must stay for a whole talk. What is the largest number of talks you can attend?

## Input

The first line contains an integer `n`.

Each of the next `n` lines contains two integers `s_i` and `e_i`.

## Output

Print the largest number of talks you can attend.

## Constraints

- `1 ≤ n ≤ 50000`
- `0 ≤ s_i < e_i ≤ 1000000`

## Examples

### Example 1

**Input**

```text
4
1 10
2 4
4 6
6 9
```

**Output**

```text
3
```

Skip the long first talk and attend the other three back to back.

### Example 2

**Input**

```text
3
0 5
0 5
5 8
```

**Output**

```text
2
```
