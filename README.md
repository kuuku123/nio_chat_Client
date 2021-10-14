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
|파일 다운로드|5|\downloadfile (fileNumber)|
|파일 제거|6|\deletefile (fileNumber)|
|방 개설|7|\createroom|
|방 탈퇴|8|\exitroom|
|방 초대|9|\inviteuser|
|방 참여자 목록|10|\showuser|
|방 목록|11|\showroom|
|방 입장|12|\enterroom (roomNum)|
|파일 등록|13|\enrollfile (filePath)
|파일 정보|14|x|
|방 퇴장|15|\exitroom|

현재는 text 파일 형식으로 client가 방에서 한 채팅기록들을 (temp_db)라는 폴더에 보관하는 방식 추후에 jpa ,mysql 을 통해 db와 함꼐하는 형식으로 바꿀 예정
<br>
<br>
(spring branch) 해결이라고 하긴 애매한 이슈 왜 @Transactional이 뜻대로 동작안하는 지 모르겠음 나의 질문 링크 https://stackoverflow.com/questions/69538924/spring-transactional-not-working-inside-completionhandler-callback-method
(spring branch) UI 클래스에서 @PostConstruct를 @EventListener(ApplicationReadyEvent.class) 로 바꾸니 main thread가 더 이상 블로킹을 하지 않고 정상적으로 동작함
