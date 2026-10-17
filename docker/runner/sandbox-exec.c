#define _GNU_SOURCE
#include <errno.h>
#include <sched.h>
#include <stddef.h>
#include <seccomp.h>
#include <stdio.h>
#include <sys/prctl.h>
#include <unistd.h>

static const int EXIT_INTERNAL = 2;

static const int DENIED_SYSCALLS[] = {
    SCMP_SYS(ptrace),         SCMP_SYS(process_vm_readv), SCMP_SYS(process_vm_writev),
    SCMP_SYS(io_uring_setup), SCMP_SYS(io_uring_enter),   SCMP_SYS(io_uring_register),
    SCMP_SYS(unshare),        SCMP_SYS(setns),            SCMP_SYS(mount),
    SCMP_SYS(bpf),            SCMP_SYS(userfaultfd),      SCMP_SYS(perf_event_open),
    SCMP_SYS(keyctl),         SCMP_SYS(add_key),          SCMP_SYS(request_key),
};

static int add_rules(scmp_filter_ctx ctx) {
  for (size_t i = 0; i < sizeof DENIED_SYSCALLS / sizeof DENIED_SYSCALLS[0]; i++)
    if (seccomp_rule_add(ctx, SCMP_ACT_ERRNO(EPERM), DENIED_SYSCALLS[i], 0) != 0) return -1;
  if (seccomp_rule_add(ctx, SCMP_ACT_ERRNO(EPERM), SCMP_SYS(fork), 0) != 0) return -1;
  if (seccomp_rule_add(ctx, SCMP_ACT_ERRNO(EPERM), SCMP_SYS(vfork), 0) != 0) return -1;
  if (seccomp_rule_add(ctx, SCMP_ACT_ERRNO(EPERM), SCMP_SYS(clone), 1,
                       SCMP_A0(SCMP_CMP_MASKED_EQ, CLONE_THREAD, 0)) != 0)
    return -1;
  if (seccomp_rule_add(ctx, SCMP_ACT_ERRNO(ENOSYS), SCMP_SYS(clone3), 0) != 0) return -1;
  return 0;
}

int main(int argc, char *argv[]) {
  if (argc < 2) {
    fprintf(stderr, "usage: sandbox-exec <program> [args...]\n");
    return EXIT_INTERNAL;
  }
  if (prctl(PR_SET_NO_NEW_PRIVS, 1, 0, 0, 0) != 0) {
    perror("prctl");
    return EXIT_INTERNAL;
  }
  scmp_filter_ctx ctx = seccomp_init(SCMP_ACT_ALLOW);
  if (ctx == NULL) {
    fprintf(stderr, "seccomp_init failed\n");
    return EXIT_INTERNAL;
  }
  if (add_rules(ctx) != 0 || seccomp_load(ctx) != 0) {
    seccomp_release(ctx);
    fprintf(stderr, "seccomp setup failed\n");
    return EXIT_INTERNAL;
  }
  seccomp_release(ctx);
  execvp(argv[1], argv + 1);
  perror("execvp");
  return EXIT_INTERNAL;
}
