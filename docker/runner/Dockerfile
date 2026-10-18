FROM eclipse-temurin:25.0.4_7-jdk-noble AS launcher

RUN apt-get update \
  && apt-get install -y --no-install-recommends gcc libc6-dev libseccomp-dev \
  && rm -rf /var/lib/apt/lists/*

COPY sandbox-exec.c /src/sandbox-exec.c
RUN gcc -O2 -Wall -Wextra -Werror -o /usr/local/bin/sandbox-exec /src/sandbox-exec.c -lseccomp

FROM eclipse-temurin:25.0.4_7-jdk-noble

RUN apt-get update \
  && apt-get install -y --no-install-recommends g++ python3 libseccomp2 time \
  && rm -rf /var/lib/apt/lists/*

RUN useradd --uid 10001 --user-group --no-create-home --home-dir /sandbox \
  --shell /usr/sbin/nologin sandbox

COPY --from=launcher /usr/local/bin/sandbox-exec /usr/local/bin/sandbox-exec
COPY compile.sh run.sh /opt/runner/
RUN chmod 0555 /opt/runner/compile.sh /opt/runner/run.sh

WORKDIR /sandbox

CMD ["sleep", "infinity"]
