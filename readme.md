<!-- markdownlint-disable MD033 MD041 MD007 -->

<!-- pretty badges -->
<div align="center">
  <img src="https://img.shields.io/badge/Language-Java-red" alt="Language Badge"/>
  <img src="https://img.shields.io/badge/Version-1.0.0_alpha-blue" alt="Version Badge">
  <img src="https://img.shields.io/badge/License-MIT-dark_green.svg" alt="License Badge"/>
  <img src="https://img.shields.io/badge/School-ULB-yellow" alt="School Badge"/>
</div>

# 🖋️ YaLCC Compiler

Welcome to the **YaLCC compiler**! This project is part of the **Language Theory course INFO—F403** at `ULB`. It implements the **lexical analysis** phase for a toy programming language called **YaLCC**, turning source code into a stream of tokens for later parsing.

<div align="center">
  <img src="more/lexer_demo.svg" alt="lexer demo" width="400"/>
  <figcaption><b>YaLCC Lexer</b> scanning the tokens for <b><code>PGCD.ycc</code></b></figcaption>
</div>

## 📜 Description

This Java project provides:

- A **lexer** generated with **JFlex**.
- Recognition of **keywords**, **identifiers**, **numbers**, **operators**, and **comments**.

For more detailed problem specifications and additional information, please refer to: **`./more/F403project1.pdf`**.

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

## 📄 Generating Javadoc

To generate documentation:

```sh
javadoc -d doc src/*.java
```

- `-d doc`: output folder for HTML doc files.
- `src/*.java`: java files to generate doc from.

Once generated, open `doc/index.html` in a browser to explore the documentation.

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for more details.

## 🙏 Acknowledgements

This project was developed for the **`Introduction to language theory and compiling`** course **`INFO—F403`**. Special thanks to `Gille Geeraerts (ULB)` for their guidance and support.
