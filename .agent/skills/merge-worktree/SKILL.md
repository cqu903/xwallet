---
name: merge-worktree
description: Merges changes from a git worktree into the current branch with conflict handling and optional cleanup
---

# Merge Git Worktree Skill

## Overview

This skill merges changes from a git worktree back into the current branch. It automatically detects the worktree's base branch, handles conflicts, and offers to clean up the worktree after merging.

**Core principle:** Safe merging with automatic conflict detection and user-guided cleanup.

**Announce at start:** "I'm using the merge-worktree skill to merge changes from the worktree."

## Workflow

### Step 1: Parse User Request

Extract the worktree name from user input (e.g., "feat/bill" from "将feat/bill合并").

### Step 2: Verify Worktree Exists

```bash
# List all worktrees to verify
git worktree list
```

If worktree doesn't exist, report error and stop.

### Step 3: Check Worktree Base Branch

```bash
# Get the worktree path
worktree_path="$HOME/.worktree/<worktree-name>"

# Check which branch the worktree is based on
cd "$worktree_path"
worktree_branch=$(git rev-parse --abbrev-ref HEAD)
parent_branch=$(git log --ancestry-path --merges --oneline HEAD...develop | tail -1 | awk '{print $NF}' || echo "unknown")
```

**If worktree is NOT from current branch:**
- Warn user: "This worktree appears to be based on a different branch. Continue?"
- Ask for confirmation before proceeding

### Step 4: Ensure Current Branch is Up-to-Date

```bash
git switch develop
git pull --ff-only
```

### Step 5: Merge the Worktree

```bash
git merge --no-ff <worktree-name>
```

### Step 6: Handle Merge Results

**If merge succeeds:**
Proceed to Step 7.

**If merge has conflicts:**
Show conflict status:
```bash
git status
```

Provide options:
1. Show conflicts and ask user to resolve manually
2. Abort merge: `git merge --abort`

**Conflict Resolution Flow:**
- List conflicting files
- Ask user how to resolve each conflict
- After resolution, commit: `git commit -m "Merge <worktree-name> into develop"`

### Step 7: Ask About Worktree Cleanup

```
✅ Successfully merged <worktree-name> into develop!

Would you like to delete the worktree?

1. Yes - Remove worktree and delete branch
2. No - Keep worktree for now

Enter your choice (1 or 2):
```

**If user chooses "Yes":**

```bash
# Remove worktree
git worktree remove <worktree-name>

# Delete the branch
git branch -d <worktree-name>

echo "Worktree and branch removed successfully"
```

**If user chooses "No":**

```bash
echo "Worktree preserved at: $HOME/.worktree/<worktree-name>"
echo "To remove later, run:"
echo "  git worktree remove <worktree-name>"
echo "  git branch -d <worktree-name>"
```

### Step 8: Report Completion

```
✅ Merge complete!

Summary:
- Merged: <worktree-name>
- Into: develop
- Conflicts: <none/resolved>
- Worktree: <deleted/preserved>

Next steps:
- Review changes: git log --oneline -5
- Push to remote: git push origin develop
```

## Command Reference

| Action | Command |
|--------|---------|
| List worktrees | `git worktree list` |
| Get current branch | `git rev-parse --abbrev-ref HEAD` |
| Switch branch | `git switch develop` |
| Pull latest | `git pull --ff-only` |
| Merge worktree | `git merge --no-ff <worktree-name>` |
| Check status | `git status` |
| Abort merge | `git merge --abort` |
| Remove worktree | `git worktree remove <worktree-name>` |
| Delete branch | `git branch -d <worktree-name>` |

## Interactive Prompts Summary

| Step | Prompt | Validation |
|------|--------|------------|
| 1 | Worktree name | Must exist in worktree list |
| 2 | Continue if different base | Yes/No confirmation |
| 3 | Resolve conflicts | User-guided or abort |
| 4 | Delete worktree | 1 (Yes) or 2 (No) |

## Error Handling

| Situation | Action |
|-----------|--------|
| Worktree doesn't exist | Stop, show available worktrees |
| Not in git repo | Stop, report error |
| Merge conflicts | Show status, guide resolution |
| Can't remove worktree | Report error, branch cleanup optional |
| Uncommitted changes | Warn user, offer stash |

## Common Mistakes

### Forgetting to pull before merge
- **Problem:** Merge creates unnecessary conflicts
- **Fix:** Always pull --ff-only before merging

### Deleting worktree with uncommitted changes
- **Problem:** Lost work
- **Fix:** Check worktree status before removal

### Not using --no-ff
- **Problem:** Fast-forward loses merge commit history
- **Fix:** Always use --no-ff for worktree merges

## Example Workflow

```
User: 将feat/bill合并

You: I'm using the merge-worktree skill to merge changes from the worktree.

[Verify worktree exists: feat/bill at ~/.worktree/feat/bill]
[Check base branch: derived from develop ✓]

[Switch to develop]
[Pull latest changes]

[Merge feat/bill]
✅ Merge successful (no conflicts)

[Ask about cleanup]
User: 1 (Yes)

[Remove worktree]
[Delete branch]

✅ Merge complete!

Summary:
- Merged: feat/bill
- Into: develop
- Conflicts: none
- Worktree: deleted

Next steps:
- Review changes: git log --oneline -5
- Push to remote: git push origin develop
```

## Red Flags

**Never:**
- Merge without verifying worktree exists
- Delete worktree without user confirmation
- Force delete branches without checking
- Ignore merge conflicts

**Always:**
- Check worktree status before removal
- Use --no-ff for merge commits
- Pull latest before merging
- Report clear status at each step

## Integration

**Called by:**
- Users who want to merge worktree changes back to main branch
- Workflows completing feature branches

**Does NOT include:**
- Pushing to remote (user's responsibility)
- Running tests after merge (use separate skills)
- Squashing commits (user's choice)
