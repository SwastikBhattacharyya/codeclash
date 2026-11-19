A weather station logged one temperature change per hour, so the log is an array of `n` integers. Analysts keep asking for the total change between hour `l` and hour `r`, both inclusive.

Answer every question.

## Input

The first line contains two integers `n` and `q`, the number of hours and the number of questions.

The second line contains `n` integers `a1 a2 … an`, the logged changes.

Each of the next `q` lines contains two integers `l` and `r`.

## Output

For every question, print the sum `a_l + a_(l+1) + … + a_r` on its own line.

## Constraints

- `1 ≤ n ≤ 100000`
- `1 ≤ q ≤ 35000`
- `-1000 ≤ a_i ≤ 1000`
- `1 ≤ l ≤ r ≤ n`

## Examples

### Example 1

**Input**

```text
5 3
3 -1 4 1 5
1 3
2 5
4 4
```

**Output**

```text
6
9
1
```

### Example 2

**Input**

```text
1 1
-7
1 1
```

**Output**

```text
-7
```
