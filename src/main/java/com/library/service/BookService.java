package com.library.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.PageResponse;
import com.library.dto.book.BookRequest;
import com.library.dto.book.BookResponse;
import com.library.dto.book.CopyResponse;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.enums.CopyStatus;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.CopyCountView;
import com.library.repository.LoanRepository;
import com.library.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

	private final BookRepository bookRepository;
	private final BookCopyRepository copyRepository;
	private final LoanRepository loanRepository;
	private final ReservationRepository reservationRepository;

	@Transactional
	public BookResponse create(BookRequest request) {
		String isbn = request.isbn().trim();
		if (bookRepository.existsByIsbn(isbn)) {
			throw new BusinessException("A book with ISBN " + isbn + " already exists");
		}

		Book book = new Book();
		applyRequest(book, request);
		return BookResponse.from(bookRepository.save(book), 0, 0);
	}

	@Transactional
	public BookResponse update(Long id, BookRequest request) {
		Book book = findBook(id);
		String isbn = request.isbn().trim();
		if (bookRepository.existsByIsbnAndIdNot(isbn, id)) {
			throw new BusinessException("A book with ISBN " + isbn + " already exists");
		}

		applyRequest(book, request);
		return toResponse(book);
	}

	@Transactional
	public void delete(Long id) {
		Book book = findBook(id);
		if (loanRepository.existsByCopyBookId(id) || reservationRepository.existsByBookId(id)) {
			throw new BusinessException("Book has loan or reservation history and cannot be deleted");
		}

		copyRepository.deleteByBookId(id);
		bookRepository.delete(book);
	}

	@Transactional(readOnly = true)
	public BookResponse getById(Long id) {
		return toResponse(findBook(id));
	}

	@Transactional(readOnly = true)
	public PageResponse<BookResponse> list(Pageable pageable) {
		return toPageResponse(bookRepository.findAll(pageable));
	}

	@Transactional
	public List<CopyResponse> addCopies(Long bookId, int count) {
		Book book = findBook(bookId);

		List<BookCopy> copies = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			BookCopy copy = new BookCopy();
			copy.setBook(book);
			copy.setStatus(CopyStatus.AVAILABLE);
			copies.add(copy);
		}

		return copyRepository.saveAll(copies).stream()
				.map(CopyResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public PageResponse<CopyResponse> listCopies(Long bookId, Pageable pageable) {
		findBook(bookId);
		return PageResponse.from(copyRepository.findByBookId(bookId, pageable).map(CopyResponse::from));
	}

	Book findBook(Long id) {
		return bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + id));
	}

	PageResponse<BookResponse> toPageResponse(Page<Book> books) {
		Map<Long, CopyCountView> counts = loadCopyCounts(books.getContent().stream().map(Book::getId).toList());
		return PageResponse.from(books.map(book -> toResponse(book, counts.get(book.getId()))));
	}

	private BookResponse toResponse(Book book) {
		return toResponse(book, loadCopyCounts(List.of(book.getId())).get(book.getId()));
	}

	private BookResponse toResponse(Book book, CopyCountView count) {
		long total = count == null ? 0 : count.getTotal();
		long available = count == null || count.getAvailable() == null ? 0 : count.getAvailable();
		return BookResponse.from(book, total, available);
	}

	private Map<Long, CopyCountView> loadCopyCounts(List<Long> bookIds) {
		if (bookIds.isEmpty()) {
			return Map.of();
		}
		return copyRepository.countCopiesByBookIds(bookIds).stream()
				.collect(Collectors.toMap(CopyCountView::getBookId, Function.identity()));
	}

	private void applyRequest(Book book, BookRequest request) {
		book.setTitle(request.title().trim());
		book.setIsbn(request.isbn().trim());
		book.setAuthor(request.author().trim());
		book.setCategory(request.category().trim());
		book.setPrice(request.price());
	}
}
