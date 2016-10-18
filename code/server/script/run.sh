#!/bin/sh

JAVA=/usr/local/java/bin/java
if [ ! -f $JAVA ]; then
	echo "Can't find java: $JAVA"
	exit -1
fi

# Build classpath (using all jar files in lib directory)
CP=.:conf
for file in lib/*
do
	if [ ! -d $file ]; then
#		file_ext=${file##*.}
#		if [ $file_ext=="jar" ]; then
		if [[ $file == *.jar ]]; then
			CP=$CP:$file
		fi
	fi
done

$JAVA $JVM_OPTS -classpath $CP com.xyz.idgen.mainframe.Starter
