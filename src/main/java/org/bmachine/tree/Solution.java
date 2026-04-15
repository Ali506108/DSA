package org.bmachine.tree;

import org.bmachine.ListNode;
import org.bmachine.Main;
import org.bmachine.TreeNode;

import java.util.*;
import java.util.concurrent.Executors;

public class Solution {

    static void main() {
        try(var vThread = Executors.newVirtualThreadPerTaskExecutor()) {
            vThread.submit(() -> IO.println("Java"));

        }
    }

    public static int leastInterval(char[] tasks , int n) {
        int[] count = new int[26];

        for(char task : tasks) {
            count[task - 'A']++;
        }

        Arrays.sort(count);

        int maxf = count[25];
        int idle = (maxf -1) *n;

        for(int i = 24 ; i >= 0; i--){
            idle -= Math.min(count[i] , maxf-1);
        }

        return Math.max(0 , idle) + tasks.length;

    }

    public boolean carPooling(int[][] trips, int capacity) {
        Arrays.sort(trips , (a , b) -> a[1] - b[1]);

        for(int i = 0; i<trips.length ; i++) {
            int curPas =trips[i][0];
            for (int j = 0; j<i;j++) {
                if(trips[j][2]> trips[i][1]) {
                    curPas+=trips[j][0];
                }
            }
            if (curPas > capacity) {
                return false;
            }
        }
        return true;
    }


    public int countDays(int days, int[][] meetings) {
        Arrays.sort(meetings , (a ,b) -> a[0]-b[0]);

        int freeDays = 0;
        int lastCoverDay = 0;

        for (int[] meeting : meetings) {
            int start = meeting[0];
            int endDay = meeting[1];

            if(lastCoverDay < start) {
                freeDays += start - lastCoverDay-1;
            }

            lastCoverDay = Math.max(lastCoverDay , endDay);
        }
        freeDays+=days -lastCoverDay;

        return freeDays;
    }

    public List<Integer> partitionLabels(String s) {
        List<Integer> res = new ArrayList<>();
        int[] map = new int[26];

        for (int i = 0; i < s.length(); i++) {
            map[s.charAt(i)-'a'] = i;
        }

        int last = 0 , start = 0;

        for (int i = 0; i < s.length(); i++) {
            last = Math.max(last , map[s.charAt(i) - 'a']);
            if (last == i) {
                res.add(last - start +1);
                start = last+1;

            }
        }
        return res;
    }


    public int compress(char[] chars) {
        int n = chars.length;
        int i = 0;
        int index = 0;

        while (i < n) {
            char ch = chars[i];
            int count = 0;

            while (i < n && chars[i] == ch) {
                i++;
                count++;
            }

            chars[index++] = ch;

            if (count > 1) {
                String s = String.valueOf(count);
                for (char c : s.toCharArray()) {
                    chars[index++] = c;
                }
            }
        }

        return index;
    }

    public static boolean isPalindrome_two_edu(String string) {

        // Replace this placeholder return statement with your code
        int left = 0, right = string.length() -1;

        while (left < right) {
            if(string.charAt(left) != string.charAt(right)) {
                return palindromeHelper(left+1 , right , string) ||
                        palindromeHelper(left , right-1 , string);
            }else{
                left++;
                right--;
            }
        }

        return true;
    }

    private static boolean palindromeHelper(int i , int j , String s) {
        while(i < j) {
            if(s.charAt(i) != s.charAt(j) ) {
                return false;
            }

            i++;
            j--;
        }

        return true;
    }


    public static boolean validWordAbbreviation_str(String word, String abbr) {
        int left = 0 , right = 0;

        while(left < word.length() && right < abbr.length()) {

            if(Character.isDigit(abbr.charAt(right))) {
                if(abbr.charAt(right) == '0') return false;
                int num = 0;
                while (right < abbr.length() && Character.isDigit(abbr.charAt(right))) {
                    num = num * 10 + (abbr.charAt(right) - '0');
                    right++;
                }

                right += num;
            }else{
                if(abbr.charAt(right++) != word.charAt(left++)) {
                    return false;
                }
            }
        }

        return left == word.length() && right == abbr.length();
    }


