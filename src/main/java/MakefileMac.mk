JC = javac
JFLAGS = -cp jdbm-1.0.jar:htmlparser.jar:.

.SUFFIXES: .java .class

default: compile

compile:
	$(JC) $(JFLAGS) Project/*.java

clean:
	$(RM) Project/*.class
