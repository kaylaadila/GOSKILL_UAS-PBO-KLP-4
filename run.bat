@echo off
if not exist lib\mysql-connector-j-8.4.0.jar (
  echo Jar MySQL belum ada di folder lib. Lihat README.
  pause
  exit /b
)
if not exist out mkdir out
javac -encoding UTF-8 -cp "lib/*" -d out src\main\java\goskill\*.java src\main\java\goskill\db\*.java src\main\java\goskill\model\*.java src\main\java\goskill\dao\*.java src\main\java\goskill\util\*.java src\main\java\goskill\service\*.java src\main\java\goskill\gui\*.java
if errorlevel 1 (
  echo Kompilasi gagal.
  pause
  exit /b
)
java -cp "out;lib/*" goskill.MainApp
