# Seed data

Static data for `./codeclash seed-questions` and `./codeclash seed-contests`: 12 catalog questions, 3 contests with 9 more questions, contestants, and scripted submissions. Seeding reads this folder and never writes to it.

## Questions

```sh
./codeclash seed-admin                 # once, before seeding
./codeclash seed-questions             # upload the catalog
./codeclash seed-questions --verify    # also submit every reference solution
```

The tool logs in as the profile's superadmin. On compose it uses `GATEWAY_PORT` from `.env`, and on k8s a `kubectl port-forward` to `gateway-service`.

### What it does

1. **Generates the tests** in the `codeclash-runner:local` image, with no network. Each question gets its samples first, then the generated inputs, with expected outputs from the reference solution. The host needs no compiler and nothing is written to disk.
2. **Creates categories and tags** from `catalog/`, reusing any that already exist (case-insensitive).
3. **Creates each question** as published and uploads its test cases as public.
4. With `--verify`, **submits every reference solution** and prints the verdict, time and memory.

Rerunning is safe. Complete questions are skipped, and a question left half-seeded by an interrupted run gets its missing test cases. To rebuild a question, delete it in the admin and seed again.

### Layout

```
catalog/categories.json        name + description for each category
catalog/tags.json              name + description for each tag
questions/<slug>/question.json title, summary, limits, category and tag names, generated test count
questions/<slug>/statement.md  markdown description, including the Examples section
questions/<slug>/samples/*.in  sample inputs, uploaded as the first test cases
generators/<slug>.cpp          `gen <index>` prints generated test <index>; `gen <index> small` prints a tiny case
generators/rng.hpp             fixed-seed mt19937_64 with its own range mapping
solutions/<slug>.cpp           reference solution
```

### Generators

- Output is identical on every machine: `mt19937_64` is fully specified, and the range mapping and shuffle are hand-written instead of using the standard library's.
- Indexes 1–5 are small or medium random cases. Indexes 6–8 are maximum-size cases that make naive solutions time out.
- `small` mode prints tiny cases for checking a solution against a brute force.

### Limits

- Every test input and output stays under 1 MiB.
- 500 ms (1000 ms for Grid Escape) and 256 MB. Java and Python get 2× and 3× the time.
- On the largest tests, every reference solution runs in under 40 ms, and the naive approach takes 600 ms to over 3 s.

### Catalog

| Slug | Categories | Tags | Intended approach |
|---|---|---|---|
| range-sum-queries | Arrays | easy, prefix-sums | Prefix sums |
| next-taller-tower | Data Structures | easy, stack, monotonic-stack | Monotonic stack |
| prime-count-queries | Math | easy, sieve, number-theory, prefix-sums | Sieve plus prefix counts |
| shipping-capacity | Searching, Greedy | medium, binary-search, greedy | Binary search on the answer |
| longest-budget-window | Arrays | medium, two-pointers, sliding-window | Sliding window |
| grid-escape | Graphs | medium, bfs, grid | Breadth-first search |
| network-components | Graphs, Data Structures | medium, dsu | Disjoint set union |
| course-order | Graphs | medium, topological-sort, priority-queue | Kahn's algorithm with a min-heap |
| longest-rising-sequence | Dynamic Programming, Searching | medium, dynamic-programming, binary-search | Patience sorting |
| pattern-occurrences | Strings | medium, kmp, string-matching | Prefix function |
| cheapest-route | Graphs | hard, dijkstra, shortest-path, priority-queue | Dijkstra |
| inversion-count | Data Structures | hard, fenwick-tree, divide-and-conquer | Fenwick tree |

### Adding a question

1. Create `questions/<slug>/` with `question.json`, `statement.md` and at least one `samples/*.in`. Use only category and tag names from `catalog/`.
2. Add `generators/<slug>.cpp` (include `rng.hpp`, use `parse_mode`) and `solutions/<slug>.cpp`.
3. Compare the solution with a brute force on `gen <i> small` outputs.
4. Time the solution and a naive one on the largest tests in the runner image.
5. Run `./codeclash seed-questions --verify` against a local stack.

## Contests

