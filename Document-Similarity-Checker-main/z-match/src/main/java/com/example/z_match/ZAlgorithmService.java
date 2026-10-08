package com.example.z_match;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class ZAlgorithmService {

    public int[] calculateZArray(String str) {

        int n = str.length();
        int[] z = new int[n];

        int l = 0;
        int r = 0;

        for (int i = 1; i < n; i++) {

            if (i > r) {

                l = r = i;

                while (
                        r < n &&
                        str.charAt(r) == str.charAt(r - l)
                ) {
                    r++;
                }

                z[i] = r - l;
                r--;

            } else {

                int k = i - l;

                if (z[k] < r - i + 1) {

                    z[i] = z[k];

                } else {

                    l = i;

                    while (
                            r < n &&
                            str.charAt(r) == str.charAt(r - l)
                    ) {
                        r++;
                    }

                    z[i] = r - l;
                    r--;
                }
            }
        }

        return z;
    }

    public List<Integer> searchZ(
            String text,
            String pattern) {

        List<Integer> occurrences =
                new ArrayList<>();

        if (
                text == null ||
                pattern == null ||
                text.isEmpty() ||
                pattern.isEmpty()
        ) {
            return occurrences;
        }

        String combined =
                pattern + "$" + text;

        int[] z =
                calculateZArray(combined);

        int patternLength =
                pattern.length();

        for (int i = 0; i < z.length; i++) {

            if (z[i] == patternLength) {

                occurrences.add(
                        i - patternLength - 1
                );
            }
        }

        return occurrences;
    }

    public List<Integer> searchNaive(
            String text,
            String pattern) {

        List<Integer> occurrences =
                new ArrayList<>();

        if (
                text == null ||
                pattern == null ||
                text.isEmpty() ||
                pattern.isEmpty()
        ) {
            return occurrences;
        }

        int n = text.length();
        int m = pattern.length();

        for (int i = 0; i <= n - m; i++) {

            int j;

            for (j = 0; j < m; j++) {

                if (
                        text.charAt(i + j)
                                != pattern.charAt(j)
                ) {
                    break;
                }
            }

            if (j == m) {
                occurrences.add(i);
            }
        }

        return occurrences;
    }

    public ComparisonResult compareDocuments(
            String text1,
            String text2,
            List<String> chunks1,
            List<String> chunks2) {

        ComparisonResult result =
                new ComparisonResult();

        if (
                chunks1.isEmpty() ||
                chunks2.isEmpty()
        ) {

            result.zSimilarity = 0;
            result.bitmaskSimilarity = 0;
            result.networkFlowSimilarity = 0;
            result.finalSimilarity = 0;

            return result;
        }

        /*
         * Z-ALGORITHM
         */

        long zStart =
                System.nanoTime();

        Set<String> zMatches =
                new LinkedHashSet<>();

        for (String chunk : chunks1) {

            if (
                    !searchZ(
                            text2,
                            chunk
                    ).isEmpty()
            ) {

                zMatches.add(chunk);
            }
        }

        long zEnd =
                System.nanoTime();

        result.zMatchedChunks =
                zMatches.size();

        result.zSimilarity =
                ((double) result.zMatchedChunks
                        / chunks1.size())
                        * 100.0;

        result.zTimeMs =
                (zEnd - zStart)
                        / 1_000_000.0;

        /*
         * NAIVE
         */

        long naiveStart =
                System.nanoTime();

        for (String chunk : chunks1) {

            searchNaive(
                    text2,
                    chunk
            );
        }

        long naiveEnd =
                System.nanoTime();

        result.naiveTimeMs =
                (naiveEnd - naiveStart)
                        / 1_000_000.0;

        /*
         * BITMASK DP
         */

        long bitmaskStart =
                System.nanoTime();

        result.bitmaskMatchedChunks =
                calculateBitmaskDPSubsetMatch(
                        chunks1,
                        chunks2
                );

        long bitmaskEnd =
                System.nanoTime();

        result.bitmaskTimeMs =
                (bitmaskEnd - bitmaskStart)
                        / 1_000_000.0;

        int bitmaskBase =
                Math.min(
                        chunks1.size(),
                        chunks2.size()
                );

        if (bitmaskBase > 0) {

            result.bitmaskSimilarity =
                    ((double)
                            result.bitmaskMatchedChunks
                            / bitmaskBase)
                            * 100.0;
        }

        /*
         * NETWORK FLOW
         */

        long flowStart =
                System.nanoTime();

        result.networkFlowMatchedChunks =
                calculateMaxBipartiteFlow(
                        chunks1,
                        chunks2
                );

        long flowEnd =
                System.nanoTime();

        result.networkFlowTimeMs =
                (flowEnd - flowStart)
                        / 1_000_000.0;

        int flowBase =
                Math.min(
                        chunks1.size(),
                        chunks2.size()
                );

        if (flowBase > 0) {

            result.networkFlowSimilarity =
                    ((double)
                            result.networkFlowMatchedChunks
                            / flowBase)
                            * 100.0;
        }

        /*
         * FINAL SIMILARITY
         */

        result.finalSimilarity =
                (
                        result.zSimilarity
                                + result.bitmaskSimilarity
                                + result.networkFlowSimilarity
                ) / 3.0;

        if (result.finalSimilarity > 100) {
            result.finalSimilarity = 100;
        }

        result.matchedSentences =
                new ArrayList<>(zMatches);

        return result;
    }

    private int calculateBitmaskDPSubsetMatch(
            List<String> chunks1,
            List<String> chunks2) {

        int n =
                Math.min(
                        chunks1.size(),
                        10
                );

        int m =
                Math.min(
                        chunks2.size(),
                        10
                );

        if (n == 0 || m == 0) {
            return 0;
        }

        int limit =
                1 << m;

        int[] dp =
                new int[limit];

        for (
                int mask = 0;
                mask < limit;
                mask++
        ) {

            int count =
                    Integer.bitCount(mask);

            if (count >= n) {
                continue;
            }

            for (
                    int j = 0;
                    j < m;
                    j++
            ) {

                if (
                        (mask & (1 << j))
                                == 0
                ) {

                    boolean match =
                            !searchZ(
                                    chunks2.get(j),
                                    chunks1.get(count)
                            ).isEmpty();

                    int nextMask =
                            mask | (1 << j);

                    int value =
                            dp[mask]
                                    + (match ? 1 : 0);

                    dp[nextMask] =
                            Math.max(
                                    dp[nextMask],
                                    value
                            );
                }
            }
        }

        int maximum = 0;

        for (int value : dp) {

            maximum =
                    Math.max(
                            maximum,
                            value
                    );
        }

        return maximum;
    }

    private int calculateMaxBipartiteFlow(
            List<String> chunks1,
            List<String> chunks2) {

        int uSize =
                chunks1.size();

        int vSize =
                chunks2.size();

        int source = 0;

        int sink =
                uSize + vSize + 1;

        int totalNodes =
                sink + 1;

        int[][] capacity =
                new int[
                        totalNodes
                ][
                        totalNodes
                ];

        for (
                int i = 0;
                i < uSize;
                i++
        ) {

            capacity[source][i + 1] = 1;
        }

        for (
                int i = 0;
                i < uSize;
                i++
        ) {

            for (
                    int j = 0;
                    j < vSize;
                    j++
            ) {

                boolean match =
                        !searchZ(
                                chunks2.get(j),
                                chunks1.get(i)
                        ).isEmpty();

                if (match) {

                    capacity[i + 1]
                            [uSize + 1 + j] = 1;
                }
            }
        }

        for (
                int j = 0;
                j < vSize;
                j++
        ) {

            capacity[
                    uSize + 1 + j
            ][sink] = 1;
        }

        int maxFlow = 0;

        int[] parent =
                new int[totalNodes];

        while (
                bfsFlow(
                        capacity,
                        source,
                        sink,
                        parent
                )
        ) {

            int pathFlow =
                    Integer.MAX_VALUE;

            for (
                    int v = sink;
                    v != source;
                    v = parent[v]
            ) {

                int u =
                        parent[v];

                pathFlow =
                        Math.min(
                                pathFlow,
                                capacity[u][v]
                        );
            }

            for (
                    int v = sink;
                    v != source;
                    v = parent[v]
            ) {

                int u =
                        parent[v];

                capacity[u][v]
                        -= pathFlow;

                capacity[v][u]
                        += pathFlow;
            }

            maxFlow += pathFlow;
        }

        return maxFlow;
    }

    private boolean bfsFlow(
            int[][] capacity,
            int source,
            int sink,
            int[] parent) {

        boolean[] visited =
                new boolean[
                        capacity.length
                ];

        Queue<Integer> queue =
                new LinkedList<>();

        queue.add(source);

        visited[source] = true;

        parent[source] = -1;

        while (!queue.isEmpty()) {

            int u =
                    queue.poll();

            for (
                    int v = 0;
                    v < capacity.length;
                    v++
            ) {

                if (
                        !visited[v] &&
                        capacity[u][v] > 0
                ) {

                    parent[v] = u;

                    visited[v] = true;

                    queue.add(v);

                    if (v == sink) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static class ComparisonResult {

        public int zMatchedChunks;

        public int bitmaskMatchedChunks;

        public int networkFlowMatchedChunks;

        public double zSimilarity;

        public double bitmaskSimilarity;

        public double networkFlowSimilarity;

        public double finalSimilarity;

        public double zTimeMs;

        public double naiveTimeMs;

        public double bitmaskTimeMs;

        public double networkFlowTimeMs;

        public List<String> matchedSentences =
                new ArrayList<>();
    }
}