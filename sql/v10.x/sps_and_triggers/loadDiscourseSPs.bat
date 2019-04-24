
@ECHO OFF
set DB=%1
set USER=%2
set PWD=%3

echo Loading Stored Procedures to %DB%
echo %cd%

echo loading file discourse_delete_sp.sql
mysql -u %USER% -p%PWD% %DB% < discourse_delete_sp.sql

echo.

