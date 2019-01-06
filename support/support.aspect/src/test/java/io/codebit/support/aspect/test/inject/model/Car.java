package io.codebit.support.aspect.test.inject.model;

import javax.inject.Inject;
import javax.inject.Provider;

//https://docs.spring.io/spring/docs/3.1.x/spring-framework-reference/html/beans.html#beans-factory-method-injection
public class Car {
	private String name;
	
	private Engine engine;

	@Inject private Provider<Seat> seatProvider;
	
	private Seat childSeat;
	
	public Car(@Better Engine engine) {
		this.engine = engine;
	}
	
	public Seat seat() {
		return seatProvider.get();
	}
	
	@Inject
	public void chiledSeat(Seat seat) {
		childSeat = seat;
	}
	
	public Seat childSeat() {
		return childSeat;
	}
}