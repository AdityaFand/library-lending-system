package com.library.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.PageResponse;
import com.library.dto.member.MemberResponse;
import com.library.entity.User;
import com.library.enums.Role;
import com.library.repository.LoanRepository;
import com.library.repository.MemberLoanCountView;
import com.library.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final UserRepository userRepository;
	private final LoanRepository loanRepository;
	private final Clock clock;

	@Transactional(readOnly = true)
	public PageResponse<MemberResponse> search(String query, Pageable pageable) {
		Page<User> members = query == null || query.isBlank()
				? userRepository.findByRole(Role.MEMBER, pageable)
				: userRepository.searchByRole(Role.MEMBER, "%" + escapeLike(query.trim().toLowerCase()) + "%", pageable);

		List<Long> ids = members.getContent().stream().map(User::getId).toList();
		Map<Long, MemberLoanCountView> counts = ids.isEmpty()
				? Map.of()
				: loanRepository.countOpenLoansByMemberIds(ids, LocalDate.now(clock)).stream()
						.collect(Collectors.toMap(MemberLoanCountView::getMemberId, Function.identity()));

		return PageResponse.from(members.map(member -> {
			MemberLoanCountView count = counts.get(member.getId());
			long open = count == null ? 0 : count.getOpenLoans();
			long overdue = count == null || count.getOverdueLoans() == null ? 0 : count.getOverdueLoans();
			return MemberResponse.from(member, open, overdue);
		}));
	}

	private String escapeLike(String value) {
		return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}
}
