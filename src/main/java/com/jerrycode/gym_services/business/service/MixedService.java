package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.data.dao.InvoiceRepository;
import com.jerrycode.gym_services.data.dao.MemberRepository;
import com.jerrycode.gym_services.data.dao.PackageRepository;
import com.jerrycode.gym_services.response.AllCountsResponse;
import com.jerrycode.gym_services.response.Response;
import com.jerrycode.gym_services.response.TimeSeriesData;
import com.jerrycode.gym_services.utils.ObjectFactory;
import com.jerrycode.gym_services.utils.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MixedService implements MixedManager {

    private static final Logger logger = LoggerFactory.getLogger(MixedService.class);
    private final MemberRepository memberRepository;
    private final InvoiceRepository invoiceRepository;
    private final PackageRepository packageRepository;

    @Override
    public Response<AllCountsResponse> fetchAllCounts() {
        Response<AllCountsResponse> response = new Response<>();

        try {
            logger.info("Processing fetch all counts request");

            AllCountsResponse counts = new AllCountsResponse();
            counts.setMembers(memberRepository.count());
            counts.setInvoices(invoiceRepository.count());
            counts.setPaidMembers(invoiceRepository.countDistinctMemberByPaidTrue());
            counts.setPackages(packageRepository.count());
            counts.setDiscounts(invoiceRepository.countByDiscountPercentageGreaterThan(0));

            LocalDate today = LocalDate.now();

            counts.setDaily(generateTimeSeriesData(today, 7, TimeUnit.DAILY));
            counts.setWeekly(generateTimeSeriesData(today, 6, TimeUnit.WEEKLY));
            counts.setMonthly(generateTimeSeriesData(today, 6, TimeUnit.MONTHLY));

            response.setSuccess(true);
            response.setMessage("Totals retrieved successfully");
            response.setData(counts);
        } catch (Exception e) {
            logger.error("Unexpected error fetching all counts: {}", e.getMessage());
            response.setSuccess(false);
            response.setMessage("An unexpected error occurred");
        }
        logger.info("Returning processed all counts response {}", response);
        return response;
    }

    private TimeSeriesData generateTimeSeriesData(LocalDate today, int range, TimeUnit timeUnit) {
        Map<String, String> labels = ObjectFactory.newOrderedStringMap();
        Map<String, Long> members = ObjectFactory.newOrderedLongMap();
        Map<String, Long> invoices = ObjectFactory.newOrderedLongMap();
        Map<String, Long> paidMembers = ObjectFactory.newOrderedLongMap();

        for (int i = 0; i < range; i++) {
            LocalDate start, end;
            String labelKey = "label_" + (i + 1);

            switch (timeUnit) {
                case DAILY:
                    start = today.minusDays(range - i - 1);
                    end = start;
                    labels.put(labelKey, start.getDayOfWeek().toString().substring(0, 3));
                    break;
                case WEEKLY:
                    start = today.minusWeeks(range - i - 1).with(java.time.DayOfWeek.MONDAY);
                    end = start.plusDays(6);
                    labels.put(labelKey, "Week " + (i + 1));
                    break;
                case MONTHLY:
                    start = today.minusMonths(range - i - 1).withDayOfMonth(1);
                    end = start.withDayOfMonth(start.lengthOfMonth());
                    labels.put(labelKey, start.getMonth().toString().substring(0, 3) + " " + start.getYear());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported time unit");
            }

            members.put("member_" + (i + 1), memberRepository.countByCreatedAtBetween(start.atStartOfDay(), end.atTime(23, 59, 59)));
            invoices.put("invoice_" + (i + 1), invoiceRepository.countByCreatedAtBetween(start.atStartOfDay(), end.atTime(23, 59, 59)));
            paidMembers.put("paid_member_" + (i + 1), invoiceRepository.countDistinctMemberByPaidTrueAndCreatedAtBetween(start.atStartOfDay(), end.atTime(23, 59, 59)));
        }

        return TimeSeriesData.builder()
                .labels(labels)
                .members(members)
                .invoices(invoices)
                .paidMembers(paidMembers)
                .build();
    }

}