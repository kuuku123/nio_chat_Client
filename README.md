# nio_chat_Client <br>

executable_Client_server.zip 안에 bin 에 들어가 스크립트를 실행시키면 사용가능

Chat Protocol <br>
https://gitlab.com/jk6841/nio-chat-protocol/-/tree/jk
<br>

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
<br>
<br>
<h3> (2) 구현한 기능들 </h3>
<br>
로그인<br>
클라이언트가 서버에 연결 시도<br>
연결되면 서버에서 Client객체를 만들어서 연결된 SocketChannel을 주입시키고 read 메소드를 호출하고 ,연결되었음을 클라이언트에게 알림<br>
클라이언트가 서버랑 성공적으로 연결이 되었음을 인식하고 userId로 로그인을 시도<br>
성공 시 서버가 성공했다는 response 전송<br>
<br>
로그아웃<br>
클라이언트가 로그아웃 request를 서버에게 전달<br>
서버는 해당 Client 객체 필드 중 SocketChannel을 close하고 상태를 로그아웃으로 수정<br>
속한 방이 존재하면 해당 Room 객체의 UserState를 관리하는 필드에서 해당 Client가 로그아웃 인 것을 반영<br>
다시 같은 userId로 로그인 시도 시 서버에는 해당 userId를 가지고 있는 Client객체가 존재함을 확인하고 SoketChannel 필드에 새로 현재 연결 성공한 SocketChannel을 주입<br>
<br>
방 개설<br>
클라이언트가 방 이름과 함께 request를 서버에게 전달<br>
서버에서 주어진 정보를 통해 Room 객체를 새로 만들고 해당 Client객체의 curRoom 필드는 이 방으로 변경 <br>
성공 시 서버가 성공했다는 response 방번호를 담아서 전송<br>
해당 방 번호를 Client가 로컬에 저장을 해 놓고 추후 복구용으로 사용<br>
<br>
방 초대<br>
클라이언트가 초대할 userId들 과 초대할 방번호를 request에 담아 서버에게 전달<br>
서버는 초대당한 userId를 통해 해당 Client 객체를 찾아 curRoom 필드를 초대한 Room 객체로 교체하고 Room 객체의 clientlist에도 추가<br>
성공 시 서버가 성공했다는 response를 초대자에게 전송<br>
방에 있는 모든 client객체들에게 초대당한 user Id들을 Broadcast로 전송<br>
Broadcast를 받은 클라이언트들도 로컬에 초대된 방번호를 기록<br>
<br>
방 참여자 목록<br>
클라이언트가 방 참여자 목록 조회 request를 서버에게 전달<br>
request를 보낸 Client 객체의 curRoom 을 통해서 해당 Room 객체의 userList와 userState를 참고하여 알맞게 Response에 담아 전달<br>
<br>
방 목록<br>
클라이언트가 방 목록 조회 request를 서버에게 전달<br>
request를 보낸 Client 객체가 현재 지니고 있는 roomList를 참고하여 총 방 개수와  (방 번호 , 방 이름, 사람수 , 안 읽은 메시지 개수)를 response에 보내줌<br>
<br>
방 입장<br>
클라이언트가 입장할 방 번호와 함께 request를 전달<br>
서버에서 해당 Client객체의 curRoom 을 입장하려는 Room 객체로 바꾸고 해당 Room 객체에서 textList를 조회하여 이 Client객체가 읽지 않은 textId를 response에 담아 전달해줌<br>
클라이언트가 해당 textId를 보고 안 읽은 만큼 콘솔에 순서대로 뿌려줌<br>
<br>
방 퇴장<br>
클라이언트가 방 퇴장 request 를 전달<br>
서버는 해당 Client 객체의 curRoom 을 null 처리하고 Room에서의 userState를 2(로그인 되어있으나 현재방에 없음)으로 바꿈<br>
<br>
방 탈퇴<br>
클라이언트가 방 탈퇴 request를 서버에게 전달<br>
기존의 "방 퇴장" 과는 다르게 서버에서 해당 Client 객체의 roomList에서 완전히 제거 해버리고 해당 Room 객체에서도 clientList에서 제거해버린다.<br>
성공 시 서버가 성공했다는 response 전송<br>
<br>
텍스트 보내기<br>
클라이언트가 텍스트 request를 서버에게 전달<br>
보낸 클라이언트에게 서버에게 잘 도달했다는 성공 response 전송<br>
같은 방에 있는 모든 클라이언트에게 broadcast 형식으로 해당 텍스트를 뿌림<br>
이때 같은방에 있는 클라이언트가 로그아웃, 강제 종료된 상태인 경우 send를 실패하게 되는데 이 때 fail callback에서 전송 실패한 text broadcast들을 Client객체 필드 에 저장을 해두었다가<br>
해당 Client가 같은 userId로 재 로그인 성공 시 다시 전송하게 됨<br>
클라이언트는 받은 Text Broadcast를 로컬에 다 기록<br>
<br>
파일 등록<br>
파일의 이름(확장자까지) , 총 파일 크기를 request에 담아 서버에게 전달<br>
서버는 해당 정보를 기반으로 File객체를 생성하고 해당Client가 속한 Room 객체 필드에 fileList에 추가<br>
경로는 ./temp_db/{roomNum}/{fileNum}/{fileName} 으로 만들어서 중복으로 보내도 구분할수 있도록함 <br>
아직 해당 경로에는 실제 파일이 존재하지 않고 업로드 request시 실제 데이터가 오면 채워 넣음<br>
이런 등록 과정이 있는 이유는 보내려 하는 파일이 커서 chunk로 짤려 오게 되는 경우<br>
해당 파일 chunk가 어느 파일의 일부 인지를 서버가 파일 번호를 보고 구별하기 위함<br>
<br>
파일 업로드<br>
클라이언트가 파일 번호, 파일 포지션 , 파일 데이터 를 담아 request를 서버에게 전달<br>
서버는 미리 "파일 등록" request 처리 시 등록된 File 객체를 기준으로 파일을 담게 됨<br>
파일 포지션의 경우는 전체 파일 중 몇 번째 index용 파일 chunk인지 알려주는 용도<br>
성공 시 서버가 성공했다는 response 전송 과 함께 방에 있는 모든 사람에 업로드가 완료 되었다는 broadcast 전송<br>
<br>
파일 목록<br>
클라이언트가 파일 목록 request를 서버에게 전달<br>
서버가 request를 보낸 Client객체가 현재 속한 방의 file list를 담아 보내줌<br>
성공 시 서버가 성공했다는 response (파일수,<파일번호, 파일이름, 파일크키> response 전송<br>
<br>
파일 다운로드<br>
클라이언트가 다운로드 할 file Number를 request에 담아 서버에게 전달<br>
업로드 한 방법이랑 정확히 반대로 동작함<br>
동시에 여러 개를 다운로드 하지 않고 하나만 다운로드 받는다 가정하여 등록 과정이 없음<br>
성공 시 서버가 성공했다는 response와 파일 chunk 전송<br>
<br>
파일 제거<br>
클라이언트가 제거할 file Number 를 request에 담아 서버에게 전달<br>
삭제 성공 시 서버가 성공했다는 response 전송<br>
<br>
<h3>시나리오</h3>
<br>
a b c d 로그인<br>
    <ul>
    a가 방만들고 b , c 초대<br>
    얘기하다가 c를 강제종료 시킴<br>
    \showuser 로  c가 강제종료된 상태 확인<br>
    채팅하면 안읽은 횟수 1 인거 확인<br>
    다시 c 재로그인후 \showroom 을 통해 안읽은 메시지수 확인<br>
    \showuser 로 c가 강제종료에서 현재 채팅방에 있지 않음 확인<br>
    a ,b 계속 채팅 쳐도 안읽은 횟수 1인거 확인<br>
    c \showroom 을 통해 안읽은 메시지수 증가 확인<br>
    다시 재로그인 하고 \enterroom 을 통해 방들어가면 안읽었던 메세지들 화면에 뜨는거 확인<br>
    </ul>
  b 로그아웃 위의 프로세스 반복 확인<br>
d 가 방만들고 a만 초대해서 채팅치면 a에게 안나타는거 확인<br>
a 가 \exitroom으로 방을 나간 후 \showroom으로 방 2개인거 확인 , 안읽은 메세지수 확인<br>
a 가 d가 만든방으로 \enterroom 을 통해 들어가면 d가 쳤던 메세지 뜨는거 확인<br>
a 가 \enrollfile 을 통해 방에 파일정보를 알려주고 uploadfile을 통해 업로드 한후 \showfile로 확인<br>
d 가 \downloadfile을 통해 다운로드 받은거 확인<br>
a 가 \deletefile 을 통해 파일 지워버리는것 확인<br>
a 가 \quitroom으로 방완전히 나가버리는거 d가 \showuser로 확인<br>
<br>
현재는 text 파일 형식으로 client가 방에서 한 채팅기록들을 (temp_db)라는 폴더에 보관하는 방식 추후에 jpa ,mysql 을 통해 db와 함꼐하는 형식으로 바꿀 예정
<br>
<br>
(spring branch) 해결이라고 하긴 애매한 이슈 왜 @Transactional이 뜻대로 동작안하는 지 모르겠음 나의 질문 링크 https://stackoverflow.com/questions/69538924/spring-transactional-not-working-inside-completionhandler-callback-method
(spring branch) UI 클래스에서 @PostConstruct를 @EventListener(ApplicationReadyEvent.class) 로 바꾸니 main thread가 더 이상 블로킹을 하지 않고 정상적으로 동작함