    // "internationalization" to "13iz4n"
    public static boolean validWordAbbreviation(String word, String abbr) {
        if(word == null || abbr == null) return false;
        int i = 0 , j = 0;
        while(i < word.length() && j < abbr.length()) {

            if(Character.isDigit(abbr.charAt(j))) {
                if(abbr.charAt(j) == '0') return false;
                int num = 0;

                while(j < abbr.length() && Character.isDigit(abbr.charAt(j))) {
                    num = num * 10 + (abbr.charAt(j) - '0');
                    j++;
                }

                i += num;

            }else{

                if(abbr.charAt(j++) != word.charAt(i++)) {
                    return false;
                }

            }

        }

        return i == word.length() && j == abbr.length();
    }


    public static boolean isPalindrome_two(String s) {
        int left = 0 , right = s.length() -1;


        while(left < right) {
            while(left < right && !Character.isLetterOrDigit(s.charAt(left))) {
                left++;
            }

            while(left < right && !Character.isLetterOrDigit(s.charAt(right))) {
                right--;
            }

            if(Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
                return false;
            }

            left++;
            right--;
        }

        return true;
    }


    public static boolean isPalindrome(String s) {
        int left = 0, right = s.length() - 1;

        while (left < right) {
            while (left < right && !Character.isLetterOrDigit(s.charAt(left))) {
                left++;
            }

            while (left < right && !Character.isLetterOrDigit(s.charAt(right))) {
                right--;
            }

            if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
                // write your code here
                return false;
            }

            left++;
            right--;
        }

