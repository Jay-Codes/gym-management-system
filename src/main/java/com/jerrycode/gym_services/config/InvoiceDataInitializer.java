package com.jerrycode.gym_services.config;

import com.jerrycode.gym_services.data.dao.InvoiceRepository;
import com.jerrycode.gym_services.data.vo.Invoices;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;

@Component
public class InvoiceDataInitializer {

    private final InvoiceRepository invoiceRepository;

    public InvoiceDataInitializer(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @PostConstruct
    public void init() {
        if (invoiceRepository.count() == 0) {
            Invoices invoice1 = Invoices.builder()
                    .userName("John Doe")
                    .userPhone("1234567890")
                    .userEmail("john.doe@example.com")
                    .memberId(1L)
                    .memberName("Jane Smith")
                    .memberPhone("0987654321")
                    .amountPaid(200.0)
                    .status("paid")
                    .paid(true)
                    .packageName("Monthly Membership")
                    .discountPercentage(10)
                    .startDate(LocalDate.of(2023, 9, 1))
                    .endDate(LocalDate.of(2023, 9, 30))
                    .build();

            Invoices invoice2 = Invoices.builder()
                    .userName("Alice Johnson")
                    .userPhone("2345678901")
                    .userEmail("alice.j@example.com")
                    .memberId(2L)
                    .memberName("Bob Brown")
                    .memberPhone("8765432109")
                    .amountPaid(150)
                    .status("unpaid")
                    .paid(false)
                    .packageName("Quarterly Membership")
                    .discountPercentage(5)
                    .startDate(LocalDate.of(2023, 8, 1))
                    .endDate(LocalDate.of(2023, 10, 31))
                    .build();

            // Add more invoices as needed...

            invoiceRepository.save(invoice1);
            invoiceRepository.save(invoice2);

            System.out.println("Sample invoices initialized successfully");
        }
    }
}