package com.xuecheng.ucenter.model.dto;

import lombok.Data;

/*
cellphone:'',
   username:'',
   email:'',
   nickname:'',
   password:'',
   confirmpwd:'',
   checkcodekey:'',
   checkcode:''
*/
@Data
public class RegisterParams {
    private String cellphone;
    private String email;
    private String nickname;
    private String password;
    private String confirmpwd;
    private String checkcodekey;
    private String checkcode;
}
