rm peersim-1.0.5.jar

find . -type f -name \*Test.java -exec ./stash.sh {} +

ant
jar cvf peersim-1.0.5.jar -C classes .

find . -type f -name \*Test._java -exec ./unstash.sh {} +

if [ "$1" = "cyclon" ]
then
    echo "CYCLON:"
    java -cp "peersim-1.0.5.jar:jep-2.3.0.jar:djep-1.0.0.jar" peersim.Simulator example/paper/cyclon.txt
elif [ "$1" = "scamp" ]
then
    echo "SCAMP:"
    java -cp "peersim-1.0.5.jar:jep-2.3.0.jar:djep-1.0.0.jar" peersim.Simulator example/paper/scamp.txt
elif [ "$1" = "scamplon" ]
then
    echo "SCAMPLON"
    java -cp "peersim-1.0.5.jar:jep-2.3.0.jar:djep-1.0.0.jar" peersim.Simulator example/paper/scamplon.txt
elif [ "$1" = "acyclon" ]
then
    echo "ADAPTIVE CYCLON"
    java -cp "peersim-1.0.5.jar:jep-2.3.0.jar:djep-1.0.0.jar" peersim.Simulator example/paper/acyclon.txt
else
    echo "NO PROTOCOL SELECTED!"
fi