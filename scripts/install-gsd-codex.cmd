@echo off
setlocal

cd /d "%~dp0\.."

echo Installing GSD Core for Codex as a local project runtime...
npx.cmd --yes @opengsd/gsd-core@latest --codex --local

echo.
echo Done. Restart Codex so it can load the local .codex runtime files.