`./codeclash seed-contests` seeds three contests from `seed/contests/`, with times relative to the moment it runs:

| Contest | Schedule | Problems (100 / 200 / 300 points) |
|---|---|---|
| Autumn Sprint | 2 hours, ended 2 days ago | Pair Sum Count, Budget Packing, Sensor Minimums |
| Midnight Gauntlet | started 1 hour ago, ends in 7 days | Meeting Picker, Orchard Paths, Family Tree Queries |
| Frostbyte Open | starts in 3 days, 2 hours | Power Mod, Delivery Tour, Critical Links |

### What it does

1. **Skip check:** logs in as the superadmin and skips every contest whose name already exists. If none is missing, it stops here.
2. **Tests:** generates the tests for the 9 contest questions in the runner image, in the same way as `seed-questions`.
3. **Questions:** creates the contest questions **hidden**, so opening a contest is what reveals them.
4. **Contestants:** for every handle in `users.json` it does the following:
   - signs the user up through `POST /api/v1/users` as `<handle>@example.test`, using the profile's contestant password
   - reads the verification token from the auth database
   - verifies with the anonymous `PATCH`
   - logs in
5. **Upcoming contest** (Frostbyte Open): creates it at its real schedule and registers its users.
6. **Past and ongoing contests:**
   1. Creates the contest 35 seconds ahead.
   2. Waits for contest-service to open it and for problem-service to make its questions visible.
   3. Registers the users and submits every `script.json` entry as its user.
   4. Waits for every verdict and checks it against the variant. Any mismatch stops before step 6.
   5. Waits for contest-service to receive every verdict.
   6. Moves everything to the real schedule with SQL:
      - the contest times, in both the contest and problem databases
      - registrations, set to a day before the start
      - submissions, set to `start + minute`
   7. Rebuilds `leaderboard_entries` with contest-service's own query.
   8. Prints the leaderboard.

   The moved rows get `updated_at = now()`, so a redelivered CDC event can't undo the move.

Rerunning is safe: existing contests, questions and contestants are skipped. If a run fails after creating a contest, that contest is left half seeded. Delete it in the admin, or reset, before seeding again.

### Layout

```
contests/users.json                          contestant handles and display names
contests/<contest>/contest.json              name, description, startOffsetMinutes, durationMinutes, questions + points, registrations
contests/<contest>/script.json               submissions: user, question, variant, minute after the start
contests/<contest>/questions/<slug>/         same format as questions/<slug>/
generators/<slug>.cpp, solutions/<slug>.cpp  shared with the catalog questions
variants/<slug>/naive.cpp                    correct but too slow: time_limit_exceeded on the largest tests
variants/<slug>/buggy.cpp                    fast but wrong: incorrect
variants/common/ce.cpp, re.cpp               compilation_error and runtime_error for any question
```

`seed-questions` ignores `contests/`.

### Script rules

The tool checks these before any API call:
- Every script user is registered for the contest.
- Every script question belongs to the contest.
- The variant is one of `correct`, `naive`, `buggy`, `ce` and `re`.
- Every minute falls inside the time that has already passed: under 120 for Autumn Sprint and under 60 for Midnight Gauntlet.
- An upcoming contest has no submissions.

### Expected leaderboards

| Autumn Sprint | Points | Midnight Gauntlet | Points |
|---|---|---|---|
| 1. byte_wizard | 600 | 1. quick_sort_queen | 600 |
| 2. quick_sort_queen | 600 | 2. byte_wizard | 400 |
| 3. heap_hopper | 400 | 3. greedy_goblin | 300 |
| 4. null_pointer | 300 | 4. lambda_lynx | 300 |
| 5. lambda_lynx | 300 | 5. heap_hopper | 200 |
| 6. bit_shifter | 100 | 6. null_pointer | 100 |
| 7. stack_smasher | 100 | 7. bit_shifter | 100 |
| 8. recursion_ronin, segfault_sam | 0 | 8. recursion_ronin | 0 |

Ties on points are broken by the earlier last solve.

### Checking a new contest question

Every check from [Adding a question](#adding-a-question) applies. In addition, `naive.cpp` must time out and `buggy.cpp` must give a wrong answer on the generated tests in the runner image.
