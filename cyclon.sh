rm peersim-1.0.5.jar

find . -type f -name \*Test.java -exec ./stash.sh {} +

ant
jar cvf peersim-1.0.5.jar -C classes .

find . -type f -name \*Test._java -exec ./unstash.sh {} +

#java -cp "peersim-1.0.5.jar:jep-2.3.0.jar:djep-1.0.0.jar" peersim.Simulator example/scamp_simple.txt
#java -cp "peersim-1.0.5.jar:jep-2.3.0.jar:djep-1.0.0.jar" peersim.Simulator example/example_cyclon_hs.txt
java -cp "peersim-1.0.5.jar:jep-2.3.0.jar:djep-1.0.0.jar" peersim.Simulator example/cyclon_nohandshake.txt
