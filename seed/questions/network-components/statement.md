A lab has `n` computers numbered `1` to `n` and no cables at first. You process `q` operations in order:

- `1 u v` connects computers `u` and `v` with a cable.
- `2 u v` asks whether `u` and `v` can currently exchange messages, directly or through other computers.

Answer every question at the moment it is asked.

## Input

The first line contains two integers `n` and `q`.

Each of the next `q` lines contains an operation `t u v` as described above.

## Output

For every operation of type `2`, print `YES` if the two computers are connected and `NO` otherwise, each on its own line.

## Constraints

- `1 ≤ n ≤ 100000`
- `1 ≤ q ≤ 70000`
- `1 ≤ u, v ≤ n`
- There is at least one operation of type `2`.

## Examples

### Example 1

**Input**

```text
5 6
2 1 2
1 1 2
1 4 5
2 1 2
2 2 5
2 3 3
```

**Output**

```text
NO
YES
NO
YES
```
