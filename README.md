# String Matching Application

## Project Description

The **String Matching Application** is a Java-based text comparison application used to identify matching or duplicated content between multiple text documents.

The application reads `.txt` files from a `corpus` folder, processes the text into meaningful chunks, compares every pair of documents, calculates their similarity percentage, and displays the results in the terminal.

## Problem Statement

When multiple documents contain similar or repeated content, manually comparing them is time-consuming.

This application provides an automated way to compare text documents and identify the amount of common content between them. It produces a similarity percentage and classifies the matching level.

## Application

The application is a **text string-matching and document comparison system** used to find common or duplicated content between documents.

### Applications

- Plagiarism detection
- Duplicate document detection
- Assignment comparison
- Report comparison
- Text content matching

## Technologies Used

- **Programming Language:** Java
- **Input:** `.txt` files
- **Execution:** Terminal / Command Prompt
- **Algorithm:** Z-Algorithm
- **Data Structures:** ArrayList, HashMap, List, Array
- **File Handling:** Java NIO

## Project Structure

```text
StringMatchingApplication/
│
├── DocMatching.java
│
└── corpus/
    ├── doc1.txt
    ├── doc2.txt
    ├── doc3.txt
    └── ...
