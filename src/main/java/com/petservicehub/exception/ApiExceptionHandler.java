package com.petservicehub.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ProblemDetail handleNotFound(ResourceNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		problem.setTitle("Recurso não encontrado");
		return problem;
	}

	@ExceptionHandler(BusinessException.class)
	public ProblemDetail handleBusiness(BusinessException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
		problem.setTitle(exception.getStatus() == HttpStatus.CONFLICT ? "Conflito" : "Regra de negócio");
		return problem;
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataIntegrity(DataIntegrityViolationException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.CONFLICT,
				"A operação viola uma restrição de integridade dos dados");
		problem.setTitle("Conflito");
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais campos estão inválidos");
		problem.setTitle("Erro de validação");
		problem.setProperty("erros", fieldErrors(exception));
		return problem;
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
		Map<String, String> erros = new LinkedHashMap<>();
		exception.getConstraintViolations().forEach(violation ->
				erros.put(violation.getPropertyPath().toString(), violation.getMessage()));

		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais campos estão inválidos");
		problem.setTitle("Erro de validação");
		problem.setProperty("erros", erros);
		return problem;
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ProblemDetail handleUnreadableMessage(HttpMessageNotReadableException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"O corpo da requisição está ausente ou em formato inválido");
		problem.setTitle("Requisição inválida");
		return problem;
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"O parâmetro '%s' possui um valor inválido".formatted(exception.getName()));
		problem.setTitle("Parâmetro inválido");
		return problem;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpected(Exception exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"Ocorreu um erro inesperado ao processar a requisição");
		problem.setTitle("Erro interno");
		return problem;
	}

	private Map<String, String> fieldErrors(MethodArgumentNotValidException exception) {
		Map<String, String> erros = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(error -> erros.putIfAbsent(error.getField(), error.getDefaultMessage()));
		return erros;
	}
}
