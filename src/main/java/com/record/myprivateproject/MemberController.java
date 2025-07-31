package com.record.myprivateproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/")
public class MemberController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @Autowired
    public MemberController(MemberService memberService, MemberRepository memberRepository) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
    }

    @GetMapping("v1/member")
    public List<MemberEntity> getMembers() {
        return memberService.getMembers();
    }
    @PostMapping("add/member")
    public void resisterNewMember (@RequestBody MemberEntity member) {
        memberService.addNewMember(member);
    }
}
