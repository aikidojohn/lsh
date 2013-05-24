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

public class LSH {
	private final int k;
	private final int l;
	private Map<Long, List<Dress>> table = new HashMap<Long, List<Dress>>();
	private List<G_Hamming> G = new ArrayList<G_Hamming>();
	private List<Dress> catalog;
	private Random random = new Random();
	
	public LSH(int k, int l, List<Dress> catalog) {
		this.k = k;
		this.l = l;
		this.catalog = catalog;
		//Choose l functions uniformly at random from G
		for (int i =0; i < l; i++) {
			G.add(new G_Hamming(k, catalog.size()));
		}
		
		//Hash each point in the l buckets
		for (Dress dress : catalog) {
			for (G_Hamming g : G) {
				long hashcode = g.apply(dress.getFeatureBitSet());
				if (hashcode > 0) {
					List<Dress> bucket = table.get(hashcode);
					if (bucket == null) {
						bucket = new ArrayList<Dress>();
						table.put(hashcode, bucket);
					}
					bucket.add(dress);
				}
			}
		}
	}
	
	public Set<Dress> query(BitSet featureQuery, int maxDistance) {
		Set<Dress> result = new HashSet<Dress>();
		for (G_Hamming g : G) {
			//for each bucket that in g(featureQuery)
			List<Dress> bucket = table.get(g.apply(featureQuery));
			if (bucket == null) {
				continue;
			}
			
			//find dresses with the specified distance
			for (Dress dress : bucket) {
				if (hammingDistance(featureQuery, dress.getFeatureBitSet()) <= maxDistance) {
					result.add(dress);
				}
			}
		}
		return result;
	}
	
	public Set<Dress> linearQuery(BitSet featureQuery, int maxDistance) {
		Set<Dress> result = new HashSet<Dress>();
		for (Dress d : catalog) {
			if (hammingDistance(featureQuery, d.getFeatureBitSet()) <= maxDistance) {
				result.add(d);
			}
		}
		return result;
	}
	
	public String printStats() {
		StringBuilder sb = new StringBuilder();
		sb.append("Buckets: ");
		sb.append(table.size());
		for (Map.Entry<Long, List<Dress>> entry : table.entrySet()) {
			sb.append("\n\tBucket Key ");
			sb.append(entry.getKey());
			sb.append(" size ");
			sb.append(entry.getValue().size());
		}
		
		return sb.toString();
	}
	
	private int hammingDistance(BitSet a, BitSet b) {
		BitSet clone = (BitSet)a.clone();
		clone.xor(b);
		return clone.cardinality();
	}
	
	private static class G_Hamming {
		private static Long[] primes = new Long[] {
			2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L, 29L, 
			31L, 37L, 39L, 41L, 43L, 47L, 51L, 53L, 57L, 59L, 
			61L, 67L, 71L, 73L, 79L, 83L, 89L, 97L, 101L, 103L,
			107L, 109L, 113L, 127L, 131L, 137L };
		
		private List<Integer> bits = new ArrayList<Integer>();
		private Random random = new Random();
		
		public G_Hamming(int k, int maxBit) {
			for (int i =0; i < k; i++) {
				bits.add(random.nextInt(maxBit));
			}
			
		}
		
		public long apply(BitSet features) {
			long hashcode = 1;
			
			for (int i =0; i < bits.size(); i++) {
				if (features.get(bits.get(i)))
				hashcode *= primes[i];
			}
			
			if (hashcode == 1) {
				return 0;
			}
			return hashcode;
		}
		
	}
	
	public static void main(String[] args) {
	
		List<Dress> catalog = new ArrayList<Dress>();
		long start = System.currentTimeMillis();
		for (int i =0; i < 10000; i++) {
			Dress d = Dress.generateRandom();
			catalog.add(d);
		}
		long end = System.currentTimeMillis();
		System.out.println("Time to generate dresses: " + (end - start) + " ms");
		
		start = System.currentTimeMillis();
		LSH lsh = new LSH(32, 1000, catalog);
		end = System.currentTimeMillis();
		System.out.println("Time to generate LSH table: " + (end - start) + " ms");
		System.out.println();
		System.out.println(lsh.printStats());
		System.out.println();
		
		List<Integer> features = lsh.generateRandomFeatures();
		Dress queryDress = new Dress("Query Dress", features);
		BitSet queryPoint= queryDress.getFeatureBitSet();
		
		
		start = System.currentTimeMillis();
		Set<Dress> results = lsh.query(queryPoint, 2);
		end = System.currentTimeMillis();
		System.out.println("Time to query: " + (end - start) + " ms");
		
		System.out.println("Query: " + queryDress.getFeatures());
		for (Dress d: results) {
			System.out.println(d);
		}
		System.out.println();
		
		int sampleQuerySize = 2000;
		BitSet[] sampleQueries = new BitSet[sampleQuerySize];
		for (int i = 0; i < sampleQuerySize; i++) {
			List<Integer> f = lsh.generateRandomFeatures();
			Dress d = new Dress("Query Dress", f);
			sampleQueries[i] = d.getFeatureBitSet();
		}
		
		System.out.println("Running random lsh queries");
		start = System.currentTimeMillis();
		for (int i = 0; i < sampleQuerySize; i++) {
			Set<Dress> qr = lsh.query(sampleQueries[i], 2);
			qr.size();
		}
		end = System.currentTimeMillis();
		System.out.println("Time to run " + sampleQuerySize + " random queries: " + (end - start) + " ms");
		System.out.println("Average query time = " + (end - start)/sampleQuerySize + " ms");
		
		System.out.println();
		System.out.println("Running random linear queries");
		start = System.currentTimeMillis();
		for (int i = 0; i < sampleQuerySize; i++) {
			Set<Dress> qr = lsh.linearQuery(sampleQueries[i], 2);
			qr.size();
		}
		end = System.currentTimeMillis();
		System.out.println("Time to run " + sampleQuerySize + " random queries: " + (end - start) + " ms");
		System.out.println("Average query time = " + (end - start)/sampleQuerySize + " ms");
	}
	
	public List<Integer> generateRandomFeatures() {
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
		
		return features;
	}
}
