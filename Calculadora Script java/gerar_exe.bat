@echo off
echo Compilando...
javac Calculadora.java
if errorlevel 1 goto fim
echo Main-Class: Calculadora> manifesto.txt
jar cfm Calculadora.jar manifesto.txt *.class
del manifesto.txt
if exist entrada rmdir /s /q entrada
if exist Calculadora rmdir /s /q Calculadora
mkdir entrada
copy Calculadora.jar entrada\ >nul
echo Gerando o executavel...
jpackage --type app-image --name Calculadora --input entrada --main-jar Calculadora.jar --main-class Calculadora
rmdir /s /q entrada
echo.
echo Pronto! Abra a pasta Calculadora e de dois cliques em Calculadora.exe
:fim
pause
