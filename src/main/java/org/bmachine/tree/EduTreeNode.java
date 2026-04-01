package org.bmachine.tree;

class EduTreeNode {
    int data;
    EduTreeNode left;
    EduTreeNode right;
    EduTreeNode parent;

    EduTreeNode(int data ) {
        this.data = data;
        this.left = null;
        this.right = null;
        this.parent = null;

    }
}
