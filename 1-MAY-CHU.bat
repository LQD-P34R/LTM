@echo off
chcp 65001 >nul
cd /d "%~dp0"
java -Dfile.encoding=UTF-8 -cp "lib/VoiceLink.jar" com.voicechat.group.GroupHost 5000
if errorlevel 1 echo Kiem tra Java 21 va thong bao loi o tren.
pause
