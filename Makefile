# makefile for the first part of our compiler project

# few variables
SRC_DIR = src
BIN_DIR = bin
DIST_DIR = dist
LEXER = $(SRC_DIR)/LexicalAnalyzer.flex
MAIN_CLASS = Main
JAR_NAME = part1.jar

JAVA_FILES = $(SRC_DIR)/*.java

# base rule
all: dirs jar

# mk folders if not exist
dirs:
	@mkdir -p $(BIN_DIR)
	@mkdir -p $(DIST_DIR)

# generate lexer
$(SRC_DIR)/LexicalAnalyzer.java: $(LEXER)
	@jflex $<

# compile java files
classes: $(JAVA_FILES) $(SRC_DIR)/LexicalAnalyzer.java
	@javac -d $(BIN_DIR) $(JAVA_FILES)

# create jar
jar: classes
	@cd $(BIN_DIR) && jar cfe ../$(DIST_DIR)/$(JAR_NAME) $(MAIN_CLASS) *.class
	@echo "[i] JAR created in $(DIST_DIR)/$(JAR_NAME)"

# clean
clean:
	@rm -rf $(BIN_DIR)
	@rm -rf $(DIST_DIR)

.PHONY: all dirs classes jar clean
