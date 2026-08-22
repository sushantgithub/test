# test

## Laptop speedup helper

These scripts check why a laptop feels slow and can optionally clear **your
own** caches/temp files. They do not overclock, disable security software, or
kill processes.

### Windows (PowerShell)

If the script is in `C:\Users\spkul\Downloads`:

1. Press **Start**, type **PowerShell**, open **Windows PowerShell**.
2. Run:

```powershell
cd C:\Users\spkul\Downloads
powershell -ExecutionPolicy Bypass -File .\laptop-speedup.ps1
```

Preview cleanup (no deletions):

```powershell
powershell -ExecutionPolicy Bypass -File .\laptop-speedup.ps1 -Clean -DryRun
```

Clean user temp/cache files (asks first):

```powershell
powershell -ExecutionPolicy Bypass -File .\laptop-speedup.ps1 -Clean
```

Copy `laptop-speedup.ps1` into Downloads if you only copied the `.sh` file.
The `.sh` file is for Linux/macOS (or Git Bash / WSL), not Command Prompt.

### Linux / macOS (or Git Bash / WSL)

```bash
chmod +x laptop-speedup.sh
./laptop-speedup.sh
./laptop-speedup.sh --clean --dry-run
./laptop-speedup.sh --clean
```

If the report shows high RAM use, closing unused apps (especially browsers)
usually helps more than cache cleanup.
