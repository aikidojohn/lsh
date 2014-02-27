package com.johnhite.hash;

import java.util.Set;

public interface LocalitySensitiveHash<T extends Hashable<?>> {

	Set<T> query(T featureQuery, int maxDistance);
	Set<T> linearQuery(T featureQuery, int maxDistance);
	double distance(T a, T b);
}
