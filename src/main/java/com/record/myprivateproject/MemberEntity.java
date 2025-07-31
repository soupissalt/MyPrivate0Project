package com.record.myprivateproject;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(name = "members")
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //ID
    private String name; //이름
    private LocalDate dob; //생일
    @Transient //민감한 정보를 직렬화 처리를 해버려 메모리에는 저장돼지만 노출되지 않도록 null값으로 변환되어 보여주는 역할을 한다.
    private Integer age; //나이
    private String gender; //성별
    private String email; //이메일
    private LocalDateTime createid; //ID 생성일자
    public MemberEntity(){
    }

    public MemberEntity(Long id,
                        String name,
                        LocalDate dob,
                        String gender,
                        String email,
                        LocalDateTime createid) {
        this.id = id;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.createid = createid;
    }

    public MemberEntity(String name,
                        LocalDate dob,
                        String gender,
                        String email,
                        LocalDateTime createid) {
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.createid = createid;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDob() {
        return dob;
    }

    public Integer getAge() {
        return Period.between(dob, LocalDate.now()).getYears();
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreateid() {
        return createid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCreateid(LocalDateTime createid) {
        this.createid = createid;
    }

    @Override
    public String toString() {
        return "MemberDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dob=" + dob +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", email='" + email + '\'' +
                ", createId=" + createid +
                '}';
    }
}
