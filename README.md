# Words

Small vocabulary for learning and repeating foreign words or all sentences and 
listening correct pronunciation.

![Words.png](doc/Words.png)

## What the project does

Words is small application I made for my little school girl. It enables you 
put in foreign words and sentences, categorize them and download correct 
pronunciation.  

## The features

* Add, modify or delete new words or sentences.
* Organize words or sentences in various categories.
* Automatically download pronunciation for your words and sentences - vocabulary supports many languages.
* Application tests your small student and creates statistics for each particular word. Base on this statistics vocabulary sorts words in each category.  

## How you can get started with

Words is Java application and so it can run on Windows, Mac or Linux.

1. Make sure you have Java Runtime installed on your machine. You can run `java -version` 
in command line in order to test if Java is installed or not. If Java is not present you can download and install JRE from
[Oracle site](https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html).
1. If Java Runtime is already installed you can go to `Releases` menu and download latest binary (for ex. Words-1.2.0-RELEASE.jar).
1. Create directory Words somewhere in your computer and copy there binary Words-1.2.0-RELEASE.jar.
1. Run it and choose foreign language for which you will want to download pronunciation.
1. Now you can start creating new words and categories.   

## Where you can get help

In case of any questions or troubles you can contact me at my [e-mail](mailto:jaroslav.beran@gmail.com).

## More information

### Files and directories

First time you run program you will be prompted about dictionary language.
After choosing language program will create following files and directories:

* `./setup.properties` - configuration file with your settings
* `./Data/Dictionary.txt` - dictionary file
* `./Data/MP3/` - MP3 directory for files with pronunciation 

### MP3 files

Each time when you add new word program will automatically download MP3 with pronunciation from internet for you.
Optionally you can download MP3 manually and place it into MP3 directory. In this case you can obtain MP3 files for your dictionary from following sites:

* http://forvo.com
* http://soundoftext.com

### How to build project

 1. Download project `git clone https://github.com/berk76/words Words`
 1. Change directory `cd Words`
 1. Build project `mvn assembly:assembly`
 1. Change directory `cd target` and find Words.jar
 1. Place it somewhere into Words/ directory for example and run it `java -jar Words.jar`
 