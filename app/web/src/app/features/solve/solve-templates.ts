import type { SubmissionLanguage } from '../../core/submissions/submissions';

export const SOLVE_TEMPLATES: Readonly<Record<SubmissionLanguage, string>> = {
  cpp: `#include <bits/stdc++.h>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    return 0;
}
`,
  java: `import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        StringTokenizer tokens = new StringTokenizer("");

        out.flush();
    }
}
`,
  python: `import sys


def main():
    data = sys.stdin.buffer.read().split()


main()
`,
};
