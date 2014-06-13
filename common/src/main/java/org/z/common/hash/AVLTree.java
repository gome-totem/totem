package org.z.common.hash;


public class AVLTree {
	public static class Node {

		private char getLeftmost() {
			Node curr;
			for (curr = this; curr.left != null; curr = curr.left)
				;
			return curr.element;
		}

		private static int sizeOfSubtree(Node top) {
			if (top == null)
				return 0;
			else
				return 1 + sizeOfSubtree(top.left) + sizeOfSubtree(top.right);
		}

		private void setLeft(Node child) {
			left = child;
			if (child != null)
				child.parent = this;
		}

		private void setRight(Node child) {
			right = child;
			if (child != null)
				child.parent = this;
		}

		private void setHeight() {
			height = Math.max(getHeight(left), getHeight(right)) + 1;
		}

		private static int getHeight(Node node) {
			return node != null ? node.height : -1;
		}

		private Node higherChild() {
			return getHeight(left) < getHeight(right) ? right : left;
		}

		private boolean isHeightBalanced() {
			int balance = getHeight(left) - getHeight(right);
			return balance >= -1 && balance <= 1;
		}

		private char element;

		private Node left;

		private Node right;

		private Node parent;

		private int height;

		public Node(char elem, Node parent) {
			element = elem;
			left = null;
			right = null;
			this.parent = parent;
			height = 0;
		}
	}

	public AVLTree() {
		deletee = null;
		root = null;
	}

	public Node search(char target) {
		int direction = 0;
		Node curr = root;
		do {
			if (curr == null)
				return null;
			direction = target - curr.element;
			if (direction == 0)
				return curr;
			if (direction < 0)
				curr = curr.left;
			else
				curr = curr.right;
		} while (true);
	}

	public int size() {
		return Node.sizeOfSubtree(root);
	}

	public boolean find(char target) {
		int direction = 0;
		Node curr = root;
		do {
			if (curr == null)
				return false;
			direction = target - curr.element;
			if (direction == 0)
				return true;
			if (direction < 0)
				curr = curr.left;
			else
				curr = curr.right;
		} while (true);
	}

	public void insert(char elem) {
		Node ins = insertBST(elem);
		if (ins != null)
			rebalance(ins);
	}

	private Node insertBST(char elem) {
		int direction = 0;
		Node parent = null;
		Node curr = root;
		do {
			if (curr == null) {
				Node ins = new Node(elem, parent);
				if (root == null)
					root = ins;
				else if (direction < 0)
					parent.left = ins;
				else
					parent.right = ins;
				return ins;
			}
			direction = elem - curr.element;
			if (direction == 0)
				return null;
			parent = curr;
			if (direction < 0)
				curr = curr.left;
			else
				curr = curr.right;
		} while (true);
	}

	public void delete(char elem) {
		deleteBST(elem);
		if (deletee != null) {
			rebalance(deletee);
			deletee = null;
		}
	}

	private void deleteBST(char elem) {
		int direction = 0;
		Node curr = root;
		do {
			if (curr == null)
				return;
			direction = elem - curr.element;
			if (direction == 0) {
				Node modified = deleteTopmost(curr);
				Node parent = curr.parent;
				if (curr == root)
					root = modified;
				else if (curr == parent.left)
					parent.left = modified;
				else
					parent.right = modified;
				if (modified != null)
					modified.parent = parent;
				return;
			}
			if (direction < 0)
				curr = curr.left;
			else
				curr = curr.right;
		} while (true);
	}

	private Node deleteTopmost(Node top) {
		if (top.left == null) {
			deletee = top;
			return top.right;
		}
		if (top.right == null) {
			deletee = top;
			return top.left;
		} else {
			top.element = top.right.getLeftmost();
			top.right = deleteLeftmost(top.right);
			return top;
		}
	}

	private Node deleteLeftmost(Node top) {
		Node curr;
		for (curr = top; curr.left != null; curr = curr.left)
			;
		deletee = curr;
		if (curr == top) {
			return top.right;
		} else {
			curr.parent.left = curr.right;
			return top;
		}
	}

	private void rebalance(Node node) {
		for (Node ancestor = node; ancestor != root;) {
			ancestor = ancestor.parent;
			ancestor.setHeight();
			if (!ancestor.isHeightBalanced()) {
				Node greatAncestor = ancestor.parent;
				Node rotated = rotate(ancestor);
				if (ancestor == root)
					setRoot(rotated);
				else if (ancestor == greatAncestor.left)
					greatAncestor.setLeft(rotated);
				else
					greatAncestor.setRight(rotated);
				ancestor = rotated;
			}
		}

	}

	private Node rotate(Node grandparent) {
		Node parent = grandparent.higherChild();
		Node child = parent.higherChild();
		Node a;
		Node b;
		Node c;
		Node t1;
		Node t2;
		Node t3;
		Node t4;
		if (child == parent.left && parent == grandparent.left) {
			a = child;
			b = parent;
			c = grandparent;
			t1 = child.left;
			t2 = child.right;
			t3 = parent.right;
			t4 = grandparent.right;
		} else if (child == parent.right && parent == grandparent.left) {
			a = parent;
			b = child;
			c = grandparent;
			t1 = parent.left;
			t2 = child.left;
			t3 = child.right;
			t4 = grandparent.right;
		} else if (child == parent.right && parent == grandparent.right) {
			a = grandparent;
			b = parent;
			c = child;
			t1 = grandparent.left;
			t2 = parent.left;
			t3 = child.left;
			t4 = child.right;
		} else {
			a = grandparent;
			b = child;
			c = parent;
			t1 = grandparent.left;
			t2 = child.left;
			t3 = child.right;
			t4 = parent.right;
		}
		a.setLeft(t1);
		a.setRight(t2);
		a.setHeight();
		c.setLeft(t3);
		c.setRight(t4);
		c.setHeight();
		b.setLeft(a);
		b.setRight(c);
		b.setHeight();
		return b;
	}

	private void setRoot(Node newRoot) {
		root = newRoot;
		newRoot.parent = null;
	}

	public void print() {
		printSubtree(root, "");
	}

	private static void printSubtree(Node top, String indent) {
		
		if (top == null) {
			System.out.println(indent + "o");
		} else {
			System.out.println(indent + "o-->");
			String childIndent = indent + "    ";
			printSubtree(top.right, childIndent);
			System.out.println(childIndent
					+ top.element
					+ " ("
					+ top.height
					+ ")"
					+ (top.parent != null ? " parent " + top.parent.element
							: ""));
			printSubtree(top.left, childIndent);
		}
	}

	public static void main(String args[]) {
		AVLTree t = new AVLTree();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			t.insert(arg.charAt(0));
		}
		
//		Logger.getSysLogger().message(""+t.find('a')+" " + t.size());
	}

	private Node root;

	private Node deletee;
}