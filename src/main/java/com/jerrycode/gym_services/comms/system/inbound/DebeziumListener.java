//package com.jerrycode.gym_services.comms.system.inbound;
//
//import com.jerrycode.gym_services.data.dto.DebeziumEventDTO;
//import com.jerrycode.gym_services.data.dto.InvoicesDTO;
//import com.jerrycode.gym_services.data.dto.PayloadDTO;
//import org.modelmapper.ModelMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.cloud.stream.annotation.StreamListener;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.stereotype.Service;
//
//@Service
//public class DebeziumListener {
//
//    private static final Logger logger = LoggerFactory.getLogger(DebeziumListener.class);
//
//
//    @Autowired
//    @Qualifier("threadPoolTaskExecutor")
//    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
//
//    @Autowired
//    private ModelMapper modelMapper;
//
//    @StreamListener(target = "loan_terms")
//    public void processLoanTermsEvent(DebeziumEventDTO event) {
//        threadPoolTaskExecutor.execute(() -> process(event));
//    }
//
//    private void process(DebeziumEventDTO event) {
//        PayloadDTO payload = event.getPayload();
//        String operation = payload.getOperation();
//
//        logger.info("Received event with operation: {}", event);
//
//        switch (operation) {
//            case "c":
//                handleCreate(payload.getAfter());
//                break;
//            case "u":
//                handleUpdate(payload.getAfter());
//                break;
//            case "d":
//                handleDelete(payload.getBefore());
//                break;
//            case "r": // Snapshot (initial read)
//                handleCreate(payload.getAfter());
//                break;
//            default:
//                logger.warn("Unknown operation type: {}", operation);
//        }
//    }
//
//    private void handleCreate(InvoicesDTO data) {
//        logger.info("Processing CREATE for loan account {}", data);
//        //loanTermsRepository.insertLoanTerms(data);
//    }
//
//    private void handleUpdate(InvoicesDTO data) {
//        logger.info("Processing UPDATE for loan account {}", data);
//       // loanTermsRepository.updateNativeLoanTerms(data);
//    }
//
//    private void handleDelete(InvoicesDTO data) {
//        logger.info("Processing DELETE for loan account {}", data);
//      //  loanTermsRepository.deleteNativeLoanTerms(data.getId());
//    }
//}