        return true;
    }


    public int appendCharacters_data(String s, String t){
        int i = 0;
        int j = 0;
        int rem = 0;
        while(i < s.length() &&  j <t.length()) {
            if(s.charAt(i) == t.charAt(i) ) {
                    j++;
            }
            i++;
        }
        if(j == t.length()) {
            return 0;
        }else{
            rem += t.length() -j;
        }

        return rem;
    }

    public int appendCharacters(String s, String t) {
        if(t.isEmpty()) {
            return 0;
        }
        int tIdx = 0 ;

        for(char c : s.toCharArray()) {
            if(c == t.charAt(tIdx)) {
                tIdx++;
                if(tIdx == t.length()) {
                    return 0;
                }
            }
        }


        return t.length() - tIdx;
    }

    // input : "We love Python "  , output : "Python love We"
    public static String reverseWords(String sentence) {
        String[] words = sentence.trim().split("\\s+");
        int left = 0 , right = words.length-1;

        while(left < right) {
            String temp = words[left];
            words[left++] = words[right];
            words[right--] = temp;
        }
        return String.join(" " , words);
    }

    public EduTreeNode LowestCommonAncestor(EduTreeNode p, EduTreeNode q) {
        EduTreeNode left = p;
        EduTreeNode right = q;

        while(left != right) {
            left = (left == null) ? q : left.parent;
            right = (right == null) ? p : right.parent;

        }

        return left;
    }

    public int kthSmallest_second(TreeNode node , int k) {
        if(node == null) return 0;

        int n =0;
        Stack<TreeNode> stack = new Stack<>();
        TreeNode cur = node;

        while(!stack.isEmpty() || cur != null) {
            while(cur != null) {
                stack.push(cur);
                cur = cur.left;
            }

            cur= stack.pop();
            n+=1;

            if(n== k) return cur.val;

            cur = cur.right;
        }

        return -1;
    }

    public List<List<Integer>> levelOrder(TreeNode root) {
        if(root == null) return new ArrayList<>();
        List<List<Integer>> res = new ArrayList<>();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);

        while(!queue.isEmpty()) {
            int size = queue.size();
            List<Integer> currentLevel = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();

                if(node != null) {
                    currentLevel.add(node.val);
                    queue.add(node.left);
                    queue.add(node.right);
                }
            }
            if(currentLevel.size() > 0) res.add(currentLevel);

        }
        return res;
    }


    public TreeNode insertIntoBST(TreeNode root, int val) {
        if(root ==null) return new TreeNode(val);


        TreeNode cur = root;

        while (true) {
            if(cur.val < val) {
                if(cur.right == null) {
                    cur.right = new TreeNode(val);
                    break;
                }else{
                    cur = cur.right;
                }
            }else {
                if(cur.left == null) {
                    cur.left = new TreeNode(val);
                    break;
                }else{
                    cur = cur.left;
                }
            }
        }
        return cur;
    }

    public static int[] sortedSquares(int[] nums) {
        int n = nums.length;
        int[] res = new int[n];
        int left = 0 ,  right = nums.length-1;
        int idx = n-1;

        while (left <= right) {
            if(Math.abs(nums[left]) < Math.abs(nums[right])) {
                res[idx--] = nums[right] * nums[right];
                right--;
            }else{
                res[idx--] = nums[left] * nums[left];
                left++;
            }
        }

        return res;
    }

    public static ListNode reverseTheList(ListNode head) {
        //ListNode slow = head , fast = head;
        // my input is data like that 1 -> 2 -> 3 -> 4 -> 5
        ListNode prev = null;

        while (head != null) {
            ListNode next = head.next;
            head.next = prev;
            prev = head;
            head = next;
        }

        return head;
    }



    public static boolean circularArrayLoop(int[] nums) {

        for (int i = 0; i < nums.length; i++) {
            int slow = i , fast = i;

            do {
                slow = getNextIndex(slow , nums);
                fast = getNextIndex(getNextIndex(fast , nums) , nums);
            }while (slow != fast);
        }

        return true;
    }

    private static int getNextIndex(int num , int[] nums) {
        int n = nums.length;
        int nextIndex = (num + nums[num]) % n;
        return nextIndex >= 0 ? nextIndex : nextIndex + n;
    }


    public static int maxProfit(int[] prices) {
        int minPrice = Integer.MAX_VALUE;
        int max = 0;


        for(int price : prices) {
            if(price < minPrice) {
                minPrice = price;
            } else if (price-minPrice > max) {
                max = price-minPrice;
            }
        }
        return max;
    }

    public static int findLongestSubstring(String str) {
        int left =0 , right =0;
        int res = 0;
        Set<Character> set = new HashSet<>();


        while (right < str.length()) {
            if (set.contains(str.charAt(right))) {
                set.remove(str.charAt(left++));
            }else{
                set.add(str.charAt(right++));
                res = Math.max(res , right - left);
            }
        }

        return res;
    }

    public static int longestRepeatingCharacterReplacement(String s, int k) {
        int left = 0 ,  right = 0;
        int[] alp = new int[26];
        int res = 0 , maxCount = 0;

        while (right < s.length()) {
            int curIndex = s.charAt(right) - 'A';
            alp[curIndex]++;

            maxCount = Math.max(maxCount , alp[curIndex]);

            if(right -left +1-maxCount >k){
                alp[s.charAt(left) - 'A']--;
                left++;
            }
            res = Math.max(res , right - left +1);
            right++;
        }
        return res;
    }

    public int totalFruit(int[] fruits) {
        int left = 0 , right = 0;
        Map<Integer , Integer> map = new HashMap<>();
        int res = 0;

        while (right < fruits.length) {
            map.put(fruits[right] , map.getOrDefault(fruits[right] , 0) +1);

            while (map.size() > 2) {
                map.put(fruits[left] , map.get(fruits[left]) -1);
                if(map.get(fruits[left]) == 0) {
                    map.remove(fruits[left]);
                }
                left++;
            }

            res = Math.max(res , right - left +1);
            right++;
        }

        return res;
    }

    public List<String> findRepeatedDnaSequences(String s) {
        Map<String , Integer> map = new HashMap<>();
        List<String> repeated = new ArrayList<>();

        for (int i = 0; i <s.length(); i++) {
            String cur = s.substring(i , i+10);

            int newCount = map.merge(cur , 1 , Integer::sum);

            if(newCount ==2) repeated.add(cur);
        }

        return repeated;
    }


    public int MaxProfit_(int[] prices) {
        int left = 0 , res = 0;
        for(int right = 0 ; right < prices.length ; right++){
            int profit = prices[right] - prices[left];
            if (profit > 0) {
                res = Math.max(res , profit);
            }else {
                left = right;
            }
        }

        return res;
    }


    public static int maxFrequency(int[] nums, int k) {
        Arrays.sort(nums);
        int left = 0 , right = 0;
        long total = 0;
        int res = 1;

        while (right < nums.length) {
            total += nums[right];

            while ( (long) nums[right] * (right - left +1) - total > k) {
                total -= nums[left];
                left++;
            }

            res = Math.max(res , right - left +1);
            right++;
        }

        return res;
    }

    public static double findMaxAverage_copy(int[] nums, int k) {
        int sum = 0;

        for (int i = 0; i < k; i++) {
            sum += nums[i];
        }

        int maxSum = sum;

        for (int i = k; i < nums.length; i++) {
            sum += nums[i] - nums[i-k];
            maxSum = Math.max(maxSum , sum);
        }

        return (double) maxSum / k;
    }


    private static int res = Integer.MIN_VALUE;
    public int maxPathSum(TreeNode root) {
        res = root.val;
        getMaxSum(root);
        return res;
    }

    private int getMaxSum(TreeNode root) {
        if (root == null) return 0;

        int left = getMaxSum(root.left);
        int right = getMaxSum(root.right);
        res = Math.max(res , root.val + left + right);

        return Math.max( 0 , Math.max(root.val , Math.max(
                root.val + left , root.val + right
        )));
    }

    public static boolean detectCycle(ListNode head) {
        if(head == null) return false;

        ListNode slow = head , fast = head;

        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;

            if(fast == slow) return true;
        }
        return false;
    }

    public static boolean isHappyNumber(int n) {
        int slow = n;
        int fast = n;

        do{
            slow = getNext(slow);
            fast = getNext(getNext(fast));
        }while (slow != fast);

        return slow ==1;
    }


    public static int middleOfTheArray(int[] nums) {
        int slow = 0 , fast = 0;

        while (fast < nums.length-1) {

            slow++;
            fast +=2;

        }
        return slow;
    }


    public static boolean palindrome(ListNode head) {
        ListNode slow = head , fast = head;

        while (fast !=null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        ListNode prev = null;
        while (slow != null) {
            ListNode next = slow.next;
            slow.next = prev;
            prev = slow;
            slow =next;
        }

        while (prev != null) {
            if (prev.val != head.val) {
                return false;
            }
            prev = prev.next;
            head = head.next;
        }

        return true;
    }



    public static ListNode middleNode(ListNode head) {
        ListNode slow = head , fast = head;

        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        return slow;
    }

    private static int getNext(int n) {
        int sum = 0;

        while (n != 0) {
            sum += (n% 10) * (n%10);
            n/=10;
        }

        return sum;
    }




    public static ListNode reverseTheList_rec(ListNode head) {

        if(head == null || head.next == null) return head;

        ListNode result = reverseTheList_rec(head.next);
        head.next.next = head;
        head.next = null;

        return result;
    }


    // input data : [2,3,5,7] -> output => 9 => combination of 2 and 7
    public static int twinSum(ListNode head) {
        ListNode slow = head , fast = head;
        int result = 0;

        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        ListNode prev = null;

        while (slow != null) {
            ListNode next = slow.next;
            slow.next = prev;
            prev = slow;
            slow = next;
        }

        ListNode left = head , right = prev;

        while (right != null) {
            result = Math.max(result, left.val + right.val);
            left = left.next;
            right = right.next;
        }

        return result;
    }


    // input : [1 ,2 ,3 ,4, 4, 5]
    // in my output i need to return 4
    // what if we have input like that : [1,3,6,2,7,3,5,4]
    public static int findDuplicate(int[] nums) {

        int slow = 0 , fast = 0;

        do{
            slow = nums[slow];
            fast = nums[nums[fast]];
        }while (slow != fast);


        slow = 0;
        while (slow != fast){
            slow = nums[slow];

            fast = nums[fast];
        }

        return slow;
    }

    public void reverseString(char[] s) {
        // Write your code here
        int left = 0 , right = s.length-1;

        while(left < right) {
            char temp = s[right];
            s[right] = s[left];
            s[left] = temp;
            left++;
            right--;
        }
    }

    public static void rotate(int[] nums, int k) {
        int len = nums.length;
        k = k&len;

        reverse(nums , 0 , len-1);
        reverse(nums , 9, k-1);
        reverse(nums , k , len-1);
    }

    private static void reverse(int[] nums , int left , int right ) {
        while (left < right) {
            int temp = nums[left];
            nums[left] = nums[right];
            nums[right] = temp;

            left++;
            right--;
        }
    }


    public static int[][] intervalsIntersection(int[][] intervalLista, int[][] intervalListb) {
        List<int[]> res = new ArrayList<>();
        int i = 0 , j = 0;

        while (i < intervalLista.length && j < intervalListb.length) {
            int start = Math.max(intervalLista[i][0] , intervalListb[j][0]);
            int end = Math.min(intervalListb[j][1] , intervalLista[i][1]);

            if (start <= end) {
                res.add(new int[]{start ,end});
            }

            if (intervalLista[i][1] > intervalListb[j][1]){
                i++;
            }else {
                j++;
            }
        }

        return res.toArray(new int[res.size()][]);

    }


    public static int maxSum(int[] nums1, int[] nums2) {
        // Replace this placeholder return statement with your code
        int N1 = nums1.length;
        int N2 = nums2.length;
        int mod =(int) 1e9+7;

        int point1 = 0;
        int point2 = 0;

        int sum1 = 0;
        int sum2 = 0;

        while (point1 < N1 || point2 < N2) {
            if(point1 == N1) {
                sum2 += nums2[point2++];
            }else if(point2 == N2) {
                sum1 += nums1[point1++];
            } else if (nums1[point1] < nums2[point2]) {
                sum1 += nums1[point1++];
            } else if (nums1[point1] > nums2[point2]) {
                sum2 += nums2[point2++];
            }else{
                sum1 = sum2 = Math.max(sum1 ,sum2 ) + nums1[point1];
                point1++;
                point2++;
            }
        }

        return (int)(Math.max(sum1 , sum2) % mod);
    }


    public long countSubarrays(int[] nums, int minK, int maxK) {
        long total = 0;

        int lastMinPos =-1;
        int lastMaxPos = -1;
        int lastInvalidPosition = -1;

        for (int i = 0; i < nums.length; i++) {
            if(nums[i ] < minK || nums[i] > maxK) {
                lastInvalidPosition = i;
            }

            if(nums[i] == minK) lastMinPos = i;

            if(nums[i] == maxK) lastMaxPos = i;

            int leftMostValidStart = Math.min(lastMinPos , lastMaxPos);
            int validSubarraysCount = Math.max(0 , leftMostValidStart - lastInvalidPosition);

            total += validSubarraysCount;
        }

        return total;
    }

    public static int[] sortColors (int[] colors) {
        int left = 0 , low = 0;
        int right = colors.length -1;

        while (left <= right) {

            if(colors[left] == 0) {
                swap(colors, left++, low++);
            }else if(colors[left] == 1) {
                left++;
            }else if(colors[left] == 2){
                swap(colors, left, right--);
            }
        }

        return colors;
    }

    private static void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }


    int index = 0;
        HashMap<Integer , Integer > map = new HashMap<>();

        public TreeNode buildTree(int[] preorder, int[] inorder) {
            for (int i = 0; i < inorder.length; i++) {
                map.put(inorder[i] , i);
            }

            return dfs(preorder , 0 , preorder.length - 1);
        }

        public TreeNode dfs(int[] preorder , int l , int r) {
            if(l > r) return null;

            int root_val = preorder[index++];
            TreeNode node = new TreeNode(root_val);

            int mid = map.get(root_val);

            node.left = dfs(preorder ,l , mid-1);
            node.right = dfs(preorder , mid+1 , r);
            return node;
        }

    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if(root == null || p == null || q == null) return null;

        if(root.val > p.val && root.val > q.val) {
            return lowestCommonAncestor(root.left , p , q);
        }else if(root.val < p.val && root.val < q.val) {
            return lowestCommonAncestor(root.right, p, q);
        }else{
            return root;
        }
    }

    public int kthSmallest(TreeNode root, int k) {
        if(root == null) return -1;

        int n = 0;
        Stack<TreeNode> stack = new Stack<>();
        TreeNode curr = root;

        while (!stack.isEmpty() || curr != null) {
            while(curr != null) {
                stack.push(curr);
                curr = curr.left;
            }

            curr = stack.pop();
            n+=1;

            if(n == k) return curr.val;
            curr = curr.right;
        }

        return -1;
    }

    public boolean isValidBST(TreeNode root) {
        if(root == null) return false;

        return dfs_validate(root ,Integer.MIN_VALUE , Integer.MAX_VALUE);
    }

    private boolean dfs_validate(TreeNode root , int left , int right ) {
        if(root == null) return true;
        if(root.val >= right || root.val <= left) return false;

        return dfs_validate(root.right , root.val , right) &&
                dfs_validate(root.left , left ,root.val);
    }


//    private boolean ValidateBst(TreeNode node, long min, long max) {
//        if(node == null) return true;
//        if(node.val >= max || node.val <= min) return false;
//
//        return ValidateBst(node.left, min, node.val) && ValidateBst(node.right, node.val, max);
//
//    }
//
    public int goodNodes(TreeNode root) {
        if(root == null) return 0;

        return goodNodeHelper(root , root.val);
    }

    private int goodNodeHelper(TreeNode node , int maxElem) {

        if(maxElem <= node.val) {
            return 1 + goodNodeHelper(node.left , node.val ) + goodNodeHelper(node.right , node.val);
        }else{
            return goodNodeHelper(node.left , maxElem ) + goodNodeHelper(node.right , maxElem);
        }
    }


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
