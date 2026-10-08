package com.library.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.PageResponse;
import com.library.dto.book.AddCopiesRequest;
import com.library.dto.book.BookRequest;
import com.library.dto.book.BookResponse;
import com.library.dto.book.CopyResponse;
import com.library.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Manage books and their copies")
public class BookController {

	private final BookService bookService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('LIBRARIAN')")
	@Operation(summary = "Add a new book (librarian)")
	public BookResponse create(@Valid @RequestBody BookRequest request) {
		return bookService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('LIBRARIAN')")
	@Operation(summary = "Edit a book (librarian)")
	public BookResponse update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
		return bookService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('LIBRARIAN')")
	@Operation(summary = "Delete a book that has no loan or reservation history (librarian)")
	public void delete(@PathVariable Long id) {
		bookService.delete(id);
	}

	@GetMapping("/{id}")
	@Operation(summary = "View a book with its copy counts")
	public BookResponse getById(@PathVariable Long id) {
		return bookService.getById(id);
	}

	@GetMapping
	@Operation(summary = "Search books by title (partial), author (partial), category and availability")
	public PageResponse<BookResponse> search(
			@RequestParam(required = false) String title,
			@RequestParam(required = false) String author,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) Boolean available,
			@ParameterObject @PageableDefault(size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
		return bookService.search(title, author, category, available, pageable);
	}

	@PostMapping("/{id}/copies")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('LIBRARIAN')")
	@Operation(summary = "Add copies to a book (librarian)")
	public List<CopyResponse> addCopies(@PathVariable Long id, @Valid @RequestBody AddCopiesRequest request) {
		return bookService.addCopies(id, request.count());
	}

	@GetMapping("/{id}/copies")
	@Operation(summary = "List copies of a book with their status")
	public PageResponse<CopyResponse> listCopies(@PathVariable Long id,
			@ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
		return bookService.listCopies(id, pageable);
	}
}
