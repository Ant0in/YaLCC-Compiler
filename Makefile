# makefile for the first part of our compiler project

# directories
SRC_DIR = src
BIN_DIR = bin
DIST_DIR = dist
TEST_DIR = test
LIB_DIR = lib

# main variables
LEXER = $(SRC_DIR)/LexicalAnalyzer.flex
MAIN_CLASS = Main
JAR_NAME = part1.jar
JAVA_FILES = $(SRC_DIR)/*.java
TEST_FILES = $(TEST_DIR)/*.java

# path to junit standalone jar (must be downloaded and placed in lib/)
JUNIT_JAR = $(LIB_DIR)/junit-platform-console-standalone.jar

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

# build test classes
test-classes: classes
	@javac -cp "$(JUNIT_JAR):$(BIN_DIR)" -d $(BIN_DIR) $(TEST_FILES)

# run tests
test: test-classes
	@java -jar $(JUNIT_JAR) execute --class-path $(BIN_DIR) --scan-class-path

# clean
clean:
	@rm -rf $(BIN_DIR)
	@rm -rf $(DIST_DIR)

.PHONY: all dirs classes jar clean test-classes test
