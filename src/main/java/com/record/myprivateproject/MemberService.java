package com.record.myprivateproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberEntity> getMembers() {
        return null;
    }

    public void addNewMember(MemberEntity member) {
        Optional<MemberEntity> memberEmail= memberRepository.findMemberEntitiesByEmail(member.getEmail());
        if(memberEmail.isPresent()) {
            throw new IllegalStateException("Email taken ");
        }

        member.setCreateid(LocalDateTime.now());
        memberRepository.save(member);
        System.out.println(member);
    }
}
