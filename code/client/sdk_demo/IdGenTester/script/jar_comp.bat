REM Set jdk bin path
set PATH=%PATH%;C:\Program Files\Java\jdk1.7.0_75\bin
set curdir=%~dp0
cd /d %curdir%

cd ../bin
jar cf idGenTester.jar com 
move idGenTester.jar ../lib
cd ..
pause