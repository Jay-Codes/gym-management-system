//package com.jerrycode.gym_services.comms.system;
//
//import org.springframework.cloud.stream.annotation.Input;
//import org.springframework.cloud.stream.annotation.Output;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.SubscribableChannel;
//
//
//public interface DataInjector {
//
//	String MEMBER_EVENTS_OUT = "member-events-out";
//	String INVOICE_EVENTS_OUT = "invoice-events-out";
//	String LOAN_TERMS_IN = "loan_terms";
//
//	@Input(LOAN_TERMS_IN)
//	SubscribableChannel createLoanTerms();
//
//	@Output(MEMBER_EVENTS_OUT)
//	MessageChannel memberEvents();
//
//	@Output(INVOICE_EVENTS_OUT)
//	MessageChannel invoiceEvents();
//
//}
