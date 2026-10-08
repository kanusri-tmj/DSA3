# Z-Match | Document Similarity Checker

A full-stack web application designed to compare two PDF documents and identify duplicated or plagiarized content. This project implements the **Z-Algorithm** for highly efficient string matching, demonstrating linear-time performance against large text datasets.

## 🚀 Features

* **PDF Text Extraction:** Seamlessly reads and extracts clean text from uploaded PDF files using Apache PDFBox.
* **Advanced Pattern Matching:** Breaks documents down into logical chunks and uses the Z-Algorithm to find exact matches.
* **Performance Analytics:** Benchmarks the Z-Algorithm against a Naïve string-matching approach in real-time, displaying execution speeds in milliseconds.
* **Visual Highlighting:** Provides a clean UI dashboard to display the final similarity percentage and visually highlight the copied text for the user.

## 🛠️ Tech Stack

* **Backend:** Java 21, Spring Boot (v4.1.1)
* **Frontend:** Vanilla HTML, CSS, JavaScript
* **Core Library:** Apache PDFBox (v3.0.8)

## 🧠 The Algorithm 

Comparing massive project documents manually is time-consuming and computationally expensive. Traditional naïve string matching compares characters one by one, resulting in a worst-case time complexity of $O(m \times n)$, which causes heavy lag when scanning large files.

This application optimizes the search by pre-processing a combined string of the search pattern and the target text to create a **Z-array**. This mathematical approach allows the system to skip redundant character comparisons, achieving a strictly linear time complexity of $O(m + n)$. 

## ⚙️ Local Setup and Installation

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/your-username/Z-Match.git](https://github.com/your-username/Z-Match.git)
