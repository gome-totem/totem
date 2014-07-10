package org.z.common.hash;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.z.global.interfaces.KetamaNodeIntf;


/**
 * This is an implementation of the Ketama consistent hash strategy from
 * last.fm. This implementation may not be compatible with libketama as hashing
 * is considered separate from node location.
 * 
 * Note that this implementation does not currently supported weighted nodes.
 * 
 * @see <a href="http://www.last.fm/user/RJ/journal/2007/04/10/392555/">RJ's
 *      blog post</a>
 */
public final class KetamaLocator<T> {
	final SortedMap<Long, T> ketamaNodes;
	final Collection<T> allNodes;
	final HashAlgorithm hashAlg;
	final KetamaNodeIntf<T> ketamaNode;

	public KetamaLocator(List<T> nodes, KetamaNodeIntf<T> ketamaNode,
			HashAlgorithm alg) {
		super();
		allNodes = nodes;
		this.ketamaNode = ketamaNode;
		hashAlg = alg;
		ketamaNodes = new TreeMap<Long, T>();

		int numReps = ketamaNode.getNodeRepetitions();
		for (T node : nodes) {
			// Ketama does some special work with md5 where it reuses chunks.
			if (alg == HashAlgorithm.KETAMA_HASH) {
				for (int i = 0; i < numReps / 4; i++) {
					byte[] digest = HashAlgorithm.computeMd5(ketamaNode
							.getKeyForNode(node, i));
					for (int h = 0; h < 4; h++) {
						Long k = ((long) (digest[3 + h * 4] & 0xFF) << 24)
								| ((long) (digest[2 + h * 4] & 0xFF) << 16)
								| ((long) (digest[1 + h * 4] & 0xFF) << 8)
								| (digest[h * 4] & 0xFF);
						ketamaNodes.put(k, node);
					}
				}
			} else {
				for (int i = 0; i < numReps; i++) {

					ketamaNodes.put(hashAlg.hash(ketamaNode.getKeyForNode(node,
							i)), node);
				}
			}
		}
		assert ketamaNodes.size() == numReps * nodes.size();
	}



	public Collection<T> getAll() {
		return allNodes;
	}

	public T getPrimary(final String k) {
		T rv = getNodeForKey(hashAlg.hash(k));
		assert rv != null : "Found no node for key " + k;
		return rv;
	}

	long getMaxKey() {
		return ketamaNodes.lastKey();
	}

	T getNodeForKey(long hash) {
		final T rv;
		if (!ketamaNodes.containsKey(hash)) {
			// Java 1.6 adds a ceilingKey method, but I'm still stuck in 1.5
			// in a lot of places, so I'm doing this myself.
			SortedMap<Long, T> tailMap = ketamaNodes.tailMap(hash);
			if (tailMap.isEmpty()) {
				hash = ketamaNodes.firstKey();
			} else {
				hash = tailMap.firstKey();
			}
		}
		rv = ketamaNodes.get(hash);
		return rv;
	}

	public Iterator<T> getSequence(String k) {
		return new KetamaIterator(k, allNodes.size());
	}

	class KetamaIterator implements Iterator<T> {

		final String key;
		long hashVal;
		int remainingTries;
		int numTries = 0;

		public KetamaIterator(final String k, final int t) {
			super();
			hashVal = hashAlg.hash(k);
			remainingTries = t;
			key = k;
		}

		private void nextHash() {
			// this.calculateHash(Integer.toString(tries)+key).hashCode();
			long tmpKey = hashAlg.hash((numTries++) + key);
			// This echos the implementation of Long.hashCode()
			hashVal += (int) (tmpKey ^ (tmpKey >>> 32));
			hashVal &= 0xffffffffL; /* truncate to 32-bits */
			remainingTries--;
		}

		public boolean hasNext() {
			return remainingTries > 0;
		}

		public T next() {
			try {
				return getNodeForKey(hashVal);
			} finally {
				nextHash();
			}
		}

		public void remove() {
			throw new UnsupportedOperationException("remove not supported");
		}

	}
}
