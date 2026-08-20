package com.janapc.online_course_system.common.exception

import jakarta.servlet.http.HttpServletRequest
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
	private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidationExceptions(
		ex: MethodArgumentNotValidException,
		request: HttpServletRequest,
	): ResponseEntity<ErrorResponse> {
		val fieldErrors =
			ex.bindingResult.fieldErrors.map {
				FieldErrorResponse(field = it.field, message = it.defaultMessage ?: "invalid value")
			}
		return buildResponse(
			status = HttpStatus.BAD_REQUEST,
			message = "Request validation failed",
			fields = fieldErrors,
			request = request,
		)
	}

	@ExceptionHandler(NotFoundException::class)
	fun handleNotFoundException(
		ex: NotFoundException,
		request: HttpServletRequest,
	): ResponseEntity<ErrorResponse> =
		buildResponse(
			HttpStatus.NOT_FOUND,
			ex.message ?: "Resource not found",
			request,
		)

	@ExceptionHandler(Exception::class)
	fun handleUnexpectedException(
		ex: Exception,
		request: HttpServletRequest,
	): ResponseEntity<ErrorResponse> {
		logger.error("Unexpected server error", ex)
		return buildResponse(
			HttpStatus.INTERNAL_SERVER_ERROR,
			"Internal Server Error",
			request,
		)
	}

	@ExceptionHandler(HttpMessageNotReadableException::class)
	fun handleMalformedJsonException(
		ex: HttpMessageNotReadableException,
		request: HttpServletRequest,
	): ResponseEntity<ErrorResponse> =
		buildResponse(
			HttpStatus.BAD_REQUEST,
			"Malformed JSON request",
			request,
		)

	@ExceptionHandler(ConflictException::class)
	fun handleConflictException(
		ex: ConflictException,
		request: HttpServletRequest,
	): ResponseEntity<ErrorResponse> =
		buildResponse(
			HttpStatus.CONFLICT,
			ex.message ?: "The operation violates a database constraint.",
			request,
		)

	@ExceptionHandler(DataIntegrityViolationException::class)
	fun handleDataIntegrityViolationException(
		ex: DataIntegrityViolationException,
		request: HttpServletRequest,
	): ResponseEntity<ErrorResponse> =
		buildResponse(
			HttpStatus.CONFLICT,
			ex.message ?: "enrollment already exists",
			request,
		)

	@ExceptionHandler(UnauthorizedException::class)
	fun handleUnauthorizedException(
		ex: UnauthorizedException,
		request: HttpServletRequest,
	): ResponseEntity<ErrorResponse> =
		buildResponse(
			HttpStatus.UNAUTHORIZED,
			ex.message ?: "Unauthorized",
			request,
		)

	@ExceptionHandler(AuthorizationDeniedException::class)
	fun handleAuthorizationDeniedException(
		ex: AuthorizationDeniedException,
		request: HttpServletRequest,
	): ResponseEntity<ErrorResponse> =
		buildResponse(
			HttpStatus.FORBIDDEN,
			ex.message ?: "You don't have permission to access this resource",
			request,
		)

	@ExceptionHandler(BadCredentialsException::class)
	fun handleBadCredentialsException(
		ex: BadCredentialsException,
		request: HttpServletRequest,
	): ResponseEntity<ErrorResponse> =
		buildResponse(
			status = HttpStatus.UNAUTHORIZED,
			message = "Invalid email or password",
			request = request,
		)

	@ExceptionHandler(AuthenticationException::class)
	fun handleAuthenticationException(
		ex: AuthenticationException,
		request: HttpServletRequest,
	): ResponseEntity<ErrorResponse> =
		buildResponse(
			status = HttpStatus.UNAUTHORIZED,
			message = ex.message ?: "Authentication error",
			request = request,
		)

	private fun buildResponse(
		status: HttpStatus,
		message: String,
		request: HttpServletRequest,
		fields: List<FieldErrorResponse> = emptyList(),
	): ResponseEntity<ErrorResponse> =
		ResponseEntity
			.status(status)
			.body(
				ErrorResponse(
					timestamp = LocalDateTime.now(),
					status = status.value(),
					message = message,
					fields = fields,
					path = request.requestURI,
					error = status.reasonPhrase,
				),
			)
}
