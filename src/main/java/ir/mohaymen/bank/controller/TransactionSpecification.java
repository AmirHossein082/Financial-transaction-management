package ir.mohaymen.bank.controller;

import ir.mohaymen.bank.model.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TransactionSpecification {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static Specification<Transaction> hasType(String type) {
        return (root, query, criteriaBuilder) ->
                type == null || type.isEmpty() ? null : criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<Transaction> hasOriginatingAccountNumber(String originatingAccountNumber) {
        return (root, query, criteriaBuilder) ->
                originatingAccountNumber == null || originatingAccountNumber.isEmpty() ? null : criteriaBuilder.equal(root.get("originatingAccountNumber"), originatingAccountNumber);
    }

    public static Specification<Transaction> hasDestinationAccountNumber(String destinationAccountNumber) {
        return (root, query, criteriaBuilder) ->
                destinationAccountNumber == null || destinationAccountNumber.isEmpty() ? null : criteriaBuilder.equal(root.get("destinationAccountNumber"), destinationAccountNumber);
    }

    public static Specification<Transaction> hasAmount(String amount) {
        return (root, query, criteriaBuilder) ->
                amount == null || amount.isEmpty() ? null : criteriaBuilder.equal(root.get("amount"), amount);
    }

    public static Specification<Transaction> hasDateBetween(String startDate, String endDate) {
        return (root, query, criteriaBuilder) ->{
            if ((startDate == null || startDate.isEmpty()) && (endDate == null || endDate.isEmpty())) {
                return null;
            }

            Predicate predicate = criteriaBuilder.conjunction();
            if (startDate != null && !startDate.isEmpty()) {
                LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("date"), start.toString()));
            }
            if (endDate != null && !endDate.isEmpty()) {
                LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("date"), end.toString()));
            }
            return predicate;
        };

    }


}
