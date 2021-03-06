opendbcopy version 0.41

Copyright (C) 2004 Anthony Smith <smith@puzzle.ch>

=============================================================================================

WHAT IS OPENDBCOPY
******************
opendbcopy is a universal database utility to copy data from and to any database 
given a JDBC driver (or JDBC:ODBC bridge) without having to write any dump 
files first and setting up mapping files. Standard filters are included. Custom 
plugins can easily be added to do whatever operation you like (see developer manual).
The complete project model (including the database metadata) can be saved using XML format. 
A nice GUI helps you to manage your migration operations. 
You can also execute your database migration processes as batch jobs. 

Several standard converters such as trimming texts, removing whitespaces between
texts, inserting NULL instead of empty strings into nullable fields are already ready to use.

opendbcopy has been developed because standard products (Open Source), as far as known, do not provide 
a flexible way to apply custom filters such as merging fields etc. 

You define how easy or sophisticated you want to filter and migrate your data.

A plugin to produce SQL dumps for the appropriate destination database of your migration 
or delimited data files such as csv or other file type can be created using the appropriate plugin.

Please visit the opendbopy website at http://opendbcopy.sourceforge.net/ for latest updates.


HOW TO COMPILE OPENDBCOPY
*************************
See INSTALL for details.


JAR FILES REQUIRED FOR OPENDBCOPY
*********************************
opendbcopy requires the following jar files in the lib directory to run. 

-- jdom.jar
-- log4j-1.2.8.jar
-- xerces.jar

-- and appropriate JDBC drivers for the databases you want to interact with


HOW TO RUN OPENDBCOPY
*********************
To run opendbcopy please refer to the user_manual which you find at http://opendbcopy.sourceforge.net.
Forgive me if it is not yet complete... I do what I can.


UTILTIES AND LIBRARIES USED
***************************
This program has been developed using Eclipse 2.1.1. Its source code has been formatted 
using Jalopy (source code formatter plugin for Eclipse, see jalopy.sourceforge.net). 
This program includes software developed by the Apache Software Foundation (http://www.apache.org), 
as well as JDOM APIs from http://www.jdom.org.


Puzzle ITC / DIK VBS
********************

opendbcopy is sponsored by Puzzle ITC and the DIK VBS (Swiss Department for Defence, Civil Protection, Sports). 
Puzzle ITC is specialized in Java development using open-source software and Linux. 

To visit Puzzle ITC goto:
http://www.puzzle.ch (german text only) or email smith@puzzle.ch. 

To visit the DIK VBS goto:
http://www.vbs-ddps.ch/internet/vbs/en/home.html (german, french, italian, english)