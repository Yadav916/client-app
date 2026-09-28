@echo off

start "SCP-Service" /SEPARATE /B C:\eclarity\dcm4che\bin\storescp.bat -b SRCPACS:11112 --directory C:\eclarity\tmp\pending --request-timeout 5000 --release-timeout 5000