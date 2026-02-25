@echo off
cd /d "C:\Users\Jeshua\Documents\tecsupArqSoftware\ejercicioPatronesDiseño"

echo Compilando src\*.java ...
javac -encoding UTF-8 -cp postgresql-42.7.3.jar src\*.java -d out

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR de compilacion. Revisa los mensajes anteriores.
    pause
    exit /b 1
)

echo.
echo Ejecutando aplicacion...
echo.

java -cp "out;postgresql-42.7.3.jar" BibliotecaApp

pause
