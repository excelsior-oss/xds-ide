package com.excelsior.xds.core.utils;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * java 8 lambda helpers
 * @author lsa80
 */
public final class Lambdas {
	public static <T> Predicate<T> as(Predicate<T> predicate) {
		return predicate;
	}
	
	/**
	 * IF expression
	 * @param t object to test
	 * @param predicate if evaluates to {@code true} on the {@code t} then {@code iff} function evaluates to the {@code t} object itself, otherwise evaluates to {@code null}
	 * @return {@code t} itself or null
	 */
	public static <T> T iff(T t, Predicate<T> predicate) {
		return iff(() -> t, predicate, () -> t, () -> null);
	}
	
	/**
	 * IF expression
	 * @param supplier used to obtain object {@code t} of type {@code T}
	 * @param predicate used to test {@code t} of type {@code T} obtained from the {@code supplier}
	 * @param ifTrueSupplier will be evaluated (method {@link Supplier#get()} called) if {@code predicate} evaluates to true
	 * @param ifFalseSupplier will be evaluated {@code predicate} evaluates to false
	 * @return result of either {@code ifTrueSupplier} or {@code ifFalseSupplier} evaluation, depending on {@code predicate}
	 */
	public static <T, U> U iff(Supplier<T> supplier, Predicate<T> predicate, Supplier<U> ifTrueSupplier, Supplier<U> ifFalseSupplier) {
		T t = supplier.get();
		if (predicate.test(t)) {
			return ifTrueSupplier.get();
		}
		else {
			return ifFalseSupplier.get();
		}
	}
	
	public static <T> Predicate<T> not(Predicate<T> predicate) {
		return predicate.negate();
	}
	
	public static <T> Predicate<T> and(Predicate<T> predicate1, Predicate<T> predicate2) {
		return predicate1.and(predicate2);
	}
	
	public static <T> Predicate<T> or(Predicate<T> predicate1, Predicate<T> predicate2) {
		return predicate1.or(predicate2);
	}

	public static <T> Consumer<T> as(Consumer<T> consumer) {
		return consumer;
	}

	public static <T> Supplier<T> as(Supplier<T> supplier) {
		return supplier;
	}

	public static <T, R> Function<T, R> as(Function<T, R> function) {
		return function;
	}
	
	public static <T, R> Function<T, R> func(Map<T, R> m) {
		return t -> m.get(t);
	}
	
	public static <T> Predicate<T> nonnull() {
		return o -> o != null;
	}
	
	public static <T> Stream<T> toStream(Collection<T> c) {
		if (c != null){
			return c.stream();
		}
		else {
			return Stream.empty();
		}
	}
	
	public static <T> Predicate<T> TRUE() {
		return t -> {
			return true;
		};
	}
	
	public static <T> Predicate<T> FALSE() {
		return t -> {
			return false;
		};
	}
	
	/**
	 * Only static methods allowed.
	 */
	private Lambdas(){
	}
}