package com.ciav.staceymeals.controller;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(0)
public class ResponseTimeFilter implements Filter {
	@Override
	public void doFilter(
			ServletRequest request,
			ServletResponse response,
			FilterChain chain) throws ServletException, IOException {

		long runTime = System.currentTimeMillis();

		chain.doFilter(request, response);
		runTime = System.currentTimeMillis() - runTime;
		log.info("runtime = {}ms ", runTime);
	}
}
