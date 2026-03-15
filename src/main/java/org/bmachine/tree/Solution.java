package org.bmachine.tree;

import org.bmachine.TreeNode;

import java.util.*;

public class Solution {

    public List<Integer> rightSideView(TreeNode root) {
        List<Integer> result = new ArrayList<>();

        Queue<TreeNode> queue = new LinkedList<>();

        if(root != null) queue.add(root);

        while(!queue.isEmpty()) {
            TreeNode lastNode = queue.peek();

            result.add(lastNode.val);

            int size = queue.size();

            for (int i = 0 ; i< size ; i++) {
                TreeNode node = queue.poll();

                if(node.right != null) queue.add(node.right);
                if(node.left != null) queue.add(node.left);

            }
        }

        return result;

    }

    public void inOrder(TreeNode node ) {
        if(node != null) {
            inOrder(node.left);
            IO.println("value is : " + node.val);
            inOrder(node.right);
        }
    }

    public TreeNode search(TreeNode node , int val) {
        if(node == null || node.val == val ) return node;

        if(val < node.val) {
            return search(node.left , val);
        }else{
            return search(node.right , val);
        }
    }

    public TreeNode s(TreeNode node , int val) {
        TreeNode curr = node;

        while(curr != null ) {
            if(curr.val == val) return curr;

            if(curr.val < val) curr = curr.right;
            else curr = curr.left;
        }

        return null;
    }


    public List<Integer> preorderTraversal(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
        TreeNode node = root;

        while (node != null || !stack.isEmpty()) {
            if(node != null) {

                res.add(node.val);
                stack.push(node.right);
                node = node.left;

            }else{
                node = stack.pop();
            }
        }

        return res;
    }


    public List<Integer> inorderTraversal(TreeNode root) {
        List<Integer> results = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
        TreeNode node = root;

        while(node != null || !stack.isEmpty()) {

            while(node != null) {
                stack.push(node);
                node = node.left;
            }

            node = stack.pop();
            results.add(node.val);
            node = node.right;

        }

        return results;
    }


    public List<Integer> postorderTraversal(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
        TreeNode node = root;

        while(node != null || !stack.isEmpty()) {
            if(node != null) {
                res.add(node.val);
                stack.push(node);
                node = node.right;
            }else{
                node = stack.pop();
                node = node.left;
            }
        }
        Collections.reverse(res);
        return res;
    }


    public int maxDepthII(TreeNode treeNode) {
        Queue<TreeNode> queue = new LinkedList<>();

        if(treeNode != null) queue.add(treeNode);

        int level = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();


            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();

                if(node.left != null) queue.add(node.left);
                if(node.right != null) queue.add(node.right);


            }
            level++;
        }

        return level;
    }


    // The same tree
    // Time complexity O(n) , Space complexity O(n)
    // The Best case is O(log(n)) when the tree is balanced
    // The worst case is O(n) when the tree is disbalanced
    public boolean isSameTree(TreeNode p, TreeNode q) {
        if(p == null && q == null) return true;

        if(p != null && q != null && p.val == q.val){
            return isSameTree(p.left , q.left) && isSameTree(p.right , q.right);
        }else{
            return false;
        }
    }

    private int dfs_helper_balanced(TreeNode node ) {
        if(node == null) return 0;

        int left = dfs_helper_balanced(node.left);
        if(left == -1) return -1;

        int right = dfs_helper_balanced(node.right);
        if(right == -1) return -1;

        if(Math.abs(left - right) > 1) return -1;

        return 1 + Math.max(left , right);
    }

    public int diameterOfBinaryTree(TreeNode root) {
        int[] res = new int[1];
        dfs_long(root , res);

        return res[0];

    }

    private int dfs_long(TreeNode root , int[] res) {
        if(root == null) return 0;

        int left = dfs_long(root.left , res);
        int right = dfs_long(root.right , res);

        res[0] = Math.max( res[0] ,left + right);

        return 1 + Math.max(left , right);

    }


    public int maxDepth(TreeNode root) {
        return root == null
                ? 0 : 1 + Math.max(maxDepth(root.left) , maxDepth(root.right));
    }

    private void dfs(TreeNode node) {
        if(node == null) return;

        dfs(node.left);
        dfs(node.right);

        TreeNode tmp = node.left;
        node.left = node.right;
        node.right = tmp;
    }

    public TreeNode invertTree(TreeNode root) {
        dfs(root);

        return root;
    }

}
