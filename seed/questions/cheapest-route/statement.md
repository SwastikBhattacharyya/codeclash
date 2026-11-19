A country has `n` cities numbered `1` to `n`; city `1` is the capital. There are `m` one-way roads. Road `i` goes from city `u` to city `v` and costs `w` to travel.

For every city, find the cheapest total cost of travelling from the capital to it.

## Input

The first line contains two integers `n` and `m`.

Each of the next `m` lines contains three integers `u v w` describing a one-way road from `u` to `v` with cost `w`.

## Output

Print `n` integers on one line, separated by spaces. The `i`-th integer is the cheapest cost from city `1` to city `i`, or `-1` if city `i` cannot be reached. The cost for city `1` is `0`.

## Constraints

- `1 ≤ n ≤ 50000`
- `0 ≤ m ≤ 50000`
- `1 ≤ u, v ≤ n`
- `1 ≤ w ≤ 1000000`
- Roads may repeat and may start and end in the same city.

## Examples

### Example 1

**Input**

```text
4 5
1 2 4
1 3 1
3 2 2
2 4 5
4 1 1
```

**Output**

```text
0 3 1 8
```

### Example 2

**Input**

```text
3 1
2 3 7
```

**Output**

```text
0 -1 -1
```
