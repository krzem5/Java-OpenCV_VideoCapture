echo off
echo NUL>_.class&&del /s /f /q *.class
cls
javac com/krzem/opencv_videocapture/Main.java&&java com/krzem/opencv_videocapture/Main
start /min cmd /c "echo NUL>_.class&&del /s /f /q *.class"