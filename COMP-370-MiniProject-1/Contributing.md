# Contributing Guidelines

---

## 1. Cloning the Repository

```bash
git clone https://github.com/faneeshh/COMP370-MiniProject1.git
cd COMP370-MiniProject1
```

---

## 2. Working on Your Assigned Branch

Each teammate has a dedicated branch:

- `monitor-dev`
- `server-dev`
- `client-dev`
- `uml-docs`
- `testing`

Switch to your branch:

```bash
git checkout <your-branch-name>
git pull
```

Do **not** work directly on `main`.

---

## 3. Making Changes

After editing files:

```bash
git add .
git commit -m "Describe your change here"
git push
```

Push only to your own branch.

---

## 4. Opening a Pull Request (PR)

When your part is ready:

1. Go to GitHub → Pull Requests → New Pull Request  
2. Set:
   - **base branch:** `main`
   - **compare branch:** your branch  
3. Add a short description of what you implemented  
4. Submit the PR

I will review and merge it.

---

## 5. Coding Style

- Keep classes in the correct folders (`monitor/`, `server/`, `client/`, `common/`)
- Use clear method names and comments
- Keep message formats simple and consistent
- Log important events (startup, heartbeat, failover, client requests)

---

## 6. Folder Structure (Do Not Modify)

```
src/
  monitor/
  server/
  client/
  common/
scripts/
docs/
tests/
```

Please do not move or rename these folders.

---
