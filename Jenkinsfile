pipeline {
    agent any

    stages {
        // 변수 설정
        stage("Set Variable") {
            steps {
                script {
                    IMAGE_NAME = "spring-example"
                    IMAGE_STORAGE = "Container Registry 경로"
                    IMAGE_STORAGE_CREDENTIAL = "Container Registry 접근 Credential id"
                    SSH_CONNECTION = "접속할 계정@배포할 서버 IP"
                    SSH_CONNECTION_CREDENTIAL = "SSH 서버 접근 Credential id"
                }
            }
        }

        // 프로젝트 빌드 및 테스트
        stage("CI: Project Build") {
            steps {
                sh "./gradlew clean build test"
            }
        }

        // 배포 대상 서버에 dockerfile, docker-compose 파일, JAR 파일 전달
        stage("CI: Transfer Files") {
            steps {
                script {
                    def remoteServer = [
                        // SSH 호스트 설정
                        name: 'HomeServer',
                        host: 'chiptune.iptime.org', // 대상 서버 주소
                        credentialsId: 'chiptune', // Jenkins 자격 증명 ID (SSH 키 또는 사용자 이름/비밀번호)
                        sourceFiles: 'build/libs/example-0.0.1-SNAPSHOT.jar', 'dockerfile', 'docker-compose.yml' // 전송할 로컬 파일 경로 및 패턴
                        remoteDirectory: '/jenkins/jenkins_deploy/springboot_example' // 대상 서버의 원격 디렉토리 경로
                    ]

                    // SSH 플러그인을 사용하여 파일 전송
                    sshPublisher(
                        publishers: [
                            sshPublisherDesc(
                                configName: remoteServer['name'],
                                transfers: [
                                    sshTransfer(
                                        execCommand: '', // 원격 명령 (비워둘 수 있음)
                                        execTimeout: 120000, // 명령 실행 제한 시간 (밀리초)
                                        flatten: false, // true로 설정하면 원격 경로에서 파일이 복사됩니다.
                                        makeEmptyDirs: false, // true로 설정하면 원격 디렉토리에 빈 디렉토리가 생성됩니다.
                                        noDefaultExcludes: false,
                                        patternSeparator: '[, ]+',
                                        remoteDirectory: remoteServer['remoteDirectory'],
                                        remoteDirectorySDF: false,
                                        removePrefix: 'build/libs', // 원본 파일 경로에서 제거할 접두사
                                        sourceFiles: remoteServer['sourceFiles']
                                    )
                                ]
                            )
                        ]
                    )
                }
            }
        }

        // 배포 서버에서 도커 이미지 빌드
        stage("CI: Docker Build") {
            steps {
                script {
                    // 원격 서버에서 Docker 이미지 빌드 명령 실행
                    sshCommand remote: remoteServer, command: 'docker build -t {IMAGE_NAME}:latest /jenkins/jenkins_deploy/springboot_example'

                    // 빌드된 이미지를 Docker 레지스트리에 푸시 (옵션)
                    // sshCommand remote: remoteServer, command: 'docker push my-docker-image:latest'
                }
            }
        }

        // 배포 서버에서 도커 빌드 된 도커 이미지 실행
        stage("CD : Deploy") {
            steps {
                script {
                    // Docker Compose를 사용하여 컨테이너 실행
                    sshCommand remote: remoteServer, command: 'docker-compose -f /jenkins/jenkins_deploy/springboot_example/docker-compose.yml up -d'
                }
            }
        }

    }
} catch (e) {
  println "Caught: ${e}"
  throw e
}
