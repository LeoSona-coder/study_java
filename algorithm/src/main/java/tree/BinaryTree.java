package tree;

import java.util.ArrayList;

public class BinaryTree<E> {
    private Node root;

    public BinaryTree(){
        root = null;
    }

    public static void main(String[] args) {
        BinaryTree<Integer> tree = new BinaryTree<>();
        tree.root            = new Node(1);
        tree.root.left       = new Node(2);
        tree.root.right      = new Node(3);
        tree.root.left.left  = new Node(4);
        tree.root.left.right  = new Node(5);

        System.out.println(tree);

    }

    public void printPreOrder(Node node) {
        if (node == null) {
            return;
        }
        System.out.print(node.e + " ");
        printPreOrder(node.left);
        printPreOrder(node.right);
    }

    public static class Node{
        private Node left;
        private Node right;

        private Object e;

        public Node(Object e){
            this.e = e;
        }

        public Node getLeft() {
            return left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public Object getE() {
            return e;
        }

        public void setE(Object e) {
            this.e = e;
        }
    }
}
