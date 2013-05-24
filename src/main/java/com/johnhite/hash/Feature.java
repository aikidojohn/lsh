package com.johnhite.hash;

import java.util.HashMap;
import java.util.Map;

public class Feature {
	private Feature() {}
	
	private static Map<Integer, FeatureEnum> featureMap = new HashMap<Integer, FeatureEnum>();
	
	private static void addFeature(FeatureEnum feature) {
		featureMap.put(feature.index(), feature);
	}
	
	public static FeatureEnum getByIndex(Integer index) {
		return featureMap.get(index);
	}
	
	public static interface FeatureEnum {
		public int index();
		public String description();
	}
	
	public static enum Bust implements FeatureEnum {
		FITS_32(0, "Bust Fits 32"),
		FITS_33(1, "Bust Fits 33"),
		FITS_34(2, "Bust Fits 34"),
		FITS_35(3, "Bust Fits 35"),
		FITS_36(4, "Bust Fits 36"),
		FITS_37(5, "Bust Fits 37"),
		FITS_38(6, "Bust Fits 38"),
		FITS_39(7, "Bust Fits 39"),
		FITS_40(8, "Bust Fits 40"),
		FITS_41(9, "Bust Fits 41"),
		FITS_42(10, "Bust Fits 42");
		
		private int index;
		private String desc;
		private Bust(int index, String desc) {
			this.index = index;
			this.desc = desc;
			addFeature(this);
		}
		
		public int index() {
			return index;
		}
		
		public String description() {
			return desc;
		}
	}
	
	public static enum Waist implements FeatureEnum {
		FITS_24(11, "Waist Fits 24"),
		FITS_25(12, "Waist Fits 25"),
		FITS_26(13, "Waist Fits 26"),
		FITS_27(14, "Waist Fits 27"),
		FITS_28(15, "Waist Fits 28"),
		FITS_29(16, "Waist Fits 29"),
		FITS_30(17, "Waist Fits 30"),
		FITS_31(18, "Waist Fits 31"),
		FITS_32(19, "Waist Fits 32"),
		FITS_33(20, "Waist Fits 33"),
		FITS_34(21, "Waist Fits 34");
		
		private int index;
		private String desc;
		private Waist(int index, String desc) {
			this.index = index;
			this.desc = desc;
			addFeature(this);
		}
		
		public int index() {
			return index;
		}
		
		public String description() {
			return desc;
		}
	}
	
	public static enum Hip implements FeatureEnum {
		FITS_24(22, "Hip Fits 24"),
		FITS_25(23, "Hip Fits 25"),
		FITS_26(24, "Hip Fits 26"),
		FITS_27(25, "Hip Fits 27"),
		FITS_28(26, "Hip Fits 28"),
		FITS_29(27, "Hip Fits 29"),
		FITS_30(28, "Hip Fits 30"),
		FITS_31(29, "Hip Fits 31"),
		FITS_32(30, "Hip Fits 32"),
		FITS_33(31, "Hip Fits 33"),
		FITS_34(32, "Hip Fits 34");
		
		private int index;
		private String desc;
		private Hip(int index, String desc) {
			this.index = index;
			this.desc = desc;
			addFeature(this);
		}
		
		public int index() {
			return index;
		}
		
		public String description() {
			return desc;
		}
	}
	
	
	public static enum Length implements FeatureEnum {
		FITS_R(33, "Length Regular"),
		FITS_L(34, "Length Long");
		
		private int index;
		private String desc;
		private Length(int index, String desc) {
			this.index = index;
			this.desc = desc;
			addFeature(this);
		}
		
		public int index() {
			return index;
		}
		
		public String description() {
			return desc;
		}
	}
}
