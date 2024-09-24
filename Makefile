SOURCES = src/AggregationServer.java src/ContentServer.java src/GETClient.java src/LamportClock.java
DATAFILE = src/data.txt
GSON_LIB = .idea/libraries/gson-2.11.0.jar

# Default target: compile everything automatically
all: bin compile copy-data

# Ensure bin directory is created before compiling
bin:
	mkdir -p bin

# Compile the Java files required for the application, as defined in the SOURCES variable
compile:
	javac -cp $(GSON_LIB) -d bin $(SOURCES)

# Copy data.txt from src to bin
copy-data:
	cp $(DATAFILE) bin/

# Make command to run the Aggregation Server
run-server:
	cd bin && java -cp .:../$(GSON_LIB) AggregationServer 4567

# Make command to run the GETClient
run-client:
	cd bin && java -cp .:../$(GSON_LIB) GETClient http://localhost:4567

# Make command to run the ContentServer
run-content:
	cd bin && java -cp .:../$(GSON_LIB) ContentServer http://localhost:4567

# Clean up compiled classes and copied data file
clean:
	rm -rf bin/*.class bin/data.txt
