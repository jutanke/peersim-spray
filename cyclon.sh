rm peersim-1.0.5.jar
jar cvf peersim-1.0.5.jar -C classes .
java -cp "peersim-1.0.5.jar:jep-2.3.0.jar:djep-1.0.0.jar" peersim.Simulator example/cyclon.txt