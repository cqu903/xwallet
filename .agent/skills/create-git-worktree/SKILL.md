---
name: create-git-worktree
description: Creates a new git worktree in the ~/.worktree/ directory with interactive prompts for name and directory switching
---

# Create Git Worktree Skill

## Overview

This skill creates a new git worktree with an interactive workflow. It prompts the user for the worktree name and whether to switch to the new directory.

**Core principle:** User-driven workflow with clear prompts and confirmation.

**Announce at start:** "I'm using the create-git-worktree skill to set up a new worktree."

## Workflow

### Step 1: Get Current Git Repository Information

```bash
# Get the current repository root
repo_root=$(git rev-parse --show-toplevel)
repo_name=$(basename "$repo_root")
current_branch=$(git rev-parse --abbrev-ref HEAD)
```

### Step 2: Ask User for Worktree Name

Prompt the user with a clear question:

```
What name would you like to give this worktree?

Current repository: <repo-name>
Current branch: <current-branch>

Please enter a name (e.g., "feature/auth", "bugfix/login", "experiment/new-ui"):
```

**If user provides empty input:** Reprompt with clear guidance.

### Step 3: Ask User About Directory Switching

After getting the worktree name, ask:

```
Worktree will be created at: ~/.worktree/<worktree-name>

Would you like to switch to this directory immediately?

1. Yes - cd into the worktree and show me the path
2. No - just create it and tell me the path

Enter your choice (1 or 2):
```

### Step 4: Create the Worktree Directory

```bash
# Expand the tilde in the path
worktree_base="$HOME/.worktree"
worktree_path="$worktree_base/$WORKTREE_NAME"

# Create the base directory if it doesn't exist
mkdir -p "$worktree_base"

# Create the worktree (using current branch as reference)
git worktree add "$worktree_path" -b "$WORKTREE_NAME"
```

### Step 5: Handle Directory Switching

**If user chose "Yes":**

```bash
cd "$worktree_path"
echo "Switched to worktree: $worktree_path"
pwd
ls -la
```

**If user chose "No":**

```bash
echo "Worktree created at: $worktree_path"
```

### Step 6: Report Completion

Provide a clear completion message:

```
✅ Worktree created successfully!

Location: ~/.worktree/<worktree-name>
Branch: <worktree-name>
Repository: <repo-name>

To use this worktree later:
  cd ~/.worktree/<worktree-name>
  git status
```

## Interactive Prompts Summary

| Step | Prompt           | Validation                 |
| ---- | ---------------- | -------------------------- |
| 1    | Worktree name    | Non-empty, meaningful name |
| 2    | Switch directory | 1 (Yes) or 2 (No)          |

## Quick Reference

| Situation                    | Action                       |
| ---------------------------- | ---------------------------- |
| Empty worktree name          | Reprompt with guidance       |
| Invalid choice for switching | Reprompt with valid options  |
| Worktree path exists         | Error - path already exists  |
| Git error during creation    | Report error, do not proceed |

## Common Mistakes

### Not validating user input

- **Problem:** Empty or invalid worktree names cause errors
- **Fix:** Always validate non-empty input before proceeding

### Assuming user wants to switch

- **Problem:** Unexpected directory changes confuse users
- **Fix:** Always ask explicitly before switching

### Not expanding tilde

- **Problem:** `~/.worktree` doesn't work in all contexts
- **Fix:** Use `$HOME/.worktree` or explicitly expand `~`

## Example Workflow

```
You: I'm using the create-git-worktree skill to set up a new worktree.

[Get repo info: myproject on branch main]
User: feature/authentication

[Ask about directory switching]
User: 1 (Yes)

[Create worktree]
[Switch to directory]
[Report completion]

✅ Worktree created successfully!

Location: /Users/jesse/.worktree/feature/authentication
Branch: feature/authentication
Repository: myproject

To use this worktree later:
  cd /Users/jesse/.worktree/feature/authentication
  git status

You are now in: /Users/jesse/.worktree/feature/authentication
```

## Red Flags

**Never:**

- Create worktree without user-provided name
- Assume user's intent about directory switching
- Proceed after git errors during creation
- Use hardcoded paths without expansion

**Always:**

- Validate worktree name is non-empty
- Ask explicitly about directory switching
- Handle errors gracefully
- Expand `~` to `$HOME` for reliability

## Integration

**Called by:**

- Users who need a quick, simple worktree creation
- Any workflow requiring isolated worktree without complex setup

**Does NOT include:**

- Auto-ignoring the worktree directory (user's responsibility)
- Running project setup or tests (use separate skills for that)
- Complex branch detection or naming conventions
