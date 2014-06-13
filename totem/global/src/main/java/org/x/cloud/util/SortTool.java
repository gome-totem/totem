package org.x.cloud.util;

import java.util.ArrayList;
import java.util.Comparator;

public class SortTool<T> {
	private Comparator<T> comparator = null;

	public SortTool(Comparator<T> c) {
		comparator = c;
	}

	public void QuickSort(ArrayList<T> _list, int left, int right) {
		int lo = left;
		int hi = right;
		if (lo >= hi) {
			return;
		} else if (lo == hi - 1) {
			/** sort a two element list by swapping if necessary */
			if (comparator.compare(_list.get(lo), _list.get(hi)) > 0) {
				T _object = _list.get(lo);
				_list.set(lo, _list.get(hi));
				_list.set(hi, _object);
			}
			return;
		}
		/*
		 * Pick a pivot and move it out of the way
		 */
		T pivot = _list.get((lo + hi) / 2);
		_list.set((lo + hi) / 2, _list.get(hi));
		_list.set(hi, pivot);

		while (lo < hi) {
			/*
			 * Search forward from a[lo] until an element is found that is
			 * greater than the pivot or lo >= hi
			 */
			while (comparator.compare(_list.get(lo), pivot) <= 0 && lo < hi) {
				lo++;
			}

			/*
			 * Search backward from a[hi] until element is found that is less
			 * than the pivot, or lo >= hi
			 */
			while (comparator.compare(pivot, _list.get(hi)) <= 0 && lo < hi) {
				hi--;
			}

			/*
			 * Swap elements a[lo] and a[hi]
			 */
			if (lo < hi) {
				T _temp = _list.get(lo);
				_list.set(lo, _list.get(hi));
				_list.set(hi, _temp);
			}
		}

		/*
		 * Put the median in the "center" of the list
		 */
		_list.set(right, _list.get(hi));
		_list.set(hi, pivot);

		/*
		 * Recursive calls, elements a[left] to a[lo-1] are less than or equal
		 * to pivot, elements a[hi+1] to a[right] are greater than pivot.
		 */
		QuickSort(_list, left, lo - 1);
		QuickSort(_list, hi + 1, right);
	}

	public void QuickSort(T[] _list, int left, int right) {
		int lo = left;
		int hi = right;
		if (lo >= hi) {
			return;
		} else if (lo == hi - 1) {
			/** sort a two element list by swapping if necessary */
			if (comparator.compare(_list[lo], _list[hi]) > 0) {
				T _object = _list[lo];
				_list[lo] = _list[hi];
				_list[hi] = _object;
			}
			return;
		}
		/*
		 * Pick a pivot and move it out of the way
		 */
		T pivot = _list[(lo + hi) / 2];
		_list[(lo + hi) / 2] = _list[hi];
		_list[hi] = pivot;

		while (lo < hi) {
			/*
			 * Search forward from a[lo] until an element is found that is
			 * greater than the pivot or lo >= hi
			 */
			while (comparator.compare(_list[lo], pivot) <= 0 && lo < hi) {
				lo++;
			}

			/*
			 * Search backward from a[hi] until element is found that is less
			 * than the pivot, or lo >= hi
			 */
			while (comparator.compare(pivot, _list[hi]) <= 0 && lo < hi) {
				hi--;
			}

			/*
			 * Swap elements a[lo] and a[hi]
			 */
			if (lo < hi) {
				T _temp = _list[lo];
				_list[lo] = _list[hi];
				_list[hi] = _temp;
			}
		}

		/*
		 * Put the median in the "center" of the list
		 */
		_list[right] = _list[hi];
		_list[hi] = pivot;

		/*
		 * Recursive calls, elements a[left] to a[lo-1] are less than or equal
		 * to pivot, elements a[hi+1] to a[right] are greater than pivot.
		 */
		QuickSort(_list, left, lo - 1);
		QuickSort(_list, hi + 1, right);
	}

}
