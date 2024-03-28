(Only for the `Spider.java` part)

Compile the code: in the `java` directory, do `javac -cp jdbm-1.0.jar:htmlparser.jar:. Project/Spider.java`

Execute the executable: in the `java` directory, do `java -cp jdbm-1.0.jar:htmlparser.jar:. Project.Spider`  

Clean current `.class` files: `make -f MakefileMac.mk clean`

For Window(Using PowerShell):
Compile the code: in the `java` directory, do `javac -cp htmlparser.jar Project/Spider.java`
If there is still error, try to change `htmlparser.jar` to using whole path starting from C:

Execute the executable: in the `java` directory, do `java -cp ".;htmlparser.jar" Project.Spider`



How to use makefile:

compile all code: In the `java` directory, do `make -f MakeFileWindow.mk`

clean all .class files: In the `java` directory, do `make -f MakeFileWindow.mk clean`

Note: you do not need to do the cleaning before the compilation every time. Just compile it and it will update all necessary .class file for you