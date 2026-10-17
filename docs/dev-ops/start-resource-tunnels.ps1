$ErrorActionPreference = "Stop"

$ssh = Get-Command ssh.exe -ErrorAction Stop
$arguments = @(
    "-N",
    "-o", "ExitOnForwardFailure=yes",
    "-o", "ServerAliveInterval=30",
    "-o", "ServerAliveCountMax=3",
    "-L", "127.0.0.1:54363:127.0.0.1:9443",
    "-L", "127.0.0.1:18899:127.0.0.1:8899",
    "-L", "127.0.0.1:13307:127.0.0.1:13306",
    "-L", "127.0.0.1:16380:127.0.0.1:16379",
    "-L", "127.0.0.1:18082:127.0.0.1:8081",
    "User-Codex"
)

Write-Host "SSH 隧道已启动。保持此窗口打开，按 Ctrl+C 关闭。"
Write-Host "Portainer: https://127.0.0.1:54363/"
Write-Host "phpMyAdmin: http://127.0.0.1:18899/"
Write-Host "MySQL: 127.0.0.1:13307; Redis: 127.0.0.1:16380; Redis Commander: http://127.0.0.1:18082/"
& $ssh.Source @arguments
