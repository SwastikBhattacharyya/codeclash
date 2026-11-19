A degree has `n` courses numbered `1` to `n`. Some pairs `a b` mean course `a` must be completed before course `b`.

Find an order to take all courses that respects every requirement. If several orders exist, print the one that is smallest in lexicographic order: at every position, choose the smallest-numbered course that is available. If no valid order exists, print `-1`.

## Input

The first line contains two integers `n` and `m`, the number of courses and requirements.

Each of the next `m` lines contains two integers `a` and `b`, meaning `a` comes before `b`.

## Output

Print the `n` course numbers of the order on one line, separated by spaces, or `-1` if it is impossible.

## Constraints

- `1 ≤ n ≤ 100000`
- `0 ≤ m ≤ 70000`
- `1 ≤ a, b ≤ n`, `a ≠ b`
- The same requirement may appear more than once.

## Examples

### Example 1

**Input**

```text
5 4
3 1
3 2
1 4
2 4
```

**Output**

```text
3 1 2 4 5
```

### Example 2

**Input**

```text
3 3
1 2
2 3
3 1
```

**Output**

```text
-1
```
