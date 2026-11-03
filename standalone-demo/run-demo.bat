@echo off
cd /d %~dp0
if not exist out mkdir out
javac --release 17 -d out src\BillingCopilotDemo.java || exit /b 1
java -cp out BillingCopilotDemo --test || exit /b 1
java -cp out BillingCopilotDemo 8787
