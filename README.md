# nio_chat_Client
Chat Protocol <br>
https://gitlab.com/jk6841/nio-chat-protocol/-/tree/jk
<br>
현재 가능한 시나리오
#### (1)  
a,b,c 로그인 -> a 가 방만든후 b 초대 -> b가 대화하다가 끊김(꺼버림), a는 계속 채팅중 -> <br>
b가 재로그인함 ,showroom 을 통해 방을 본후 enterroom 방번호 로 들어가면 a가 썼던 채팅들을 보여줌 , a,c동일하게 됨
<br><br>
파일 업로드, 다운로드, 목록 보기 가능 기존에 같은 방에 없던 유저가 방에 들어오면 다운로드가능!!
### (1) Request 
기능|Request Number|명령어|
|:-----:|:-----:|:----:|
|로그인|0|\login (name)|
|로그아웃|1|\logout|
|텍스트 보내기|2|(text to send)|
|파일 업로드|3|\uploadfile|
|파일 목록|4|\showfile|
|파일 다운로드|5|\downloadfile|
|파일 제거|6|\deletefile|
|방 개설|7|\createroom|
|방 탈퇴|8|\exitroom|
|방 초대|9|\inviteuser|
|방 참여자 목록|10|\showuser|
|방 목록|11|\showroom|

