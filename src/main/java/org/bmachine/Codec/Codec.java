package org.bmachine.Codec;

import org.bmachine.TreeNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Codec {

    public String serialize(TreeNode root) {
        List<String> res = new ArrayList<>();
        dfsSerialize(root ,res);
        return String.join("," ,res);
    }

    private void dfsSerialize(TreeNode root ,List<String> res) {
        if(root == null) {
            res.add("N");
            return;
        }

        res.add(String.valueOf(root.val));
        dfsSerialize(root.left , res);
        dfsSerialize(root.right , res);
    }

    public TreeNode deserialize(String data) {
        String[] res = data.split(",");
        int[] i = {0};

    }

    private TreeNode dfsDesrilize(String[] res , int[] i) {
        if(res[i[0]].equals("N")) {
            i[0]++;
            return null;
        }

        TreeNode node = new TreeNode(Integer.parseInt(res[i[0]]));
        node.left = dfsDesrilize(res , i);
        node.right = dfsDesrilize(res , i);
        return node;
    }


    public String serialized_bfs(TreeNode root) {
        if(root == null) return "N";
        StringBuilder builder = new StringBuilder();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            TreeNode poll = queue.poll();
            if (poll == null) {
                builder.append("N,");
            }else{
                builder.append(poll.val).append(",");
                queue.add(poll.left);
                queue.add(poll.right);
            }
        }

        return builder.toString();
    }

    public TreeNode deserialized_bfs(String data) {
        String[] vals = data.split(",");
        if(vals[0].equals("N"))return null;
        TreeNode root = new TreeNode(Integer.parseInt(vals[0]));
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        int index = 1;


        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();

            if(!vals[index].equals("N")) {
                node.left = new TreeNode(Integer.parseInt(vals[index]));
                queue.add(node.left);
            }
            index++;
            if(!vals[index].equals("N")) {
                node.right = new TreeNode(Integer.parseInt(vals[index]));
                queue.add(node.right);
            }
            index++;
        }

        return root;
    }




}