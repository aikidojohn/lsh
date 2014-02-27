package com.johnhite.hash;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.johnhite.hash.Feature.Bust;

public class HammingLSH<T extends Hashable<BitSet>> implements LocalitySensitiveHash<T> {
	
	private Map<Long, List<T>> table = new HashMap<Long, List<T>>();
	private List<G_Hamming> G = new ArrayList<G_Hamming>();
	private List<T> catalog;
	private Random random = new Random(System.nanoTime());
	
	public HammingLSH(int k, int l, int d, List<T> catalog) {
		this.catalog = catalog;
		//Choose l functions uniformly at random from G
		for (int i =0; i < l; i++) {
			G.add(new G_Hamming(k, d));
		}
		
		//Hash each point in the l buckets
		for (T dress : catalog) {
			for (G_Hamming g : G) {
				long hashcode = g.apply(dress.getFeatures());
				if (hashcode > 0) {
					List<T> bucket = table.get(hashcode);
					if (bucket == null) {
						bucket = new ArrayList<T>();
						table.put(hashcode, bucket);
					}
					bucket.add(dress);
				}
			}
		}
	}
	
	@Override
	public Set<T> query(T query, int maxDistance) {
		Set<T> result = new HashSet<T>();
		for (G_Hamming g : G) {
			//for each bucket that in g(featureQuery)
			List<T> bucket = table.get(g.apply(query.getFeatures()));
			if (bucket == null) {
				continue;
			}
			
			//find dresses with the specified distance
			for (T dress : bucket) {
				if (hammingDistance(query.getFeatures(), dress.getFeatures()) <= maxDistance) {
					result.add(dress);
				}
			}
		}
		return result;
	}
	
	@Override
	public Set<T> linearQuery(T query, int maxDistance) {
		Set<T> result = new HashSet<T>();
		for (T d : catalog) {
			if (hammingDistance(query.getFeatures(), d.getFeatures()) <= maxDistance) {
				result.add(d);
			}
		}
		return result;
	}
	
	@Override
	public double distance(T a, T b) {
		return hammingDistance(a.getFeatures(), b.getFeatures());
	}
	
	public int hammingDistance(BitSet a, BitSet b) {
		BitSet clone = (BitSet)a.clone();
		clone.xor(b);
		return clone.cardinality();
	}
	
	public String printStats() {
		StringBuilder sb = new StringBuilder();
		//sb.append("Buckets: ");
		int bucketSize = 0;
		for (Map.Entry<Long, List<T>> entry : table.entrySet()) {
			/*sb.append("\n\tBucket Key ");
			sb.append(entry.getKey());
			sb.append(" size ");
			sb.append(entry.getValue().size());*/
			bucketSize += entry.getValue().size();
		}
		sb.append("\nTotal Buckets: ");
		sb.append(table.size());
		sb.append("\nAverage Bucket Size: ");
		sb.append(bucketSize/table.size());
		
		//sb.append("\n\n");
		//sb.append(bucketStats());
		//sb.append("\n\n");
		//sb.append(printG());
		return sb.toString();
	}
	
