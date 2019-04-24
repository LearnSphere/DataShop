@echo off

set db=%1
set user=%2
set pwd=%3

rem Dark magic to extract the dataset ids from the command line arguments.
rem It's a Rube Goldberg equivalent of "shift 3; datasets=$*" on Unix.
for /f "tokens=3*" %%a in ("%*") do set datasets=%%b

mysql %db% -u %user% -p%pwd% -e "CALL lfa_backup_datasets('%datasets%');"

