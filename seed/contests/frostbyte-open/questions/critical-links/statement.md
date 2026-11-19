The winter grid connects `n` stations with `m` two-way links, numbered `1` to `m` in input order. Two stations can be joined by several parallel links. The grid isn't necessarily connected.

A link is critical if cutting it leaves some pair of stations that could reach each other before unable to reach each other. Find every critical link.

## Input

The first line contains two integers `n` and `m`.

Each of the next `m` lines contains two integers `u` and `v`, the stations joined by that link.

## Output

On the first line, print `k`, the number of critical links. Then print the numbers of the critical links in increasing order, one per line.

## Constraints

- `1 ≤ n ≤ 50000`
- `0 ≤ m ≤ 70000`
- `1 ≤ u, v ≤ n` and `u ≠ v`

## Examples

### Example 1

**Input**

```text
6 7
1 2
2 3
3 1
3 4
4 5
5 4
5 6
```

**Output**

```text
2
4
7
```

Links `5` and `6` both join stations `4` and `5`, so neither is critical on its own.

### Example 2

**Input**

```text
3 1
1 2
```

**Output**

```text
1
1
```
