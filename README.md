# Words

Small application for learning and repeating foreign words. I made this small application for my little school girl.

![Words.png](doc/Words.png)

## Files and directories

First time you run program you will be prompted about dictionary language.
After choosing language program will create following files and directories:

* `./setup.properties` - configuration file with your settings
* `./Data/Dictionary.txt` - dictionary file
* `./Data/MP3/` - MP3 directory for files with pronunciation 

## MP3 files

Each time when you add new word program will automatically download MP3 with sound from internet for you.
Optionally you can download MP3 manually and place it into MP3 directory. In this case you can obtain MP3 files for your dictionary from following sites:

* http://forvo.com
* http://soundoftext.com

## How to build

 1. download project `git clone https://github.com/berk76/words Words`
 1. change directory `cd Words`
 1. build project `mvn assembly:assembly`
 1. change directory `cd target` and find Words.jar
 1. place it somewhere into Words/ directory for example and run it `java -jar Words.jar`
 