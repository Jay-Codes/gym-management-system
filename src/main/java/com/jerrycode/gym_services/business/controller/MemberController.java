package com.jerrycode.gym_services.business.controller;

import com.jerrycode.gym_services.business.service.MemberManager;
import com.jerrycode.gym_services.request.MemberRequest;
import com.jerrycode.gym_services.response.MemberResponse;
import com.jerrycode.gym_services.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

import static com.jerrycode.gym_services.response.BuildResponseEntity.buildResponseEntity;

@RestController
@RequestMapping("/api")
public class MemberController {

    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
    private final MemberManager memberManager;

    public MemberController(MemberManager memberManager) {
        this.memberManager = memberManager;
    }

    @PostMapping("/add-member")
    public ResponseEntity<Response<MemberResponse>> addMember(@Valid @RequestBody MemberRequest request) {
        logger.info("Processing add member request: {}" ,request);
        Response<MemberResponse> response = memberManager.addMember(request);
        logger.info("Processed member response {}",response);
        return buildResponseEntity(response);
    }

    @GetMapping("/all-members")
    public ResponseEntity<Response<List<MemberResponse>>> getAllMembers() {
        logger.info("Getting all members");
        Response<List<MemberResponse>> response = memberManager.getAllMembers();
        logger.info("Fetched members response");
        return buildResponseEntity(response);
    }

    @GetMapping("/member/{id}")
    public ResponseEntity<Response<MemberResponse>> getMemberById(@PathVariable @Positive Long id) {
        logger.info("Fetching member with id: {}", id);
        Response<MemberResponse> response = memberManager.getMemberById(id);
        logger.info("Fetched member response {}",response);
        return buildResponseEntity(response);
    }

    @PutMapping("/member/{id}/update")
    public ResponseEntity<Response<MemberResponse>> updateMember(
            @PathVariable @Positive Long id,
            @Valid @RequestBody MemberRequest request) {
        logger.info("Updating member with id: {}", id);
        Response<MemberResponse> response = memberManager.updateMember(id, request);
        logger.info("Updated member response {}",response);
        return buildResponseEntity(response);
    }

    @DeleteMapping("/member/{id}/delete")
    public ResponseEntity<Response<MemberResponse>> deleteMember(@PathVariable @Positive Long id) {
        logger.info("Deleting member with id: {}", id);
        Response<MemberResponse> response = memberManager.deleteMember(id);
        logger.info("Delete member response {}",response);
        return buildResponseEntity(response);
    }

    @GetMapping("/total-members")
    public ResponseEntity<Response<Long>> getTotalMembers() {
        logger.info("Fetching total number of members");
        Response<Long> response = memberManager.getTotalMembers();
        logger.info("Fetched total members response {}",response);
        return buildResponseEntity(response);
    }

}