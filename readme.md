<!-- markdownlint-disable MD033 MD041 MD007 -->

<!-- pretty badges -->
<div align="center">
  <img src="https://img.shields.io/badge/Language-Java-red" alt="Language Badge"/>
  <img src="https://img.shields.io/badge/Version-1.0.1-blue" alt="Version Badge">
  <img src="https://img.shields.io/badge/License-MIT-dark_green.svg" alt="License Badge"/>
  <img src="https://img.shields.io/badge/School-ULB-yellow" alt="School Badge"/>
  <img src="https://github.com/Ant0in/YaLCC-Compiler/actions/workflows/ci.yml/badge.svg" alt="Build Status"/>
  <a href="https://ant0in.github.io/YaLCC-Compiler/">
    <img src="https://img.shields.io/badge/Docs-Javadoc-brightgreen.svg" alt="Javadoc Badge"/>
  </a>
</div>

# 🖋️ YaLCC Compiler

Welcome to the **YaLCC compiler**! This project is part of the **Language Theory course INFO—F403** at `ULB`. It implements the **lexical analysis** phase for a toy programming language called **YaLCC**, turning source code into a stream of tokens for later parsing.

<div align="center">
  <img src="more/lexer_demo.svg" alt="lexer demo" width="600"/>
  <p><b>YaLCC Lexer</b> scanning the tokens for <b><code>PGCD.ycc</code></b></p>
</div>

## 📜 Description

This Java project provides:

- A **lexer** generated with **JFlex**.
- Recognition of **keywords**, **identifiers**, **numbers**, **operators**, and **comments**.

For more detailed problem specifications and additional information, please refer to: [**`./more/F403project1.pdf`**](more/F403project1.pdf).

## ⚙️ Installation

1. Clone the repository:

    ```sh
    git clone git@github.com:Ant0in/YaLCC-Compiler.git
    cd YaLCC-Compiler
    ```

2. Make sure you have **Java JDK 17+** installed and **JFlex**. Those can be installed using `pacman` and `yay`:

    ```sh
    sudo pacman -S jdk17-openjdk
    yay -S jflex
    ```

3. Compile the project using the provided `Makefile`:

    ```sh
    make
    ```

Alternatively, you can compile the project directly using `javac`.

## 🛠️ Usage

### Running the Lexer

To run the lexer on a source file, use:

```sh
java -jar dist/part1.jar test/<sourcefile>.ycc
```

## 📚 Documentation

The complete **Javadoc** is hosted on Github Pages. It is available here:

👉 [View the Javadoc online](https://ant0in.github.io/YaLCC-Compiler/)

## ✨ Generating Javadoc

To generate documentation:

```sh
javadoc -d doc src/*.java
```

- `-d doc`: output folder for HTML doc files.
- `src/*.java`: java files to generate doc from.

Once generated, open `doc/index.html` in a browser to explore the documentation.

## 🧪 Running Unit Tests

To ensure the project works as expected, you can run the **JUnit 5 tests** for the YaLCC included in the repository.

The `Makefile` provides a `test` target. When executed, it will:

- Ensure the **`lib/`** folder exists and download the **JUnit 5 standalone JAR** if missing.
- Compile all test files located in `test/java/`.
- Run all tests automatically.

Simply run:

```sh
make test
```

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for more details.

## 🙏 Acknowledgements

This project was developed for the **`Introduction to language theory and compiling`** course **`INFO—F403`**. Special thanks to `Gilles Geeraerts (ULB)` and `Arnaud Leponce (ULB)` for their guidance and support.
