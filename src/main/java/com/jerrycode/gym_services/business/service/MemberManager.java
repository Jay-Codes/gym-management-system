package com.jerrycode.gym_services.business.service;

import com.jerrycode.gym_services.request.MemberRequest;
import com.jerrycode.gym_services.response.MemberResponse;
import com.jerrycode.gym_services.response.Response;

import java.util.List;

public interface MemberManager {
    Response<MemberResponse> addMember(MemberRequest request);
    Response<List<MemberResponse>> getAllMembers();
    Response<MemberResponse> getMemberById(Long id);
    Response<MemberResponse> updateMember(Long id, MemberRequest request);
    Response<MemberResponse> deleteMember(Long id);
    Response<Long> getTotalMembers();
}
