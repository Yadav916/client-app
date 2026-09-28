@echo off

start "SCP-Service" /SEPARATE /B C:\Infospica\dcm4che\5.29.2\bin\storescp.bat -b SRCPACS:11112 --directory C:\Infospica-Workspace\eclarity-client\tmp\pending --request-timeout 5000 --release-timeout 5000 