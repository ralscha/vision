package ch.rasc.vision.util;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ValidationMessagesResult<T> {
	private boolean success = true;

	private int total;

	private List<T> records;

	private List<ValidationMessages> validations;

	public ValidationMessagesResult(T record) {
		this.records = List.of(record);
		this.total = this.records.size();
	}

	public ValidationMessagesResult(T record, List<ValidationMessages> validations) {
		this(record);
		this.validations = validations;
		if (this.validations != null && !this.validations.isEmpty()) {
			this.success = false;
		}
	}

	public boolean isSuccess() {
		return this.success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public int getTotal() {
		return this.total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<T> getRecords() {
		return this.records;
	}

	public void setRecords(List<T> records) {
		this.records = records;
		this.total = records != null ? records.size() : 0;
	}

	public List<ValidationMessages> getValidations() {
		return this.validations;
	}

	public void setValidations(List<ValidationMessages> validations) {
		this.validations = validations;
		if (this.validations != null && !this.validations.isEmpty()) {
			this.success = false;
		}
	}

}
