# bible-dictionary-creator

Creates Bible Dictionary from plain text files.

# Pre-requisites and Notes

- Need JRE 17 and above
- Project is compatible with eclipse, however can be imported into any other IDEs
- build-ant.xml file is created by eclipse, you can reconfigure based on your need

Source Folder
	Each word is expected to be in a different text file. Ex. அகபு.txt.
	The dictionary word should be file name.
	File name should end with ".txt".
	Inside this file, the description, the definition of the dictionary word needs to given.
	There is no character limit for the definition of the dictionary word.
	You can use [H1], [H2], [H3] Starting of the line to indicate different levels of titles/headings inside the definition.
INFORMATION.txt
MAPPING.txt

Tamil & most of the Indian scripts has related words.
For an ex. the related words for the word "" are 

So, if you do not create mapping of these related words, your dictionary may not link to all these words. When the user clicks on one of these related words in any of the Bible Softwares (MyBible, MySword, TheWord, etc) it will not load the dictionary word definition.
So, creating this mapping file for Indian scripts is recommended.
This Bible dictionary creator program can create these related words automatically and map it with the Biblical words.
Or you can also manually give the list of such related words in a file named "MAPPING.txt".
When you give manual list of related words & run the program to automatically generate the mapping, it will also include the mappings created by you manually.
Your manual list of related words will be considered only when such words are found in any of the bibleVersions, otherwise they will be ignored.
One such example file is located inside this program under the directory named "samples" with the name "MAPPING.txt".

We would recommend to execute this program in 2 steps specially for languages which has related words:
Step 1: Create the mapping using this program automatically and then review it manually. You can remove/update the mappings in the newly created file "MAPPING-For-Review.txt".
	After reviewing it, you need to rename "MAPPING-For-Review.txt" file to "MAPPING.txt" or you need to move the content of this file to "MAPPING.txt"
	If you want to generate the mapping file automatically, then I would recommend below steps:
	
	Step 1: Run the program by setting the below flags to yes
		generateMappingsForReview=yes
		mapRelatedWordsAutomatically=yes
	Step 2: List all the bible versions to be considered to create the mapping file. For an ex:
		bibleVersions=TBSI,TAMSL'22,TAMOVR,TAMNT,TAMIRV'19,TAMCV'22,TAMCV'20,TAMBL'98,taBCS,ERV-ta,CTB1973,TamWCV
	Step 2: This will generate a new file named "MAPPING-For-Review.txt" in the source directory
	Step 3: Review this file carefully and remove or update any mappings
	Step 4: Rename "MAPPING-For-Review.txt" file to "MAPPING.txt" or you need to move the content of this file to "MAPPING.txt"

Step 2: Use the reviewed mapping bible and re-run the program again to generate the dictionary database/documents
	Step 1: Run the program by setting the below flags to yes
		generateMappingsForReview=no
		mapRelatedWordsAutomatically=no


Exceptions during running the program:
There is one exception/error "java.lang.ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1" throwned during program execution.
You can ignore this exception/error.
It is thrown due to not having correct reference format in the dictionary word description.
Recommended reference format examples are: Gen 1:1; Gen 1:1-10; Gen 1:1,4,5,6-10; Gen 1-5;
It can have more than one references, but should be separated by the special character without quotes ";"