	public String printG() {
		StringBuilder sb = new StringBuilder();
		for (G_Hamming g : G) {
			sb.append(g.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public String bucketStats() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Long, List<T>> entry : table.entrySet()) {
			int maxNumberWithDistance1 = 0;
			int maxNumberWithDistance2 = 0;
			List<T> dresses = entry.getValue();
			for (int i = 0; i < dresses.size(); i++) {
				T d = dresses.get(i);
				int distance1 = 0;
				int distance2= 0;
				for (int j = i+1; j < dresses.size(); j++) {
					int distance = this.hammingDistance(d.getFeatures(), dresses.get(j).getFeatures());
					if (distance <= 1) {
						distance1++;
					} else {
						distance2++;
					}
				}
				maxNumberWithDistance1 = (distance1 > maxNumberWithDistance1) ? distance1 : maxNumberWithDistance1;
				maxNumberWithDistance2 = (distance2 > maxNumberWithDistance2) ? distance2 : maxNumberWithDistance2;
			}
			sb.append("bucket ");
			sb.append(entry.getKey());
			sb.append("\n\tmaxDistance1 ");
			sb.append(maxNumberWithDistance1);
			sb.append("\n\tmaxDistance2 ");
			sb.append(maxNumberWithDistance2);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	private static class G_Hamming {
		private static Long[] primes = new Long[] {
			2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L, 29L, 
			31L, 37L, 39L, 41L, 43L, 47L, 51L, 53L, 57L, 59L, 
			61L, 67L, 71L, 73L, 79L, 83L, 89L, 97L, 101L, 103L,
			107L, 109L, 113L, 127L, 131L, 137L, 139L, 149L, 151L, 157L, 
			163L, 167L, 173L, 179L, 181L, 191L, 193L, 197L, 199L, 211L,
			223L, 227L, 229L, 233L, 239L, 241L, 251L, 257L, 263L, 269L,
			271L, 277L, 281L, 283L, 293L, 307L, 311L, 313L, 317L,
			331L, 337L, 347L, 349L, 353L, 359L, 367L, 373L, 379L, 383L};
		
		private Set<Integer> bits = new HashSet<Integer>();
		private Random random = new Random(System.nanoTime());
		
		/**
		 * The function h just checks 1 random bit from the input string. G is
		 * the family of functions that maps an input to k functions h chosen
		 * uniformly at random.
		 * 
		 * @param k
		 * @param maxBit the bitlength of the feature bitset (maximum bit index + 1)
		 */
		public G_Hamming(int k, int maxBit) {
			while (bits.size() < k) {
				bits.add(random.nextInt(maxBit));
			}
			
		}
		
		public long apply(BitSet features) {
			long hashcode = 1;
			int i = 0;
			for (Integer bitIndex : bits) {
				if (features.get(bitIndex)) {
					hashcode *= primes[i];
				}
				i++;
			}
			
			if (hashcode == 1) {
				return 0;
			}
			return hashcode;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("G_Hamming [bits=");
			builder.append(bits);
			builder.append("]");
			return builder.toString();
		}
	}
	
	public static void main(String[] args) {
		//max number of random features. Min is 35
		int numberRandomFeatures = 35;
		//Max distance from query point to search
		int maxDistance = 1;
		
		//LSH parameters k and l. 
		int k = 25;
		int l = 10;
		
		//Number of random queries to run
		int sampleQuerySize = 1000;
		
		
		List<Dress> catalog = new ArrayList<Dress>();
		long start = System.currentTimeMillis();
		for (int i =0; i < 3000000; i++) {
			Dress d = Dress.generateRandom(numberRandomFeatures);
			catalog.add(d);
		}
		long end = System.currentTimeMillis();
		System.out.println("Time to generate dresses: " + (end - start) + " ms");
		
		start = System.currentTimeMillis();
		HammingLSH<Dress> lsh = new HammingLSH<Dress>(k, l, numberRandomFeatures, catalog);
		end = System.currentTimeMillis();
		System.out.println("Time to generate LSH table: " + (end - start) + " ms");
		System.out.println();
		System.out.println(lsh.printStats());
		System.out.println();
		
		List<Integer> features = lsh.generateRandomFeatures(numberRandomFeatures);
		Dress queryDress = new Dress("Query Dress", features);
		
		
		
		//Single LSH Query
		start = System.currentTimeMillis();
		Set<Dress> results = lsh.query(queryDress, maxDistance);
		end = System.currentTimeMillis();
		System.out.println("Time to query: " + (end - start) + " ms");
		
		System.out.println("Query: " + queryDress.printFeatures());
		for (Dress d: results) {
			System.out.println(d);
		}
		System.out.println();
		
		//Single Linear Query
		start = System.currentTimeMillis();
		results = lsh.linearQuery(queryDress, maxDistance);
		end = System.currentTimeMillis();
		System.out.println("Time to query: " + (end - start) + " ms");
		
		System.out.println("Query: " + queryDress.printFeatures());
		for (Dress d: results) {
			System.out.println(d);
		}
		System.out.println();
		
		//Random LSH Queries
		Dress[] sampleQueries = new Dress[sampleQuerySize];
		for (int i = 0; i < sampleQuerySize; i++) {
			List<Integer> f = lsh.generateRandomFeatures(numberRandomFeatures);
			Dress d = new Dress("Query Dress", f);
			sampleQueries[i] = d;
		}
		
		System.out.println("Running random lsh queries");
		start = System.currentTimeMillis();
		for (int i = 0; i < sampleQuerySize; i++) {
			Set<Dress> qr = lsh.query(sampleQueries[i], maxDistance);
			qr.size();
		}
		end = System.currentTimeMillis();
		System.out.println("Time to run " + sampleQuerySize + " random queries: " + (end - start) + " ms");
		System.out.println("Average query time = " + (end - start)/sampleQuerySize + " ms");
		System.out.println();
		
		//Random linear queries
		System.out.println("Running random linear queries");
		start = System.currentTimeMillis();
		for (int i = 0; i < sampleQuerySize; i++) {
			Set<Dress> qr = lsh.linearQuery(sampleQueries[i], maxDistance);
			qr.size();
		}
		end = System.currentTimeMillis();
		System.out.println("Time to run " + sampleQuerySize + " random queries: " + (end - start) + " ms");
		System.out.println("Average query time = " + (end - start)/sampleQuerySize + " ms");
		
	}
	
	public List<Integer> generateRandomFeatures(int numberRandomFeatures) {
		List<Integer> features = new ArrayList<Integer>();
		
		Bust[] bustSizes = Feature.Bust.values();
		int index = random.nextInt(bustSizes.length);
		features.add(bustSizes[index].index());
		
		Feature.Waist[] waistSizes = Feature.Waist.values();
		index = random.nextInt(waistSizes.length);
		features.add(waistSizes[index].index());
		
		Feature.Hip[] hipSizes = Feature.Hip.values();
		index = random.nextInt(hipSizes.length);
		features.add(hipSizes[index].index());

		Feature.Length[] lengthSizes = Feature.Length.values();
		index = random.nextInt(lengthSizes.length);
		features.add(lengthSizes[index].index());
		
		int featuresToGenerate = numberRandomFeatures - 34;
		if (featuresToGenerate > 0) {
			for (int i=0; i < featuresToGenerate; i++) {
				features.add(random.nextInt(featuresToGenerate) + 34);
			}
		}
		
		return features;
	}
}
