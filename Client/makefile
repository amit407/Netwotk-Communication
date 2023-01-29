CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude -lpthread -lboost_thread
LDFLAGS:=-lboost_system -lpthread -lboost_thread

all: BGRSclient
	g++ -o bin/BGRSclient bin/connectionHandler.o bin/BGRSclient.o bin/Read.o $(LDFLAGS)

BGRSclient: bin/connectionHandler.o bin/BGRSclient.o bin/Read.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/BGRSclient.o: src/BGRSclient.cpp
	g++ $(CFLAGS) -o bin/BGRSclient.o src/BGRSclient.cpp

bin/Read.o: src/Read.cpp
	g++ $(CFLAGS) -o bin/Read.o src/Read.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
