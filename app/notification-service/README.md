# Notification Service

Sends account verification emails. It listens to user changes from the auth service and mails each new verification token once.

## Overview

- No HTTP API. It consumes `auth.user.cdc` and ignores deletions and already-verified users.
- Before sending, it records the token in `user_verification_mails`. A token that is already there is skipped, so duplicate events never send a second mail.
- Mails are rendered from HTML and plain-text Thymeleaf templates and sent over SMTP. Locally that is Mailpit, which shows them in a web UI.
- Data lives in the `notification` database.
