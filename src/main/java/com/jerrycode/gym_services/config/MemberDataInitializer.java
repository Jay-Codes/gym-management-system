package com.jerrycode.gym_services.config;

import com.jerrycode.gym_services.data.dao.MemberRepository;
import com.jerrycode.gym_services.data.vo.Member;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MemberDataInitializer {

    private final MemberRepository memberRepository;

    public MemberDataInitializer(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @PostConstruct
    public void init() {
        if (memberRepository.count() == 0) {
            Member member1 = Member.builder()
                    .name("John Doe")
                    .role("Gym Member")
                    .phoneNumber("1234567890")
                    .email("john@example.com")
                    .gender("Male")
                    .height("180")
                    .weight("75")
                    .build();

            Member member2 = Member.builder()
                    .name("Jane Smith")
                    .role("Trainer")
                    .phoneNumber("2345678901")
                    .email("jane@example.com")
                    .gender("Female")
                    .height("165")
                    .weight("60")
                    .build();

           Member member3 = Member.builder()
                   .name("Jofrey Nyamasheki")
                   .role("Customer")
                   .phoneNumber("0767413968")
                   .email("jbnyamasheki@gmail.com")
                   .gender("Male")
                   .height("170")
                   .weight("59")
                   .build();

            memberRepository.save(member1);
            memberRepository.save(member2);
            memberRepository.save(member3);

            System.out.println("Sample members initialized successfully");
        }
    }
}
