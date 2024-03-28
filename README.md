(Only for the `Spider.java` part)

Compile the code: in the `java` directory, do `javac -cp jdbm-1.0.jar:htmlparser.jar:. Project/Spider.java`

Execute the executable: in the `java` directory, do `java -cp jdbm-1.0.jar:htmlparser.jar:. Project.Spider`  

Clean current `.class` files: `make -f MakefileMac.mk clean`

For Window(Using PowerShell):
Compile the code: in the `java` directory, do `javac -cp htmlparser.jar Project/Spider.java`
If there is still error, try to change `htmlparser.jar` to using whole path starting from C:

Execute the executable: in the `java` directory, do `java -cp ".;htmlparser.jar" Project.Spider`
