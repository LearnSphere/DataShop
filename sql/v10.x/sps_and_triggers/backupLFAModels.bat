@echo off

set db=%1
set user=%2
set pwd=%3

rem Dark magic to extract the model ids from the command line arguments.
rem It's a Rube Goldberg equivalent of "shift 3; models=$*" on Unix.
for /f "tokens=3*" %%a in ("%*") do set models=%%b

mysql %db% -u %user% -p%pwd% -e "CALL lfa_backup_skill_models('%models%');"

