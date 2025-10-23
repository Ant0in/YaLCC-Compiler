# makefile for the first part of our compiler project

# directories
SRC_DIR = src
BIN_DIR = bin
DIST_DIR = dist
TEST_DIR = test/java
LIB_DIR = lib
MORE_DIR = more

# main variables
LEXER = $(SRC_DIR)/LexicalAnalyzer.flex
MAIN_CLASS = Main
JAR_NAME = part1.jar
JAVA_FILES = $(SRC_DIR)/*.java
TEST_FILES = $(TEST_DIR)/*.java

# path to junit standalone jar (must be downloaded and placed in lib/)
JUNIT_JAR = $(LIB_DIR)/junit-platform-console-standalone.jar
JUNIT_URL = https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.1/junit-platform-console-standalone-1.10.1.jar

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
test: junit-jar test-classes
	@java -jar $(JUNIT_JAR) execute --class-path $(BIN_DIR) --scan-class-path --details=tree --disable-banner

# download JUnit jar if missing
junit-jar:
	@mkdir -p $(LIB_DIR)
	@if [ ! -f $(JUNIT_JAR) ]; then \
		echo "[i] JUnit JAR not found, downloading...\n"; \
		curl -L -o $(JUNIT_JAR) $(JUNIT_URL); \
	else \
		echo "[i] JUnit JAR found."; \
	fi

# generate javadoc (ignore doclint warnings from generated code)
javadoc:
	@mkdir -p doc
	# remove everything in doc/ except report
	@find doc -mindepth 1 -maxdepth 1 ! -name 'report' -exec rm -rf {} +
	# generate javadoc
	@javadoc -Xdoclint:none -html5 -d doc $(JAVA_FILES)
	@echo "[i] Javadoc generated in ./doc (doclint disabled)"

# clean
clean:
	@rm -rf $(BIN_DIR)
	@rm -rf $(DIST_DIR)
	@rm -rf $(LIB_DIR)

.PHONY: all dirs classes jar clean test-classes test javadoc junit-jar
