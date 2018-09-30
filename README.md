# Two Pass Linker 
## Introduction
This program is a linker which passes inputs twice and returns an output after processing it. The linker's target machine is word addressable and each word consists of 4 decimal digits. 

## Use Instructions
Compile the file with the java compiler. 
`Erics-MacBook-Pro:TwoPassLinker thericfang$ javac TwoPassLinker.java`
Then run the class file
`Erics-MacBook-Pro:TwoPassLinker thericfang$ java TwoPassLinker`

The program should output what type of input is needed. For example, 
```
Please state the type of input:
1. File
2. Typed
```
Please type the necessary input type with 1 or 2. Any other input will close the program.
When the input is 1, type the name of the input file.
i.e ```What is the file name? 
input-1```

When the input is 2, type the inputs and terminate by typing exit and enter twice.
i.e 
```
Please type the input. Type exit to finish reading.
4 1 xy 2 2 z 2 -1 xy 4 -1 5 10043 56781 20004 80023 70014 0 1 z 1 2 3 -1 6 80013 10004 10004 30004 10023 10102 0 1 z 1 -1 2 50013 40004 1 z 2 2 xy 2 -1 z 1 -1 3 80002 10014 20004
exit
exit
```


