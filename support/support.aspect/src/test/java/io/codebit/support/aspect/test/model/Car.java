package io.codebit.support.aspect.test.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import javax.cache.annotation.CacheResult;
import javax.inject.Inject;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.codebit.support.aspect.annotation.Async;

public class Car {
	
	private Random random = new Random();
	
	public String model;

	/**
	 * 등록 여부 
	 * 꼭 등록되어 있어야 운행 가능 
	 */
	@AssertTrue
	private boolean isRegistered;
	
	private List<String> options = new ArrayList<String>();
	
	/**
	 * 엔진 모델 , 고객은 엔진 모델은 몰라도 됨 
	 */
	@Inject
	private Engine engine;

	private int speed = 0;
	
	public Car(@NotNull(message ="차량 모델은 필수 입니다.") String model, boolean isRegistered) {
		this.model = model;
		this.isRegistered = isRegistered;
		options.add("JBL audio");
	}
	
	public boolean isRegistered() {
		return this.isRegistered;
	}

	@CacheResult
	public void options(List<String> options) {
		this.options = options;
	}
	
	@NotNull
	@Size(min=2, max=5)
	public List<String> options() {
		return this.options;
	}
	
	public String model() {
		System.out.println("type"+ this.model + options);
		return model;
	}
	
	public int speed() {
		return this.speed;
	}
	
	public void accelerator() {
		try {
			Thread.currentThread().sleep(1000);
			System.out.println("async accelerator "+  Thread.currentThread().getName());
			this.run();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void breaker() {
		try {
			Thread.currentThread().sleep(1000);
			System.out.println("async stop "+  Thread.currentThread().getName());
			this.stop();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Async
	private void run() {
		try {
			engine.run();
			this.speed = random.ints(10, 100).findFirst().getAsInt();
			Thread.currentThread().sleep(10000);
			System.out.println("async run"+ this.model + options +" "+  Thread.currentThread().getName());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		return "run";
	}
	
	@Async
	private Future<String> stop(){
		this.speed = 0;
		return CompletableFuture.completedFuture("stop");
	}
	
}