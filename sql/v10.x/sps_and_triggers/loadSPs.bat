
@ECHO OFF
set DB=%1
set USER=%2
set PWD=%3

echo Loading Stored Procedures to %DB%
echo %cd%

FOR %%f in (*.sql) do (
    echo loading file %%f
    mysql -u %USER% -p%PWD% %DB% < %%f
)

cd ..
mysql -u %USER% -p%PWD% %DB% < update_datashop_version_sp.sql

echo.

