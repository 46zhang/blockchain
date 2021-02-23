package com.gdut.fundraising.exception;


import com.gdut.fundraising.constant.Status;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class BaseException extends RuntimeException {
	private Integer code;
	private String message;

	public BaseException(Status status) {
		super(status.getMessage());
		this.code = status.getCode();
		this.message = status.getMessage();
	}

	public BaseException(Integer code, String message) {
		super(message);
		this.code = code;
		this.message = message;
	}
}
