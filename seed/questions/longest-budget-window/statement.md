A project lists the cost of each of its `n` days. You have a budget `B` and want to fund the longest run of **consecutive** days whose total cost is at most `B`.

Print the length of the longest such run. If even a single day costs more than `B`, the answer is `0`.

## Input

The first line contains two integers `n` and `B`.

The second line contains `n` integers `c1 c2 … cn`, the daily costs.

## Output

Print the maximum number of consecutive days whose total cost is at most `B`.

## Constraints

- `1 ≤ n ≤ 150000`
- `1 ≤ c_i ≤ 10000`
- `1 ≤ B ≤ 1500000000`

## Examples

### Example 1

**Input**

```text
7 10
4 2 3 6 1 1 2
```

**Output**

```text
4
```

### Example 2

**Input**

```text
3 2
5 3 4
```

**Output**

```text
0
```
