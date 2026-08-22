# test

## Laptop speedup helper

`laptop-speedup.sh` checks why a Linux or macOS laptop feels slow and can
optionally clear **your own** caches and trash. It does not overclock, disable
security software, or kill processes.

```bash
chmod +x laptop-speedup.sh
./laptop-speedup.sh                 # diagnose CPU, RAM, disk, top processes
./laptop-speedup.sh --clean --dry-run
./laptop-speedup.sh --clean          # asks before deleting
```

If the report shows high RAM/swap use, closing unused apps (especially browsers)
usually helps more than cache cleanup.
