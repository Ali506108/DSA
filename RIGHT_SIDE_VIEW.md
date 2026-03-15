# Binary Tree Right Side View

## What does it do?

Imagine you are standing on the **right side** of a tree and looking left.
You can only see the **rightmost node at each level**. This algorithm collects those nodes.

---

## The Key Trick

The algorithm uses **BFS (Breadth-First Search)** but with a twist:
- It adds **right child first**, then left child into the queue.
- So the **first node in the queue at each level** is always the rightmost one.

---

## Step-by-Step Walkthrough

Given this tree:
```
        1
       / \
      2   3
       \   \
        5   4
```

**Expected output:** `[1, 3, 4]` — the nodes you see from the right side.

---

### Level 0 — Root

```
Queue: [1]
```
- `peek()` → node `1` is the front → add `1` to result ✅
- Process level (size = 1):
  - Poll `1` → add right(`3`) then left(`2`) to queue

```
Result: [1]
Queue after: [3, 2]
```

---

### Level 1

```
Queue: [3, 2]
```
- `peek()` → node `3` is the front (rightmost!) → add `3` to result ✅
- Process level (size = 2):
  - Poll `3` → add right(`4`), no left
  - Poll `2` → add right(`5`), no left

```
Result: [1, 3]
Queue after: [4, 5]
```

---

### Level 2

```
Queue: [4, 5]
```
- `peek()` → node `4` is the front (rightmost!) → add `4` to result ✅
- Process level (size = 2):
  - Poll `4` → no children
  - Poll `5` → no children

```
Result: [1, 3, 4]
Queue after: []  ← empty, loop ends
```

---

## Why does `peek()` always give the rightmost node?

Because we always add **right before left**:
```java
if(node.right != null) queue.add(node.right);  // right goes in first
if(node.left != null)  queue.add(node.left);   // left goes in second
```

So at the start of each level, the front of the queue is always the rightmost node of that level.

---

## Code Breakdown

```java
public List<Integer> rightSideView(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    Queue<TreeNode> queue = new LinkedList<>();

    if(root != null) queue.add(root);          // 1. Start with root

    while(!queue.isEmpty()) {
        TreeNode lastNode = queue.peek();       // 2. Front = rightmost of this level
        result.add(lastNode.val);              // 3. Add it to result

        int size = queue.size();               // 4. How many nodes in this level?

        for (int i = 0; i < size; i++) {
            TreeNode node = queue.poll();

            if(node.right != null) queue.add(node.right);  // right first!
            if(node.left != null)  queue.add(node.left);   // left second
        }
    }

    return result;
}
```

---

## Complexity

| | |
|---|---|
| Time  | O(n) — visits every node once |
| Space | O(n) — queue holds at most one full level |

---

## Edge Cases

| Input | Output |
|-------|--------|
| `null` (empty tree) | `[]` |
| Single node `[1]` | `[1]` |
| Left-skewed tree | One node per level, all visible |
| Right-skewed tree | One node per level, all visible |
