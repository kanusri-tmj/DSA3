package com.example.z_match;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DocumentController {

    @Autowired
    private ZAlgorithmService zAlgorithmService;

    private final Path corpusDir = Paths.get("corpus");

    @GetMapping("/compare-random")
    public ResponseEntity<?> compareRandomDocuments() {

        try {

            if (!Files.exists(corpusDir) ||
                    !Files.isDirectory(corpusDir)) {

                Map<String, Object> error =
                        new LinkedHashMap<>();

                error.put(
                        "matched",
                        false
                );

                error.put(
                        "error",
                        "Corpus folder not found."
                );

                return ResponseEntity.badRequest()
                        .body(error);
            }

            List<Path> files =
                    new ArrayList<>();

            try (DirectoryStream<Path> stream =
                         Files.newDirectoryStream(
                                 corpusDir,
                                 "*.txt"
                         )) {

                for (Path file : stream) {

                    if (Files.isRegularFile(file)) {
                        files.add(file);
                    }
                }
            }

            if (files.size() < 2) {

                Map<String, Object> error =
                        new LinkedHashMap<>();

                error.put(
                        "matched",
                        false
                );

                error.put(
                        "error",
                        "At least 2 TXT documents are required."
                );

                return ResponseEntity.badRequest()
                        .body(error);
            }

            Collections.shuffle(files);

            Path selectedFile1 = null;
            Path selectedFile2 = null;

            String selectedText1 = null;
            String selectedText2 = null;

            List<String> selectedChunks1 = null;
            List<String> selectedChunks2 = null;

            ZAlgorithmService.ComparisonResult selectedResult =
                    null;

            /*
             * Search through the corpus until
             * a matching pair is found.
             */

            for (int i = 0; i < files.size(); i++) {

                Path file1 = files.get(i);

                String text1 =
                        normalizeText(
                                Files.readString(file1)
                        );

                if (text1.isEmpty()) {
                    continue;
                }

                List<String> chunks1 =
                        createWordChunks(text1);

                if (chunks1.isEmpty()) {
                    continue;
                }

                for (int j = i + 1;
                     j < files.size();
                     j++) {

                    Path file2 = files.get(j);

                    String text2 =
                            normalizeText(
                                    Files.readString(file2)
                            );

                    if (text2.isEmpty()) {
                        continue;
                    }

                    List<String> chunks2 =
                            createWordChunks(text2);

                    if (chunks2.isEmpty()) {
                        continue;
                    }

                    /*
                     * Check whether at least one
                     * chunk from document 1 occurs
                     * in document 2.
                     */

                    boolean hasMatch = false;

                    for (String chunk : chunks1) {

                        if (!zAlgorithmService
                                .searchZ(
                                        text2,
                                        chunk
                                )
                                .isEmpty()) {

                            hasMatch = true;
                            break;
                        }
                    }

                    /*
                     * Ignore this pair if there
                     * is no matching content.
                     */

                    if (!hasMatch) {
                        continue;
                    }

                    /*
                     * Run all algorithms only
                     * after a matching pair
                     * has been found.
                     */

                    ZAlgorithmService.ComparisonResult result =
                            zAlgorithmService.compareDocuments(
                                    text1,
                                    text2,
                                    chunks1,
                                    chunks2
                            );

                    if (result.zMatchedChunks > 0) {

                        selectedFile1 = file1;
                        selectedFile2 = file2;

                        selectedText1 = text1;
                        selectedText2 = text2;

                        selectedChunks1 = chunks1;
                        selectedChunks2 = chunks2;

                        selectedResult = result;

                        break;
                    }
                }

                if (selectedResult != null) {
                    break;
                }
            }

            /*
             * No matching pair found.
             */

            if (selectedResult == null) {

                Map<String, Object> response =
                        new LinkedHashMap<>();

                response.put(
                        "matched",
                        false
                );

                response.put(
                        "message",
                        "No matching documents were found in the corpus."
                );

                return ResponseEntity.ok(response);
            }

            /*
             * Prepare result for frontend.
             */

            Map<String, Object> response =
                    new LinkedHashMap<>();

            response.put(
                    "matched",
                    true
            );

            response.put(
                    "doc1Name",
                    selectedFile1
                            .getFileName()
                            .toString()
            );

            response.put(
                    "doc2Name",
                    selectedFile2
                            .getFileName()
                            .toString()
            );

            response.put(
                    "zMatchedChunks",
                    selectedResult.zMatchedChunks
            );

            response.put(
                    "zSimilarity",
                    round(
                            selectedResult.zSimilarity
                    )
            );

            response.put(
                    "zAlgorithmTimeMs",
                    round(
                            selectedResult.zTimeMs
                    )
            );

            response.put(
                    "naiveAlgorithmTimeMs",
                    round(
                            selectedResult.naiveTimeMs
                    )
            );

            response.put(
                    "bitmaskMatchedChunks",
                    selectedResult.bitmaskMatchedChunks
            );

            response.put(
                    "bitmaskSimilarity",
                    round(
                            selectedResult.bitmaskSimilarity
                    )
            );

            response.put(
                    "bitmaskTimeMs",
                    round(
                            selectedResult.bitmaskTimeMs
                    )
            );

            response.put(
                    "networkFlowMatchedChunks",
                    selectedResult.networkFlowMatchedChunks
            );

            response.put(
                    "networkFlowSimilarity",
                    round(
                            selectedResult.networkFlowSimilarity
                    )
            );

            response.put(
                    "networkFlowTimeMs",
                    round(
                            selectedResult.networkFlowTimeMs
                    )
            );

            response.put(
                    "similarityPercentage",
                    round(
                            selectedResult.finalSimilarity
                    )
            );

            response.put(
                    "matchedSentences",
                    selectedResult.matchedSentences
            );

            response.put(
                    "doc1Text",
                    selectedText1
            );

            response.put(
                    "doc2Text",
                    selectedText2
            );

            response.put(
                    "doc1Chunks",
                    selectedChunks1.size()
            );

            response.put(
                    "doc2Chunks",
                    selectedChunks2.size()
            );

            return ResponseEntity.ok(response);

        } catch (IOException e) {

            e.printStackTrace();

            Map<String, Object> error =
                    new LinkedHashMap<>();

            error.put(
                    "matched",
                    false
            );

            error.put(
                    "error",
                    "Error reading corpus: "
                            + e.getMessage()
            );

            return ResponseEntity
                    .internalServerError()
                    .body(error);

        } catch (Exception e) {

            e.printStackTrace();

            Map<String, Object> error =
                    new LinkedHashMap<>();

            error.put(
                    "matched",
                    false
            );

            error.put(
                    "error",
                    "Error comparing documents: "
                            + e.getMessage()
            );

            return ResponseEntity
                    .internalServerError()
                    .body(error);
        }
    }

    /*
     * Convert the document into normalized
     * lowercase text.
     */

    private String normalizeText(String text) {

        return text
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    /*
     * Divide the document into groups
     * of approximately 5 words.
     */

    private List<String> createWordChunks(String text) {

        List<String> chunks =
                new ArrayList<>();

        if (text == null ||
                text.trim().isEmpty()) {

            return chunks;
        }

        String[] words =
                text.split("\\s+");

        int chunkSize = 5;

        for (int i = 0;
             i < words.length;
             i += chunkSize) {

            int end =
                    Math.min(
                            i + chunkSize,
                            words.length
                    );

            StringBuilder chunk =
                    new StringBuilder();

            for (int j = i;
                 j < end;
                 j++) {

                if (chunk.length() > 0) {
                    chunk.append(" ");
                }

                chunk.append(words[j]);
            }

            if (chunk.length() > 10) {
                chunks.add(
                        chunk.toString()
                );
            }
        }

        return chunks;
    }

    /*
     * Round values for displaying
     * algorithm results.
     */

    private double round(double value) {

        return Math.round(
                value * 1000.0
        ) / 1000.0;
    }
}