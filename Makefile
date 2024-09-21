SOURCES = src/AggregationServer.java src/GETClient.java

# Default target: compile everything automatically
all: bin compile

# Ensure bin directory is created before compiling
bin:
	mkdir -p bin

# Compile the Java files required for the application, as defined in the SOURCES variable
compile:
	javac -cp .idea/libraries/gson-2.11.0.jar -d bin $(SOURCES)

# Make command to run the Aggregation Server
run-server:
	cd bin && java AggregationServer 4567

run-client:
	cd bin && java GETClient http://localhost: 4567