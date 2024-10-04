SOURCES = src/AggregationServer.java src/ContentServer.java src/GETClient.java src/LamportClock.java
TEST_SOURCES = src/GETClientTest.java src/ContentServerTest.java src/LamportClockTest.java src/AggregationServerTest.java
DATAFILE = src/data.txt
GSON_LIB = .idea/libraries/gson-2.11.0.jar
JUNIT_LIB = .idea/libraries/junit-4.12.jar
HAMCREST_LIB = .idea/libraries/hamcrest-core-1.3.jar
DEFAULT_PORT = 4567
DEFAULT_HOST = localhost

# Default target: compile everything automatically
all: bin compile copy-data

# Ensure bin directory is created before compiling
bin:
	mkdir -p bin

# Compile the Java files required for the application and test files
compile:
	javac -cp $(GSON_LIB):$(JUNIT_LIB):$(HAMCREST_LIB) -d bin $(SOURCES) $(TEST_SOURCES)

# Copy data.txt from src to bin
copy-data:
	cp $(DATAFILE) bin/

# Make command to run the Aggregation Server in the background
run-server:
	cd bin && java -cp .:../$(GSON_LIB) AggregationServer $${PORT:-$(DEFAULT_PORT)} & \
	echo $$! > server.pid; \
	sleep 1

# Make command to stop the Aggregation Server
stop-server:
	@kill -9 `cat server.pid` || true; \
	rm -f server.pid

# JUnit Test target: compile, run server, and run all tests
test: compile run-server
	@echo "Running JUnit tests..."
	@cd bin && java -cp .:../$(JUNIT_LIB):../$(HAMCREST_LIB):../$(GSON_LIB) org.junit.runner.JUnitCore GETClientTest
	@cd bin && java -cp .:../$(JUNIT_LIB):../$(HAMCREST_LIB):../$(GSON_LIB) org.junit.runner.JUnitCore ContentServerTest
	@cd bin && java -cp .:../$(JUNIT_LIB):../$(HAMCREST_LIB):../$(GSON_LIB) org.junit.runner.JUnitCore LamportClockTest
	@cd bin && java -cp .:../$(JUNIT_LIB):../$(HAMCREST_LIB):../$(GSON_LIB) org.junit.runner.JUnitCore AggregationServerTest
	@$(MAKE) stop-server

# Clean up compiled classes and copied data file
clean:
	rm -rf bin/*.class bin/data.txt server.pid
