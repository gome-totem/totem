package org.z.common.tree;


import java.util.ArrayList;


public class RadixTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		RadixTreeImpl<String> trie;
		trie = new RadixTreeImpl<String>();
		trie.insert("xbox 360", "xbox 3604");
		trie.insert("360", "xbox 3600");
		trie.insert("xbox", "xbox");
		trie.insert("xbox 360 games", "xbox 360 games");
		trie.insert("xbox games", "xbox games");
		trie.insert("xbox xbox 360", "xbox xbox 360");
		trie.insert("xbox xbox", "xbox xbox");
		trie.insert("xbox 360 xbox games", "xbox 360 xbox games");
		trie.insert("xbox games 360", "xbox games 360");
		trie.insert("xbox 360 360", "xbox 360 360");
		trie.insert("xbox 360 xbox 360", "xbox 360 xbox 360");
		trie.insert("360 xbox games 360", "360 xbox games 360");
		trie.insert("xbox xbox 361", "xbox xbox 361");

		ArrayList<String> items = trie.searchPrefix("360", 10);
		for (int i = 0; i < items.size(); i++) {
			System.out.println(items.get(i));
		}
	}

}
