# Contest Contracts

The contest service's shared types: permission names and the contest change event.

## Overview

- `ContestPermissions` lists the two contest permissions. The auth service registers them so roles can hold them.
  - `contest:contest:write`: create, change and delete contests
  - `contest:contest:read_questions`: see a contest's questions before it starts
- `ContestCdcEventData` is the payload of `contest.contest.cdc`: id, live time, end time, when it was opened, and its question ids.
- The problem service consumes it to reveal a contest's questions when it opens, and to block hiding or deleting them until it ends.
