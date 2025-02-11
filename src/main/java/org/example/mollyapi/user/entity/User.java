package org.example.mollyapi.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mollyapi.common.entity.Base;
import org.example.mollyapi.user.auth.entity.Auth;
import org.example.mollyapi.user.dto.UpdateUserReqDto;
import org.example.mollyapi.user.type.Sex;

import java.time.LocalDate;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "user")
public class User extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(length = 11, nullable = false)
    private String cellPhone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Column(nullable = false)
    private Boolean flag;

    private String profileImage;

    private LocalDate birth;

    private Integer point;

    private String name;




    public boolean updateUser(UpdateUserReqDto updateUserReqDto){
        boolean isUpdate = false;

        if( !updateUserReqDto.name().isBlank()
                && !this.name.equals(updateUserReqDto.name())){
            this.name = updateUserReqDto.name();
            isUpdate = true;
        }
        if(!updateUserReqDto.cellPhone().isBlank()
                && !this.cellPhone.equals(updateUserReqDto.cellPhone())){
            this.cellPhone = updateUserReqDto.cellPhone();
            isUpdate = true;
        }

        if(!this.birth.isEqual(updateUserReqDto.birth())){
            this.birth = updateUserReqDto.birth();
            isUpdate = true;
        }

        if (!updateUserReqDto.nickname().isBlank()
                && !this.nickname.equals(updateUserReqDto.nickname())){
            this.nickname = updateUserReqDto.nickname();
            isUpdate = true;
        }

        if (!this.sex.equals(updateUserReqDto.sex())){
            this.sex = updateUserReqDto.sex();
            isUpdate = true;
        }

        return isUpdate;
    }

    public void updateFlag(){
        this.flag = true;
    }

}
