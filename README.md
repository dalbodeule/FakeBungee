# FakeBungee

## BungeeCord 가 적용된 서버처럼 보이게 하기 위한 플러그인 프로젝트

## 기능 목록

 - [x] Region 제작 (@ranolp)
 - [x] Region을 기준으로 채팅채널 분리
 - [x] Region을 기준으로 유저리스트 분리
 - [x] Region을 기준으로 적용되는 리소스팩 다르게 적용
 - [ ] Commend Alias 제작 (진행중)
 - [x] Header/Footer NewLine 적용
 - [x] Header/Footer null 일 때 대처
 
## COMMAND

 - /ping FakeBungee 작동확인
 - /fregion \<subcommand\> \<region name\> Region에 옵션 적용
 - /fresource \<subcommand\> \<resource name\> Resource에 에셋 설정
 - /fheader \<suubcommand\> \<header name\> Header에 에셋 설정
 - /ffooter \<subcommand\< \<footer name\> Footer에 에셋 설정
 - /resource 리소스팩 다운로드 재시도
 
## Depends
 - bukkit (spigot/paper)
    - recommend ver 1.14.x
 - WorldEdit
   - https://github.com/EngineHub/WorldEdit
   - recommend ver 7.x
 - ProtocolLib
   - https://github.com/dmulloy2/ProtocolLib/
   - recommend ver 4.5.0
   
## SoftDepends
 - Citizens
   - https://ci.citizensnpcs.co/job/Citizens2/
   - recommend ver 2.0.25
   - Citizens과 FakeBungee를 같이 사용할 경우 Citizens이 전송하는 데이터를 FakeBungee가 막는 이슈를 해결
 
## License

AGPL-3.0
