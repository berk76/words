# Words

Small application for learning and repeating foreign words. I made this small application for my little school girl.

## Files and directories

Create following directory structure:

* `Words/Words.jar` - binary file of compiled application
* `Words/Data/Dictionary.txt` - dictionary file (see below)
* `Words/Data/MP3/*.mp3` - MP3 files with pronunciation (see below)

## Dictionary file structure

Dictionary is CSV file with following structure.

`foreign word;native word;category name`

## MP3 files

MP3 file should have the same name as foreign word in `Dictionary.txt`. You can obtain MP3 files for your dictionary from following sites:

* http://forvo.com
* http://soundoftext.com

## How to build

 1. download project `git clone https://github.com/berk76/words Words`
 1. change directory `cd Words`
 1. build project `mvn assembly:assembly`
 1. change directory `cd target`
 1. find and run Words.jar `java -jar Words-x.y.z-SNAPSHOT-jar-with-dependencies.jar`
 
