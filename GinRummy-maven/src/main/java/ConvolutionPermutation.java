import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

import java.util.List;

public class ConvolutionPermutation {

	static int[][] perms = {
			{0, 1, 2, 3},{0, 1, 3, 2},{0, 2, 1, 3},{0, 2, 3, 1},
			{0, 3, 1, 2},{0, 3, 2, 1},{1, 0, 2, 3},{1, 0, 3, 2},
			{1, 2, 0, 3},{1, 2, 3, 0},{1, 3, 0, 2},{1, 3, 2, 0},
			{2, 0, 1, 3},{2, 0, 3, 1},{2, 1, 0, 3},{2, 1, 3, 0},
			{2, 3, 0, 1},{2, 3, 1, 0},{3, 0, 1, 2},{3, 0, 2, 1},
			{3, 1, 0, 2},{3, 1, 2, 0},{3, 2, 0, 1},{3, 2, 1, 0}};





	public static ArrayList<Integer[]> product(int z, int r) {
		Stack<Integer[]> temp = new Stack<Integer[]>();
		ArrayList<Integer[]> prod = new ArrayList<Integer[]>();
		Integer[] starter = new Integer[r];
		for (int i = 0; i < r; i++)
			starter[i] = -1;
		temp.push(starter);

		while (temp.size() > 0) {
			Integer[] next = temp.pop();
			boolean in_progress = false;
			for (int i = 0; i < r; i++) {
				if (next[i] == -1) {
					in_progress = true;
					for (int j = 0; j < z; j++) {
						Integer[] a = (Integer[]) next.clone();
						a[i] = j;
						temp.push(a);
					}
					break;
				}
			}
			if (!in_progress) {
				prod.add(next);
			}
		}
		return prod;
	}






	public static boolean is_a_sublist(int[] l1, Integer[] rows) {
		for (int offset = 0; offset < rows.length - l1.length + 1; offset++) {
			boolean is_sublist = true;
			for (int i = 0; i < l1.length; i++) {
				if (l1[i] != rows[i + offset]) {
					is_sublist = false;
					break;
				}
			}
			if (is_sublist) {
				return true;
			}
		}
		return false;
	}




	public static List<?> product(List<?>... a) {
        if (a.length >= 2) {
            List<?> product = a[0];
            for (int i = 1; i < a.length; i++) {
                product = product(product, a[i]);
            }
            return product;
        }

        return emptyList();
    }

    private static <A, B> List<?> product(List<A> a, List<B> b) {
        return of(a.stream()
                .map(e1 -> of(b.stream().map(e2 -> asList(e1, e2)).collect(toList())).orElse(emptyList()))
                .flatMap(List::stream)
                .collect(toList())).orElse(emptyList());
    }



	public static <T> List<List<T>> computeCombinations3(List<List<T>> lists) {
		    List<List<T>> currentCombinations = Arrays.asList(Arrays.asList());
		    for (List<T> list : lists) {
		        currentCombinations = appendElements(currentCombinations, list);
		    }
		    return currentCombinations;
		}

		public static <T> List<List<T>> appendElements(List<List<T>> combinations, List<T> extraElements) {
		    return combinations.stream().flatMap(oldCombination
		            -> extraElements.stream().map(extra -> {
		                List<T> combinationWithExtra = new ArrayList<>(oldCombination);
		                combinationWithExtra.add(extra);
		                return combinationWithExtra;
		            }))
		            .collect(Collectors.toList());
		}






//
//		public static ArrayList<Integer> createSequence() {
//			LinkedList<Integer> sequence = new LinkedList<Integer>();
//			sequence.add(0);
//			sequence.add(1);
//			sequence.add(2);
//			sequence.add(3);
//			ArrayList<Integer> permsLeft = new ArrayList<Integer>();
//			for (int i = 1; i < 23; i++)
//				permsLeft.add(i);
//			while (!permsLeft.isEmpty()) {
//				int max = 0;
//				int num = -1;
//				boolean first = false;
//				for (int j = 0; j < 4; j++) {
//					if (j == sequence.getLast()) {
//						continue;
//					}
//					sequence.add(j);
//					int count = 0;
//					for (Integer perm : permsLeft) {
//						count += (is_a_sublist(perms[perm],sequence.toArray()) ? 1 : 0);
//					}
//					sequence.removeLast();
//					if (count > max) {
//						max = count;
//						num = j;
//						first = true;
//					}
//				}
//				for (int j = 0; j < 4; j++) {
//					if (j == sequence.getFirst()) {
//						continue;
//					}
//					sequence.addFirst(j);
//					int count = 0;
//					for (Integer perm : permsLeft) {
//						count += (is_a_sublist(perms[perm],sequence.toArray()) ? 1 : 0);
//					}
//					sequence.remove(0);
//					if (count > max) {
//						max = count;
//						num = j;
//					}
//				}
//				if (num > -1) {
//					if (first) {
//						sequence.add(num);
//					}
//					else {
//						sequence.addFirst(num);
//					}
//					for (Integer perm : permsLeft) {
//						if(is_a_sublist(perms[perm],sequence.toArray())) {
//							permsLeft.remove(perm);
//						}
//					}
//					System.out.println(sequence);
//				}
//			}
//		}
//
//





		public static void main(String[] args) {

			 for (int list_size = 4; list_size < 85; list_size++) {
			 	System.out.println("Checking list size " + list_size);
			 	long s = System.currentTimeMillis();
			 	for (Integer[] rows : product(4, list_size)) {
			 		boolean all_in = true;
			 		for (int[] perm : perms) {
			 			if (!is_a_sublist(perm, rows)) {
			 				all_in = false;
			 				break;
			 			}
			 		}
			 		if (all_in) {
			 			System.out.println(Arrays.toString(rows));
			 			break;
			 		}
			 	}
			 	System.out.println("\t\t\telapsed: " + ((System.currentTimeMillis() - s) / 1000.0) + " sec");
			 }



//			for (int list_size = 2; list_size < 85; list_size++) {
//				System.out.println("Checking list size " + list_size);
//				long s = System.currentTimeMillis();
//				ArrayList<Integer>[] al = new ArrayList[list_size];
//				List<List<Integer>> al2 = new ArrayList<List<Integer>>();
//				for (int i = 0; i < list_size; i++) {
//					ArrayList<Integer> l = new ArrayList<Integer>();
//					l.add(0);
//					l.add(1);
//					l.add(2);
//					l.add(3);
//					// stuff[i] = l;
//					al[i] = l;
//					al2.add(l);
//				}
//				// System.out.println(product(al));
////				System.out.println(computeCombinations3(al2));
//				//[[0, 0], [0, 1], [0, 2], [0, 3], [1, 0], [1, 1], [1, 2], [1, 3], [2, 0], [2, 1], [2, 2], [2, 3], [3, 0], [3, 1], [3, 2], [3, 3]]
//				System.out.println("\t\t\telapsed: " + ((System.currentTimeMillis() - s) / 1000.0) + " sec");
//			}




		}


}
